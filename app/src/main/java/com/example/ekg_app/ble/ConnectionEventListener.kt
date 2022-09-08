package com.example.ekg_app.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor

/*the ConnectionEventListener class has different
* states of the ESP32 or other devices
*/
class ConnectionEventListener {
    var onConnectionSetupComplete: ((BluetoothGatt) -> Unit)? = null
    var onDisconnect: ((BluetoothDevice) -> Unit)? = null
    var onCharacteristicRead: ((BluetoothDevice, BluetoothGattCharacteristic) -> Unit)? = null
    var onDescriptorRead: ((BluetoothDevice, BluetoothGattDescriptor) -> Unit)? = null
    var onCharacteristicChanged: ((BluetoothDevice, BluetoothGattCharacteristic) -> Unit)? = null
    var onNotificationsEnabled: ((BluetoothDevice, BluetoothGattCharacteristic) -> Unit)? = null
}
