package com.example.ekg_app

import android.bluetooth.BluetoothDevice
import android.content.Context
import java.util.UUID

sealed class BleOperationType {
    abstract val device: BluetoothDevice
}

data class Connect(override val device: BluetoothDevice, val context: Context) : BleOperationType()

data class DisConnect(override val device: BluetoothDevice) : BleOperationType()

data class CharacteristicRead(override val device: BluetoothDevice, val characteristicUUID: UUID) : BleOperationType()

data class EnableNotifications(override val device: BluetoothDevice, val characteristicUUID: UUID) : BleOperationType()

data class DescriptorRead(override val device: BluetoothDevice, val descriptorUUID: UUID) : BleOperationType()