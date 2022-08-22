package com.example.ekg_app

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.graphics.Color
import android.net.wifi.aware.Characteristics
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.example.ekg_app.BleConnectionManager.isNotifiable
import com.example.ekg_app.BleConnectionManager.isReadable
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import java.util.*
import kotlin.collections.ArrayList

class RecordScreen : AppCompatActivity() {
    private lateinit var lineChart: LineChart
    private lateinit var device: BluetoothDevice
    lateinit var lineDataSet: LineDataSet

    private var notifyingCharacteristic = mutableListOf<UUID>()

    private val entries = ArrayList<Entry>()
    private val valuesFloat: ArrayList<Float> = arrayListOf()
    private val realTimeValues: ArrayList<Float> = arrayListOf()

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


//        for (i in 1..50) {
//            startCharacteristicRead(characteristics)
//        }

        startCharacteristicRead(characteristics)

        lineChart = findViewById(R.id.lineChart)
//        entries.add(Entry(1f, 1f))
//        entries.add(Entry(2f, 2f))
//        entries.add(Entry(3f, 7f))
//        entries.add(Entry(4f, 8f))
//        entries.add(Entry(5f, 16f))


/*

        Handler().postDelayed({
            var counter = 1

            for (i in valuesFloat.indices) {
                if (valuesFloat.getOrNull(i + 1) != null && !valuesFloat[i].equals(valuesFloat[i + 1])) {
                    Log.e(
                        "if branch",
                        "value : ${valuesFloat[i]} next value: ${valuesFloat[i + 1]}"
                    )
                    entries.add(Entry((counter * 0.1f), valuesFloat[i]))
                    counter++
                    lineDataSet = LineDataSet(entries, "Test")
                    lineDataSet.setDrawValues(true)
                    lineDataSet.setDrawFilled(true)
                    lineDataSet.lineWidth = 2f
                    lineChart.data = LineData(lineDataSet)
                }
            }
        }, 5000)
*/


//        lineChart.axisRight.isEnabled = false
//        lineChart.axisLeft.isEnabled = false
//        lineChart.xAxis.isEnabled = false

        showChart()
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

    override fun onResume() {
        super.onResume()
       showChart()
    }

    fun showChart() {
        Handler().postDelayed({
            var counter = 1
            for (i in realTimeValues.indices) {
                if (realTimeValues.getOrNull(i + 1) != null && !realTimeValues[i].equals(realTimeValues[i + 1])) {
                    entries.add(Entry(counter * 1f, realTimeValues[i]))
                    lineDataSet = LineDataSet(entries, "RealTime")
                    lineDataSet.setDrawValues(true)
                    lineDataSet.setDrawValues(true)
                    lineDataSet.setDrawFilled(true)
                    lineDataSet.lineWidth = 5f
                    lineChart.data = LineData(lineDataSet)
                    counter++
                }
            }

        }, 2000)
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

    private
    val connectionEventListener by lazy {
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
                val stringValue = String(characteristic.value)
                Log.d(
                    "Characteristic changed ",
                    "Value change on ${characteristic.uuid}: value: $stringValue"
                )

                val realTimeValue = String(characteristic.value).toFloat()
                realTimeValues.add(realTimeValue)
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

    private fun startCharacteristicRead(characteristics: List<BluetoothGattCharacteristic>) {
        for (characteristic in characteristics) {
            if (characteristic.isReadable() && characteristic.isNotifiable()) {
//                BleConnectionManager.readCharacteristic(device, characteristic)
                BleConnectionManager.enableNotifications(device, characteristic)
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