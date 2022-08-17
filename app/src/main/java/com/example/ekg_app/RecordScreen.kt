package com.example.ekg_app

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.graphics.Color
import android.net.wifi.aware.Characteristics
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.ekg_app.BleConnectionManager.isNotifiable
import com.example.ekg_app.BleConnectionManager.isReadable
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.utils.ColorTemplate

class RecordScreen : AppCompatActivity() {
    private lateinit var lineChart: LineChart
    private lateinit var device: BluetoothDevice
    private var values: ArrayList<String> = arrayListOf()
    private val valuesFloat: ArrayList<Float> = arrayListOf()

    private val characteristics by lazy {
        BleConnectionManager.serviceOnDevice(device)?.flatMap { service ->
            service.characteristics ?: listOf()
        } ?: listOf()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_screen)

        BleConnectionManager.registerListener(connectionEventListener)
        device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
            ?: error("Missing BluetoothDevice from Activity")


        for (i in 1..20) {
            startCharacteristicRead(characteristics)
        }


        lineChart = findViewById(R.id.lineChart)

        val entries = ArrayList<Entry>()
//        entries.add(Entry(10f, 20f))
//        entries.add(Entry(2f, 2f))
//        entries.add(Entry(3f, 7f))
//        entries.add(Entry(4f, 20f))
//        entries.add(Entry(5f, 16f))
        var counter = 1
        for (value in valuesFloat) {
            Log.e("Float values", "Hier sind die Werte $value")
            entries.add(Entry((counter*10f), value*10))
            counter++
        }

        val lineDataSet = LineDataSet(entries, "Test")
        lineDataSet.setDrawValues(true)
        lineDataSet.setDrawFilled(true)
        lineDataSet.lineWidth = 2f


        lineChart.data = LineData(lineDataSet)

//        lineChart.axisRight.isEnabled = false
//        lineChart.axisLeft.isEnabled = false
//        lineChart.xAxis.isEnabled = false

        lineChart.axisLeft.setDrawLabels(false)
        lineChart.axisRight.setDrawLabels(false)
        lineChart.xAxis.setDrawLabels(false)

        lineChart.xAxis.setLabelCount(20, true)

        lineChart.description.isEnabled = false
        lineChart.legend.isEnabled = false

        lineChart.setTouchEnabled(true)
        lineChart.setPinchZoom(true)

        lineChart.animateXY(3000, 3000)
        lineChart.invalidate()

//        setLineChartData()
    }

    override fun onDestroy() {
        BleConnectionManager.unregisterListener(connectionEventListener)
        BleConnectionManager.tearDownConnection(device)
        super.onDestroy()
    }

    private fun setLineChartData() {
        val xValue = ArrayList<String>()
        xValue.add("11.00 AM")
        xValue.add("12.00 AM")
        xValue.add("13.00 AM")
        xValue.add("14.00 AM")

        val lineEntry = ArrayList<Entry>()
        lineEntry.add(Entry(20f, 0F))
        lineEntry.add(Entry(50f, 1f))
        lineEntry.add(Entry(60f, 2f))
        lineEntry.add(Entry(70f, 3f))


        val linedataSet = LineDataSet(lineEntry, "First")
        linedataSet.color = ColorTemplate.getHoloBlue()

        lineChart.data = LineData(linedataSet)
        lineChart.setBackgroundColor(Color.LTGRAY)
        lineChart.animateXY(3000, 3000)

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
                val floatValue = stringValue.toFloat()

                valuesFloat.add(floatValue)
            }

            onCharacteristicChanged = { _, characteristic ->
                val string2 = String(characteristic.value)
                Log.d(
                    "Characteristic changed ",
                    "Value change on ${characteristic.uuid}: value: $string2"
                )
            }
        }
    }

    private fun startCharacteristicRead(characteristics: List<BluetoothGattCharacteristic>) {
        for (characteristic in characteristics) {
            if (characteristic.isReadable() && characteristic.isNotifiable()) {
                BleConnectionManager.readCharacteristic(device, characteristic)
//                BleConnectionManager.enableNotifications(device, characteristic)
            }
        }
    }

    private enum class CharacteristicProperty() {
        Readable,
        Notifiable;

        val action
            get() = when (this) {
                Readable -> "Read"
                Notifiable -> "Toggle Notifications"
            }
    }
}