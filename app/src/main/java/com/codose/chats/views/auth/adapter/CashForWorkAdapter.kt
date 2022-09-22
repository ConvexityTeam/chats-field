package com.codose.chats.views.auth.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codose.chats.R
import com.codose.chats.databinding.ItemCashForWorkItemBinding
import com.codose.chats.model.ModelCampaign
import com.codose.chats.utils.Utils.toDateTime
import com.codose.chats.views.cashForWork.model.Job

/*
Created by
Oshodin Osemwingie

on 17/07/2020.
*/

class CashForWorkAdapter(
    private val onLoadTaskClick: (List<Job>) -> Unit,
    private val onBeneficiaryClick: (Int) -> Unit
) : ListAdapter<ModelCampaign, CashForWorkAdapter.MyViewHolder>(CashForWorkDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(ItemCashForWorkItemBinding.bind(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cash_for_work_item, parent, false))
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MyViewHolder(private val binding: ItemCashForWorkItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ModelCampaign) = with(binding) {
            txtCashForWorkTitle.text = item.title
            txtCashForWorkAmount.text = String.format("₦%s", item.budget)
            txtCashForWorkCreated.text = item.createdAt?.toDateTime()
            loadTasksButton.setOnClickListener { onLoadTaskClick.invoke(item.jobs) }
            beneficiariesButton.setOnClickListener { onBeneficiaryClick(item.id) }
            completionGroup.isGone = true
            reportButton.isGone = true
        }
    }

    companion object {
        class CashForWorkDiffCallback : DiffUtil.ItemCallback<ModelCampaign>() {
            override fun areItemsTheSame(oldItem: ModelCampaign, newItem: ModelCampaign): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: ModelCampaign,
                newItem: ModelCampaign,
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}
