package com.example.ekg_app.ble

import android.annotation.SuppressLint
import android.bluetooth.le.ScanResult
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ekg_app.R

//this class shows the devices that are founded to the user
class ScanResultsAdapter(
    private val dataSet: List<ScanResult>,
    private val onClickListener: ((device: ScanResult) -> Unit)
) : RecyclerView.Adapter<ScanResultsAdapter.ScanResultsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanResultsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_results_ble_scan, parent, false)
        return ScanResultsViewHolder(view, onClickListener)
    }


    override fun onBindViewHolder(holder: ScanResultsViewHolder, position: Int) {
        holder.bind(dataSet[position])
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    class ScanResultsViewHolder(
        private val view: View,
        private val onClickListener: ((device: ScanResult) -> Unit)
    ) : RecyclerView.ViewHolder(view) {

        @SuppressLint("MissingPermission")
        fun bind(result: ScanResult) {
            val dName: TextView = view.findViewById(R.id.device_name)
            dName.text = result.device.name ?: "Unnamed"
            view.setOnClickListener { onClickListener.invoke(result) }
        }
    }
}