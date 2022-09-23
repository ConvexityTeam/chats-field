package com.codose.chats.views.auth.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codose.chats.R
import com.codose.chats.databinding.ItemCashForWorkBeneficiaryBinding
import com.codose.chats.views.cashForWork.model.AssignedWorker

/*
Created by
Oshodin Osemwingie

on 17/07/2020.
*/

class WorkerAdapter(
    private val onItemClick: (AssignedWorker) -> Unit,
    private val onAddWorkerClick: (AssignedWorker) -> Unit
) :
    ListAdapter<AssignedWorker, WorkerAdapter.MyViewHolder>(WorkerDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(ItemCashForWorkBeneficiaryBinding.bind(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cash_for_work_beneficiary, parent, false))
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MyViewHolder(private val binding: ItemCashForWorkBeneficiaryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(worker: AssignedWorker) = with(binding) {
            selectButton.setOnClickListener { onAddWorkerClick.invoke(worker) }
            nameValue.text = String.format("${worker.firstName} %s", worker.lastName)
            emailValue.text = worker.email
            phoneValue.text = worker.phone
            root.setOnClickListener {
                if (worker.taskAssignment.uploadedEvidence.not())
                    onItemClick.invoke(worker)
            }
        }
    }
}

class WorkerDiffCallback : DiffUtil.ItemCallback<AssignedWorker>() {
    override fun areItemsTheSame(oldItem: AssignedWorker, newItem: AssignedWorker): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: AssignedWorker, newItem: AssignedWorker): Boolean {
        return oldItem.id == newItem.id
    }
}
