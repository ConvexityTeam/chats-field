package chats.cash.chats_field.views.auth.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.ItemCashForWorkBeneficiaryBinding
import chats.cash.chats_field.views.cashForWork.model.AssignedWorker

/*
Created by
Oshodin Osemwingie

on 17/07/2020.
*/

class WorkerAdapter(
    private val onItemClick: (TaskState) -> Unit,
    private val onAddWorkerClick: (AssignedWorker) -> Unit,
) : ListAdapter<AssignedWorker, WorkerAdapter.MyViewHolder>(WORKER_DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            ItemCashForWorkBeneficiaryBinding.bind(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_cash_for_work_beneficiary, parent, false),
            ),
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
                /*if (worker.taskAssignment.uploadedEvidence.not())
                    onItemClick.invoke(worker)*/
                val taskState: TaskState = when (worker.taskAssignment.status) {
                    "disbursed" -> TaskState.Disbursed
                    "completed" -> TaskState.Completed
                    else -> TaskState.ProgressOrRejected(worker)
                }
                onItemClick.invoke(taskState)
            }
        }
    }

    companion object {
        val WORKER_DIFF_CALLBACK = object : DiffUtil.ItemCallback<AssignedWorker>() {
            override fun areItemsTheSame(oldItem: AssignedWorker, newItem: AssignedWorker): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: AssignedWorker, newItem: AssignedWorker): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }

    sealed class TaskState {
        object Disbursed : TaskState()
        object Completed : TaskState()
        data class ProgressOrRejected(val worker: AssignedWorker) : TaskState()
    }
}
