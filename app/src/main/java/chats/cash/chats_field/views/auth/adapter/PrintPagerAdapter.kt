package chats.cash.chats_field.views.auth.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import chats.cash.chats_field.R
import kotlinx.android.synthetic.main.print_pager_view.view.*


/*
Created by
Oshodin Osemwingie

on 17/07/2020.
*/
class PrintPagerAdapter(val context : Context) :
    ListAdapter<PrintPager, PrintPagerAdapter.MyViewHolder>(PrintPagerDiffCallback()) {

    class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SetTextI18n")
        fun bind(
            context: Context,
            item: PrintPager
        ) {
            if(item.bitmap == null){
                itemView.registerImageView.setImageDrawable(context.resources.getDrawable(item.image))
            }else{
                itemView.registerImageView.setImageBitmap(item.bitmap)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return from(parent)
    }


    private fun from(parent: ViewGroup) : MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.print_pager_view,parent,false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(context, item)
    }
}
class PrintPagerDiffCallback : DiffUtil.ItemCallback<PrintPager>(){
    override fun areItemsTheSame(oldItem: PrintPager, newItem: PrintPager): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: PrintPager, newItem: PrintPager): Boolean {
        return oldItem.image == newItem.image
    }
}

data class PrintPager(
    val image : Int,
    val bitmap : Bitmap?
)
