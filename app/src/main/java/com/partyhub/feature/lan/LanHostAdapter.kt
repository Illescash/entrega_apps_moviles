package com.partyhub.feature.lan

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.partyhub.R
import com.partyhub.core.network.HostInfo

/**
 * Adapter para la lista de salas LAN descubiertas.
 */
class LanHostAdapter(
    private var hosts: List<HostInfo>,
    private val onJoinClick: (HostInfo) -> Unit
) : RecyclerView.Adapter<LanHostAdapter.HostViewHolder>() {

    class HostViewHolder(
        parent: ViewGroup
    ) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.list_item_lan_host, parent, false)
    ) {
        val tvHostName: TextView = itemView.findViewById(R.id.tvHostName)
        val tvHostIp: TextView = itemView.findViewById(R.id.tvHostIp)
        val btnJoin: Button = itemView.findViewById(R.id.btnJoin)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HostViewHolder {
        return HostViewHolder(parent)
    }

    override fun onBindViewHolder(holder: HostViewHolder, position: Int) {
        val host = hosts[position]
        holder.tvHostName.text = holder.itemView.context.getString(R.string.lan_room_of, host.hostName)
        holder.tvHostIp.text = host.ip
        holder.btnJoin.setOnClickListener { onJoinClick(host) }
    }

    override fun getItemCount(): Int = hosts.size

    fun updateData(newHosts: List<HostInfo>) {
        hosts = newHosts
        notifyDataSetChanged()
    }
}
