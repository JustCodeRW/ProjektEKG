package com.example.ekg_app

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException

class RecordScreen : AppCompatActivity() {
    private lateinit var lineChart : LineChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_screen)
        lineChart = findViewById(R.id.lineChart)

        val entries = ArrayList<Entry>()
        entries.add(Entry(1f, 10f))
        entries.add(Entry(2f, 2f))
        entries.add(Entry(3f, 7f))
        entries.add(Entry(4f, 20f))
        entries.add(Entry(5f, 16f))

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

    private fun readCsvFile() {
        var fileReader : BufferedReader? = null

        try {
            val fileData = ArrayList<Float>()

            fileReader = BufferedReader(FileReader("eigenekg.csv"))

            fileReader.readLine()

            var line : String? = fileReader.readLine()
            while (line != null) {
                val tokens = line.split(",")

            }

        }catch (e : IOException) {
            e.printStackTrace()
        }
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


}