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
import kotlinx.android.synthetic.main.onboarding_item_layout.view.*


/*
Created by
Oshodin Osemwingie

on 17/07/2020.
*/
class OnboardingAdapter(val context : Context) :
    ListAdapter<OnBoarding, OnboardingAdapter.MyViewHolder>(OnBoardingDiffCallback()) {

    class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SetTextI18n")
        fun bind(
            context: Context,
            item: OnBoarding
        ) {
            itemView.onbDesc.text = item.desc
            itemView.onbTitle.text = item.title
            itemView.onboardingImageView.setImageDrawable(context.resources.getDrawable(item.image))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return from(parent)
    }


    private fun from(parent: ViewGroup) : MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.onboarding_item_layout,parent,false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(context, item)
    }
}
class OnBoardingDiffCallback : DiffUtil.ItemCallback<OnBoarding>(){
    override fun areItemsTheSame(oldItem: OnBoarding, newItem: OnBoarding): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: OnBoarding, newItem: OnBoarding): Boolean {
        return oldItem == newItem
    }
}

data class OnBoarding(
    val title : String,
    val desc : String,
    val image : Int
)
