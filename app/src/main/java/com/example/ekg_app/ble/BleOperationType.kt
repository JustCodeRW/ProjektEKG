package com.example.ekg_app.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import java.util.*

/*this class implements different data classes
* that are used in the BleConnectionManager and are abilities
* of the ESP32
*/
sealed class BleOperationType {
    abstract val device: BluetoothDevice
}

data class Connect(override val device: BluetoothDevice, val context: Context) : BleOperationType()

data class DisConnect(override val device: BluetoothDevice) : BleOperationType()

data class CharacteristicRead(override val device: BluetoothDevice, val characteristicUuid: UUID) : BleOperationType()

data class EnableNotifications(override val device: BluetoothDevice, val characteristicUuid: UUID) : BleOperationType()

data class DescriptorRead(override val device: BluetoothDevice, val descriptorUuid: UUID) : BleOperationType()