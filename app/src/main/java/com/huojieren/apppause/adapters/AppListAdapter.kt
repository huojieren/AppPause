package com.huojieren.apppause.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.huojieren.apppause.R
import com.huojieren.apppause.models.AppInfo

class AppListAdapter(
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<AppListAdapter.ViewHolder>() {

    private var appList: List<AppInfo> = emptyList()

    private class AppDiffCallback(
        private val oldList: List<AppInfo>,
        private val newList: List<AppInfo>
    ) : DiffUtil.Callback() {

        override fun getOldListSize() = oldList.size
        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean {
            return oldList[oldPos].packageName == newList[newPos].packageName
        }

        override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean {
            return oldList[oldPos] == newList[newPos]
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val appNameTextView: TextView = itemView.findViewById(R.id.appNameTextView)
    }

    fun updateList(newList: List<AppInfo>) {
        val diffResult = DiffUtil.calculateDiff(AppDiffCallback(appList, newList))
        appList = newList
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val appInfo = appList[position]
        holder.appNameTextView.text = appInfo.name
        holder.itemView.setOnClickListener {
            onItemClick(appInfo.packageName)
        }
    }

    override fun getItemCount(): Int {
        return appList.size
    }
}