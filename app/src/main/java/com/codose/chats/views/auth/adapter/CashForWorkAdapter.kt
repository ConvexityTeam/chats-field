package com.codose.chats.views.auth.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.model.ModelCache
import com.codose.chats.R
import com.codose.chats.model.ModelCampaign
import com.codose.chats.network.response.campaign.CashForWork
import com.codose.chats.network.response.tasks.Task
import com.codose.chats.utils.Utils.toDateTime
import kotlinx.android.synthetic.main.item_cash_for_work_item.view.*
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter


/*
Created by
Oshodin Osemwingie

on 17/07/2020.
*/
class CashForWorkAdapter(val clickListener: CashForWorkClickListener) :
    ListAdapter<ModelCampaign, CashForWorkAdapter.MyViewHolder>(CashForWorkDiffCallback()) {

    class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SetTextI18n")
        fun bind(
            clickListener: CashForWorkClickListener,
            item: ModelCampaign,
        ) {
            itemView.txt_cash_for_work_title.text = item.title
            itemView.setOnClickListener {
                clickListener.onClick(item)
            }
            itemView.txt_cash_for_work_amount.text = "₦"+ item.budget
            itemView.txt_cash_for_work_created.text = item.createdAt!!.toDateTime()

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
        holder.bind(clickListener, item)
    }
}
class CashForWorkClickListener(val clickListener: (type: ModelCampaign) -> Unit) {
    fun onClick(type: ModelCampaign) = clickListener(type)
}
class CashForWorkDiffCallback : DiffUtil.ItemCallback<ModelCampaign>(){
    override fun areItemsTheSame(oldItem: ModelCampaign, newItem: ModelCampaign): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: ModelCampaign, newItem: ModelCampaign): Boolean {
        return oldItem == newItem
    }
}
