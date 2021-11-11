package com.codose.chats.views.auth.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codose.chats.R
import com.codose.chats.network.response.tasks.details.AssociatedWorker
import com.codose.chats.utils.hide
import kotlinx.android.synthetic.main.item_cash_for_work_item.view.*

/*
Created by
Oshodin Osemwingie

on 17/07/2020.
*/
class WorkerAdapter(val clickListener: WorkerClickListener) :
    ListAdapter<AssociatedWorker, WorkerAdapter.MyViewHolder>(WorkerDiffCallback()) {

    class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SetTextI18n")
        fun bind(
            item: AssociatedWorker,
            clickListener: WorkerClickListener
        ) {
            itemView.txt_cash_for_work_title.text = item.worker.firstName + " " + item.worker.lastName
            itemView.setOnClickListener {
                clickListener.onClick(item)
            }
            itemView.textView20.hide()
            itemView.textView27.hide()

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return from(parent)
    }


    private fun from(parent: ViewGroup) : MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.item_cash_for_work_item,parent,false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, clickListener)
    }
}

class WorkerClickListener(val clickListener: (type: AssociatedWorker) -> Unit) {
    fun onClick(type: AssociatedWorker) = clickListener(type)
}
class WorkerDiffCallback : DiffUtil.ItemCallback<AssociatedWorker>(){
    override fun areItemsTheSame(oldItem: AssociatedWorker, newItem: AssociatedWorker): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: AssociatedWorker, newItem: AssociatedWorker): Boolean {
        return oldItem.worker == newItem.worker
    }
}
