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
import com.codose.chats.network.response.tasks.Task
import com.codose.chats.utils.Utils.toDateTime
import kotlinx.android.synthetic.main.item_cash_for_work_item.view.*

/*
Created by
Oshodin Osemwingie

on 17/07/2020.
*/
class TaskAdapter(val clickListener: TaskClickListener) :
    ListAdapter<Task, TaskAdapter.MyViewHolder>(TaskDiffCallback()) {

    class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SetTextI18n")
        fun bind(
            item: Task,
            clickListener: TaskClickListener
        ) {
            itemView.txt_cash_for_work_title.text = item.name
            itemView.setOnClickListener {
                clickListener.onClick(item)
            }
            itemView.txt_cash_for_work_amount.text = "₦"+ item.amount
            itemView.txt_cash_for_work_created.text = item.createdAt.toDateTime()
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

class TaskClickListener(val clickListener: (type: Task) -> Unit) {
    fun onClick(type: Task) = clickListener(type)
}
class TaskDiffCallback : DiffUtil.ItemCallback<Task>(){
    override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
        return oldItem == newItem
    }
}
