package chats.cash.chats_field.views.auth.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.PrintPagerViewBinding

/*
Created by
Oshodin Osemwingie

on 17/07/2020.
*/
class PrintPagerAdapter(val context: Context) :
    ListAdapter<PrintPager, PrintPagerAdapter.MyViewHolder>(PrintPagerDiffCallback()) {

    class MyViewHolder(val binding: PrintPagerViewBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(
            context: Context,
            item: PrintPager,
        ) {
            if (item.bitmap == null) {
                binding.registerImageView.setImageDrawable(
                    context.resources.getDrawable(item.image),
                )
            } else {
                binding.registerImageView.setImageBitmap(item.bitmap)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return from(parent)
    }

    private fun from(parent: ViewGroup): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.print_pager_view, parent, false)
        val binding = PrintPagerViewBinding.inflate(layoutInflater, parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(context, item)
    }
}
class PrintPagerDiffCallback : DiffUtil.ItemCallback<PrintPager>() {
    override fun areItemsTheSame(oldItem: PrintPager, newItem: PrintPager): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: PrintPager, newItem: PrintPager): Boolean {
        return oldItem.image == newItem.image
    }
}

data class PrintPager(
    val image: Int,
    val bitmap: Bitmap?,
)
