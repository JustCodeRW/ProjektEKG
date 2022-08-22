package com.example.ekg_app

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Property
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.sign

private const val GATT_MAX_MTU_SIZE = 517
private const val GATT_MIN_MTU_SIZE = 23

object BleConnectionManager {
    private var listeners: MutableSet<WeakReference<ConnectionEventListener>> = mutableSetOf()

    private val deviceGattMap = ConcurrentHashMap<BluetoothDevice, BluetoothGatt>()
    private val operationQueue = ConcurrentLinkedQueue<BleOperationType>()
    private var pendingOperation: BleOperationType? = null

    private val characteristicService = UUID.fromString("92fae7ae-1d8e-11ed-861d-0242ac120002")
    private val characteristic = UUID.fromString("92fae9fc-1d8e-11ed-861d-0242ac120002")

    fun serviceOnDevice(device: BluetoothDevice): List<BluetoothGattService>? = deviceGattMap[device]?.services

    fun registerListener(listener: ConnectionEventListener) {
        if (listeners.map { it.get() }.contains(listener)) {
            return
        }
        listeners.add(WeakReference(listener))
        listeners = listeners.filter { it.get() != null }.toMutableSet()
        Log.d("Listeners", "Added listener $listener, ${listeners.size} listeners total")
    }

    fun unregisterListener(listener: ConnectionEventListener){
        var toRemove: WeakReference<ConnectionEventListener>? = null
        listeners.forEach {
            if (it.get() == listener) {
                toRemove = it
            }
        }
        toRemove?.let {
            listeners.remove(it)
            Log.d("Listeners", "Removed listener ${it.get()}, ${listeners.size} listeners total")
        }
    }

    fun connect(device: BluetoothDevice, context: Context) {
        if (device.isConnected()) {
            Log.d("BLE Connection", "BLE is already connected to ${device.address}")
        } else {
            enqueueOperation(Connect(device, context.applicationContext))
        }
    }

    fun tearDownConnection(device: BluetoothDevice) {
        if (device.isConnected()) {
            enqueueOperation(DisConnect(device))
        } else {
            Log.e("Teardown Error", "Not connected to ${device.address}, can´t teardown connection")
        }
    }

    fun readCharacteristic(device: BluetoothDevice, characteristic: BluetoothGattCharacteristic) {
        if (device.isConnected() && characteristic.isReadable()) {
            enqueueOperation(CharacteristicRead(device, characteristic.uuid))
        } else if (!characteristic.isReadable()) {
            Log.e("Read Error", "Attempting to read ${characteristic.uuid} that isn`t readable")
        } else if (!device.isConnected()) {
            Log.e(
                "Connection Error",
                "Not connected to ${device.address}, can´t read from characteristic"
            )
        }
    }

    fun enableNotifications(device: BluetoothDevice, characteristic: BluetoothGattCharacteristic) {
        if (device.isConnected() && (characteristic.isIndicatable() || characteristic.isNotifiable())) {
            enqueueOperation(EnableNotifications(device, characteristic.uuid))
        } else if (!device.isConnected()) {
            Log.e("Notification", "Not connected to ${device.address}, cannot enable notifications")
        }
    }

    @Synchronized
    private fun enqueueOperation(operation: BleOperationType) {
        operationQueue.add(operation)
        if (pendingOperation == null) {
            doNextOperation()
        }
    }

    @Synchronized
    private fun signalEndOfOperation() {
        Log.d("Operation End", "$pendingOperation is ending")
        pendingOperation = null
        if (operationQueue.isNotEmpty()) {
            doNextOperation()
        }
    }

    @SuppressLint("MissingPermission")
    @Synchronized
    private fun doNextOperation() {
        if (pendingOperation != null) {
            Log.e(
                "BLE Operation Error",
                "doNextOperation() called when a operation is already pending! Aborting"
            )
            return
        }

        val operation = operationQueue.poll() ?: run {
            Log.i("BLE Operation ", "Operation  queue is empty, returning")
            return
        }

        pendingOperation = operation

        if (operation is Connect) {
            with(operation) {
                Log.w("Operation Connect", "Connecting to ${device.address}")
                device.connectGatt(context, false, gattCallback)
            }
        }

        val gatt = deviceGattMap[operation.device] ?: this@BleConnectionManager.run {
            Log.e(
                "BLE availability check",
                "Not Connected to ${operation.device.address}! Aborting $operation operation"
            )
            signalEndOfOperation()
            return
        }

        when (operation) {
            is DisConnect -> with(operation) {
                Log.w("Operation Disconnect", "Disconnecting from ${device.address}")
                gatt.close()
                deviceGattMap.remove(device)
                listeners.forEach { it.get()?.onDisconnect?.invoke(device) }
                signalEndOfOperation()
            }

            is CharacteristicRead -> with(operation) {
                gatt.findCharacteristic(characteristicUUID)?.let { characteristic ->
                    gatt.readCharacteristic(characteristic)
                } ?: this@BleConnectionManager.run {
                    Log.e("Operation Read", "Can`t find $characteristicUUID to read from")
                    signalEndOfOperation()
                }
            }

            is DescriptorRead -> with(operation) {
                gatt.findDescriptor(descriptorUUID)?.let { descriptor ->
                    gatt.readDescriptor(descriptor)
                } ?: this@BleConnectionManager.run {
                    Log.e("Descriptor Read", "Can´t find $descriptorUUID to read from")
                    signalEndOfOperation()
                }
            }

            is EnableNotifications -> with(operation) {
                gatt.findCharacteristic(characteristicUUID)?.let { characteristic ->
                    val cccUuid = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB")
                    val payload = when {
                        characteristic.isIndicatable() ->
                            BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                        characteristic.isNotifiable() ->
                            BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        else ->
                            error("${characteristic.uuid} doesn't support notifications/indications")
                    }

                    characteristic.getDescriptor(cccUuid)?.let { cccDescriptor ->
                        if (!gatt.setCharacteristicNotification(characteristic, true)) {
                            Log.e("Notifications", "setCharacteristicNotification failed for ${characteristic.uuid}")
                            signalEndOfOperation()
                            return
                        }

                        cccDescriptor.value = payload
                        gatt.writeDescriptor(cccDescriptor)
                    }?:this@BleConnectionManager.run {
                        Log.e("Notifications", "${characteristic.uuid} doesn't contain the CCC descriptor")
                        signalEndOfOperation()
                    }
                }?: this@BleConnectionManager.run {
                    Log.e("Notifications","Can't find $characteristicUUID! Failed to enable notifications")
                    signalEndOfOperation()
                }
            }

            else -> {
                Log.ERROR
                return
            }
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val deviceAddress = gatt.device.address

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.w(
                        "BluetoothGattCallback",
                        "onConnectionChanged: successfully connected to $deviceAddress"
                    )
                    deviceGattMap[gatt.device] = gatt
                    Handler(Looper.getMainLooper()).post {
                        gatt.discoverServices()
                    }

                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.w(
                        "BluetoothCallback",
                        "onConnectionChanged: successfully disconnected from $deviceAddress"
                    )
                    tearDownConnection(gatt.device)
                }
            } else {
                Log.w(
                    "BluetoothGattCallback",
                    "onConnectionChanged: status $status encountered for $deviceAddress"
                )
                if (pendingOperation is Connect) {
                    signalEndOfOperation()
                }
                tearDownConnection(gatt.device)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            with(gatt) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.w(
                        "BluetoothGattCallback",
                        "Discovered ${services?.size} services for ${device?.address}"
                    )
                    printGattTable()
                    listeners.forEach { it.get()?.onConnectionSetupComplete?.invoke(this) }

                } else {
                    Log.e("BleGattCallback Error", "service discovery failed due status $status")
                    tearDownConnection(gatt.device)
                }
            }

            if (pendingOperation is Connect) {
                signalEndOfOperation()
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            with(characteristic) {
                when (status) {
                    BluetoothGatt.GATT_SUCCESS -> {
                        Log.i("READ CHARACTERISTICS ", "Read characteristic $uuid | value: $value")
                        listeners.forEach {
                            it.get()?.onCharacteristicRead?.invoke(gatt.device, this)
                        }
                    }
                    BluetoothGatt.GATT_READ_NOT_PERMITTED -> {
                        Log.e("READ ERROR", "Read not permitted for $uuid!")
                    }
                    else -> {
                        Log.e("READ ERROR", "Characteristic read failed for $uuid, error: $status")
                    }
                }
            }

            if (pendingOperation is CharacteristicRead) {
                signalEndOfOperation()
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            with(characteristic) {
                Log.i("onCharacteristicChanged", "Characteristic $uuid changed | value: $value")
                listeners.forEach { it.get()?.onCharacteristicChanged?.invoke(gatt.device, this) }
            }
        }

        override fun onDescriptorRead(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int
        ) {
            with(descriptor) {
                when (status) {
                    BluetoothGatt.GATT_SUCCESS -> {
                        Log.i("onDescriptorRead", "Read descriptor $uuid | value: $value")
                        listeners.forEach { it.get()?.onDescriptorRead?.invoke(gatt.device, this) }
                    }
                    BluetoothGatt.GATT_READ_NOT_PERMITTED -> {
                        Log.e("onDescriptorRead", "Read not permitted for $uuid!")
                    }
                    else -> {
                        Log.e(
                            "onDescriptorRead",
                            "Descriptor read failed for $uuid, error: $status"
                        )
                    }
                }
            }

            if (pendingOperation is DescriptorRead) {
                signalEndOfOperation()
            }
        }
    }


/*  private fun readData(characteristic: BluetoothGattCharacteristic) {
      if (uuid.equals(characteristic.uuid)) {
          val data = characteristic.value.toString()
          Log.w("READED DATA", "DER WERT $data")
      }
  }*/

    private fun BluetoothGatt.findCharacteristic(uuid: UUID): BluetoothGattCharacteristic? {
        services?.forEach { service ->
            service.characteristics?.firstOrNull { characteristic ->
                characteristic.uuid == uuid
            }?.let { matchingCharacteristic ->
                return matchingCharacteristic
            }
        }
        return null
    }

    private fun BluetoothGatt.findDescriptor(uuid: UUID): BluetoothGattDescriptor? {
        services.forEach { service ->
            service.characteristics.forEach { characteristic ->
                characteristic.descriptors?.firstOrNull { descriptor ->
                    descriptor.uuid == uuid
                }?.let { matchingDescriptor ->
                    return matchingDescriptor
                }
            }
        }
        return null
    }

    private fun BluetoothGatt.printGattTable() {
        if (services.isEmpty()) {
            Log.i(
                "printGattTable",
                "No service and characteristic available, call discoverServices() first?"
            )
            return
        }

        services.forEach { service ->
            val characteristicTable = service.characteristics.joinToString(
                separator = "\n|--",
                prefix = "|--"
            ) { it.uuid.toString() }
            Log.i(
                "printGattTable",
                "\nServices ${service.uuid}\nCharacteristics:\n$characteristicTable"
            )
        }
    }

    //BluetoothGattCharacteristic
     fun BluetoothGattCharacteristic.containsProperty(property: Int): Boolean {
        return properties and property != 0
    }

     fun BluetoothGattCharacteristic.isReadable(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_READ)

    fun BluetoothGattCharacteristic.isNotifiable(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_NOTIFY)

    fun BluetoothGattCharacteristic.isIndicatable(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_INDICATE)

     fun BluetoothGattCharacteristic.isWritable(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE)

     fun BluetoothGattCharacteristic.isWriteableWithoutResponse(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)

     fun BluetoothGattDescriptor.isReadable(): Boolean =
        containsPermission(BluetoothGattDescriptor.PERMISSION_READ)

     fun BluetoothGattDescriptor.isWritable(): Boolean =
        containsPermission(BluetoothGattDescriptor.PERMISSION_WRITE)

     fun BluetoothGattDescriptor.containsPermission(permission: Int): Boolean =
        permissions and permission != 0

     fun BluetoothDevice.isConnected() = deviceGattMap.containsKey(this)
}