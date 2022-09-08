package com.example.ekg_app

import android.app.AlertDialog
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.ekg_app.ble.BleConnectionManager
import com.example.ekg_app.ble.ConnectionEventListener
import com.example.ekg_app.ble.isNotifiable
import com.example.ekg_app.ble.isReadable
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import java.util.*

/*the recordScreen Activity creates a graph
* with the data  that is transferred
* from the ESP 32
*/
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

    /*this listener is used to get the different
    * states of the ESP32 and start to save the data
    */
    private val connectionEventListener by lazy {
        ConnectionEventListener().apply {
            onDisconnect = {
                val builder = AlertDialog.Builder(this@RecordScreen)
                builder.setMessage("disconnected or can't connect to device")
                    .setPositiveButton("okay") { _, _ ->
                        onBackPressed()
                    }
                val alertDialog = builder.create()
                alertDialog.show()
            }

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

        /*the listener transfers the user back to the
        * mainMenu and stops the connection to the ESP
        */
        val backBtn: ImageButton = findViewById(R.id.backToMainPage)
        backBtn.setOnClickListener {
            val backIntent = Intent(this@RecordScreen, MainMenu::class.java)
            BleConnectionManager.unregisterListener(connectionEventListener)
            BleConnectionManager.tearDownConnection(device)
            startActivity(backIntent)
        }

        BleConnectionManager.registerListener(connectionEventListener)
        device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
            ?: error("Missing BluetoothDevice from Activity")

        //here the Chart/Graph is created
        lineChart = findViewById(R.id.lineChart)
        lineChart.description.isEnabled = false
        lineChart.isDragEnabled = true
        lineChart.setTouchEnabled(true)
        lineChart.setScaleEnabled(true)
        lineChart.setDrawGridBackground(false)
        lineChart.setPinchZoom(true)
        lineChart.setBackgroundColor(Color.WHITE)
        lineChart.axisRight.setDrawLabels(false)
        lineChart.axisLeft.setDrawLabels(false)
        lineChart.xAxis.setDrawLabels(false)
//        lineChart.axisLeft.isEnabled = false
//        lineChart.axisRight.isEnabled = false
        lineChart.legend.isEnabled = false

        val data = LineData()
        data.setValueTextColor(Color.WHITE)

        lineChart.data = data

        startCharacteristicRead(characteristics)
    }

    override fun onBackPressed() {}

    override fun onDestroy() {
        BleConnectionManager.unregisterListener(connectionEventListener)
        BleConnectionManager.tearDownConnection(device)
        super.onDestroy()
    }

    //this method adds the data from the ESP 32 into the chart
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

    /*this method creates the set for the chart
     and some designs for the set (the Chart design behaviour
     can be changed here)
   */
    private fun createSet(): LineDataSet {
        val set = LineDataSet(null, "")
        set.axisDependency = YAxis.AxisDependency.LEFT
        set.lineWidth = 2f
        set.color = Color.RED
        set.fillColor = Color.TRANSPARENT
        set.fillAlpha = 30
        set.isHighlightEnabled = true
        set.setDrawFilled(true)
        set.setDrawValues(true)
        set.setDrawCircles(true)
        set.mode = LineDataSet.Mode.CUBIC_BEZIER
        set.cubicIntensity = 0.1f

        return set
    }

    /*this method starts to read the data form the ESP32
    * there are to ways to read data.
    * the user can read the last input or get all data
    * if you want to read the last input just comment out the
    * BleConnectionManager.readCharacteristic(device, characteristic)
    * and comment in the BleConnectionManager.enableNotifications() */
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