package com.huojieren.apppause.adapters

import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.huojieren.apppause.R

class MonitoredAppsAdapter(
    private val monitoredApps: List<String>,
    private val onRemoveClick: (String) -> Unit
) : RecyclerView.Adapter<MonitoredAppsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val appNameTextView: TextView = itemView.findViewById(R.id.appNameTextView)
        val removeButton: Button = itemView.findViewById(R.id.removeButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_monitored_app, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val packageName = monitoredApps[position]
        val packageManager = holder.itemView.context.packageManager
        try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            val appName = packageManager.getApplicationLabel(appInfo).toString()
            holder.appNameTextView.text = appName
        } catch (e: PackageManager.NameNotFoundException) {
            holder.appNameTextView.text = packageName // 如果获取失败，显示包名
        }
        holder.removeButton.setOnClickListener {
            onRemoveClick(packageName)
        }
    }

    override fun getItemCount(): Int {
        return monitoredApps.size
    }
}