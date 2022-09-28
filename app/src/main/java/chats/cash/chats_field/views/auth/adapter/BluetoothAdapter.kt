package chats.cash.chats_field.views.auth.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import chats.cash.chats_field.R
import chats.cash.chats_field.model.ConnectedDevice
import kotlinx.android.synthetic.main.item_bluetooth_device.view.*


/*
Created by
Oshodin Osemwingie

on 17/07/2020.
*/
class BluetoothDeviceAdapter(val clickListener: BluetoothClickListener) :
    ListAdapter<ConnectedDevice, BluetoothDeviceAdapter.MyViewHolder>(BluetoothDiffCallback()) {

    class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SetTextI18n")
        fun bind(
            item: ConnectedDevice,
            clickListener: BluetoothClickListener
        ) {
            itemView.setOnClickListener {
                clickListener.onClick(item)
            }
            itemView.item_device_name.text = item.deviceName
            itemView.item_mac_address.text = item.deviceAddress
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return from(parent)
    }


    private fun from(parent: ViewGroup) : MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.item_bluetooth_device,parent,false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, clickListener)
    }
}
class BluetoothDiffCallback : DiffUtil.ItemCallback<ConnectedDevice>(){
    override fun areItemsTheSame(oldItem: ConnectedDevice, newItem: ConnectedDevice): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: ConnectedDevice, newItem: ConnectedDevice): Boolean {
        return oldItem == newItem
    }
}

class BluetoothClickListener(val clickListener: (item : ConnectedDevice) -> Unit){
    fun onClick(item : ConnectedDevice) = clickListener(item)
}

