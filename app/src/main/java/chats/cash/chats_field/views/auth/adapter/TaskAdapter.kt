package chats.cash.chats_field.views.auth.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.ItemCashForWorkItemBinding
import chats.cash.chats_field.utils.Utils.toDateTime
import chats.cash.chats_field.utils.toStatusString
import chats.cash.chats_field.views.cashForWork.model.Job

/*
Created by
Oshodin Osemwingie

on 17/07/2020.
*/

class JobAdapter(private val onReportClick:(Job) -> Unit) :
    ListAdapter<Job, JobAdapter.MyViewHolder>(JobDiffCallback()) {

    inner class MyViewHolder(private val binding : ItemCashForWorkItemBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(item: Job) = with(binding) {
            txtCashForWorkTitle.text = item.name
            txtCashForWorkAmount.text = "₦"+ item.amount
            txtCashForWorkCreated.text = item.createdAt.toDateTime()
            completionValue.text = item.isCompleted.toStatusString()
            cashForWorkButtons.isGone = true
            reportButton.setOnClickListener { onReportClick.invoke(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(ItemCashForWorkItemBinding.bind(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cash_for_work_item, parent, false))
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        class JobDiffCallback : DiffUtil.ItemCallback<Job>(){
            override fun areItemsTheSame(oldItem: Job, newItem: Job): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Job, newItem: Job): Boolean {
                return oldItem == newItem
            }
        }
    }
}
