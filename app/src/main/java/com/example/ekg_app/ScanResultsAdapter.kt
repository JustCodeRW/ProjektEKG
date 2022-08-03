package com.example.ekg_app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ScanResultsAdapter(private val dataSet: Array<String>) : RecyclerView.Adapter<ScanResultsAdapter.ScanResultsViewHolder>(){

    class ScanResultsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView

        init {
            textView = view.findViewById(R.id.textView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanResultsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.results_ble_scan, parent, false)
        return ScanResultsViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScanResultsViewHolder, position: Int) {
        holder.textView.text = dataSet[position]
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }
}