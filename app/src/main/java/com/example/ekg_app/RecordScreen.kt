package com.example.ekg_app

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.ekg_app.BleConnectionManager.isNotifiable
import com.example.ekg_app.BleConnectionManager.isReadable
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import java.util.*

class RecordScreen : AppCompatActivity() {
    private lateinit var device: BluetoothDevice
    private lateinit var lineChart: LineChart

    private var notifyingCharacteristic = mutableListOf<UUID>()
    private val realTimeValues: ArrayList<Float> = arrayListOf()

    private val characteristics by lazy {
        BleConnectionManager.serviceOnDevice(device)?.flatMap { service ->
            service.characteristics ?: listOf()
        } ?: listOf()
    }

    private val connectionEventListener by lazy {
        ConnectionEventListener().apply {
            /*onDisconnect = {
                runOnUiThread {
                    alert {
                        title = "Disconnected"
                        message = "Disconnected from device."
                        positiveButton("OK") { onBackPressed() }
                    }.show()
                }
            }*/

            onCharacteristicRead = { _, characteristic ->
                val stringValue = String(characteristic.value)
                Log.d(
                    "Characteristic read ",
                    "Read from ${characteristic.uuid}: value: $stringValue"
                )
            }

            onCharacteristicChanged = { _, characteristic ->
                val stringValue = String(characteristic.value)
                Log.d(
                    "Characteristic changed ",
                    "Value change on ${characteristic.uuid}: value: $stringValue"
                )

                val realTimeValue = String(characteristic.value).toFloat()
                realTimeValues.add(realTimeValue)

                addEntry(realTimeValue)
            }

            onNotificationsEnabled = { _, characteristic ->
                Log.d(
                    "Enabled notifications",
                    "Notification is on ${characteristic.uuid}"
                )
                notifyingCharacteristic.add(characteristic.uuid)
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_screen)

        BleConnectionManager.registerListener(connectionEventListener)
        device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
            ?: error("Missing BluetoothDevice from Activity")

        lineChart = findViewById(R.id.lineChart)
        lineChart.description.isEnabled = false
        lineChart.isDragEnabled = true
        lineChart.setTouchEnabled(true)
        lineChart.setScaleEnabled(true)
        lineChart.setDrawGridBackground(false)
        lineChart.setPinchZoom(true)
        lineChart.setBackgroundColor(Color.WHITE)

        val data = LineData()
        data.setValueTextColor(Color.WHITE)

        lineChart.data = data

        startCharacteristicRead(characteristics)
    }

    override fun onDestroy() {
        BleConnectionManager.unregisterListener(connectionEventListener)
        BleConnectionManager.tearDownConnection(device)
        super.onDestroy()
    }

    private fun addEntry(value: Float) {
        val data = lineChart.data

        if (data != null) {
            var set: ILineDataSet? = data.getDataSetByIndex(0)

            if (set == null) {
                set = createSet()
                data.addDataSet(set)
            }

            data.addEntry(Entry(set.entryCount.toFloat(), value), 0)
            data.notifyDataChanged()

            lineChart.notifyDataSetChanged()
            lineChart.setVisibleXRangeMaximum(150f)
            lineChart.moveViewToX(data.entryCount.toFloat())
        }
    }

    private fun createSet(): LineDataSet {
        val set = LineDataSet(null, "")
        set.axisDependency = YAxis.AxisDependency.LEFT
        set.lineWidth = 2f
        set.color = Color.BLACK
        set.isHighlightEnabled = false
        set.setDrawValues(false)
        set.setDrawCircles(false)
        set.mode = LineDataSet.Mode.CUBIC_BEZIER
        set.cubicIntensity = 0.1f

        return set
    }

    private fun startCharacteristicRead(characteristics: List<BluetoothGattCharacteristic>) {
        for (characteristic in characteristics) {
            if (characteristic.isReadable() && characteristic.isNotifiable()) {
                BleConnectionManager.enableNotifications(device, characteristic)
//                read only one value from characteristic
//                BleConnectionManager.readCharacteristic(device, characteristic)
            }
        }
    }
}