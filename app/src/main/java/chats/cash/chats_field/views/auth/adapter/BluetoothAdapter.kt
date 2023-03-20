package chats.cash.chats_field.views.auth.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.ItemBluetoothDeviceBinding
import chats.cash.chats_field.model.ConnectedDevice


/*
Created by
Oshodin Osemwingie

on 17/07/2020.
*/
class BluetoothDeviceAdapter(val clickListener: BluetoothClickListener) :
    ListAdapter<ConnectedDevice, BluetoothDeviceAdapter.MyViewHolder>(BluetoothDiffCallback()) {

    class MyViewHolder(val binding : ItemBluetoothDeviceBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(
            item: ConnectedDevice,
            clickListener: BluetoothClickListener
        ) {
            binding.root.setOnClickListener {
                clickListener.onClick(item)
            }
            binding.itemDeviceName.text = item.deviceName
            binding.itemMacAddress.text = item.deviceAddress
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return from(parent)
    }


    private fun from(parent: ViewGroup) : MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemBluetoothDeviceBinding.inflate(layoutInflater,parent,false)
        return MyViewHolder(binding)
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

