package com.example.ekg_app

import android.annotation.SuppressLint
import android.bluetooth.le.ScanResult
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class ScanResultsAdapter(
    private val dataSet: List<ScanResult>,
    private val onClickListener: ((device: ScanResult) -> Unit)
) : RecyclerView.Adapter<ScanResultsAdapter.ScanResultsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanResultsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_results_ble_scan, parent, false)
//        val inflater = LayoutInflater.from(parent.context)
//        val binding = ResultsBleScanBinding.inflate(inflater)

        return ScanResultsViewHolder(view, onClickListener)
    }


    override fun onBindViewHolder(holder: ScanResultsViewHolder, position: Int) {
        holder.bind(dataSet[position])
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

/*    class ScanResultsViewHolder(
        private val binding: ResultsBleScanBinding,
        private val onClickListener: (device: ScanResult) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("MissingPermission")
        fun bind(result: ScanResult) {
            binding.deviceName.text = result.device.name ?: "Unnamed"
            binding.macAddress.text = result.device.address
            binding.signalStrength.text = "${result.rssi} dBm"
            binding.root.setOnClickListener { onClickListener.invoke(result) }
        }
    }*/


    class ScanResultsViewHolder(
        private val view: View,
        private val onClickListener: (device: ScanResult) -> Unit
    ) : RecyclerView.ViewHolder(view) {

        @SuppressLint("MissingPermission")
        fun bind(result: ScanResult) {
            val dName : TextView = view.findViewById(R.id.device_name)
            dName.text  = result.device.name ?: "Unnamed"
//            binding.macAddress.text = result.device.address
//            binding.signalStrength.text = "${result.rssi} dBm"
//            binding.root.setOnClickListener { onClickListener.invoke(result) }
        }
    }
}