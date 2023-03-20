package chats.cash.chats_field.views.auth.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.OnboardingItemLayoutBinding

/*
Created by
Oshodin Osemwingie

on 17/07/2020.
*/

class OnboardingAdapter :
    ListAdapter<OnBoarding, OnboardingAdapter.MyViewHolder>(OnBoardingDiffCallback()) {

    class MyViewHolder(val binding: OnboardingItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: OnBoarding) = with(itemView) {
            binding.onbDesc.text = item.desc
            binding.onbTitle.text = item.title
            binding.onboardingImageView.setImageDrawable(ResourcesCompat.getDrawable(context.resources,
                item.image,
                context.theme)
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return from(parent)
    }

    private fun from(parent: ViewGroup): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = OnboardingItemLayoutBinding.inflate(layoutInflater,parent,false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }
}

class OnBoardingDiffCallback : DiffUtil.ItemCallback<OnBoarding>() {
    override fun areItemsTheSame(oldItem: OnBoarding, newItem: OnBoarding): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: OnBoarding, newItem: OnBoarding): Boolean {
        return oldItem == newItem
    }
}

data class OnBoarding(
    val title: String,
    val desc: String,
    @DrawableRes val image: Int,
)
