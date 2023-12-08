package chats.cash.chats_field.views.campaign.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.ItemSelectCampaignsBinding
import chats.cash.chats_field.model.ModelCampaign

class SelectCampaignAdapter(
    private val context: Context,
    val onSelected: (ModelCampaign) -> Unit,
) : RecyclerView.Adapter<SelectCampaignAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            ItemSelectCampaignsBinding.bind(
                LayoutInflater.from(parent.context)
                    .inflate(
                        R.layout.item_select_campaigns,
                        parent,
                        false,
                    ),
            ),
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class MyViewHolder(private val binding: ItemSelectCampaignsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ModelCampaign) {
            binding.campaignName.text = item.title
            binding.campaignCard.setOnClickListener {
                onSelected(item)
            }
            binding.beneficiaryCount.text = context.getString(R.string._284_beneficiaries, item.Beneficiaries?.size?.toString() ?: "0")
        }
    }

    private var items = listOf<ModelCampaign>()

    fun updateData(newItems: List<ModelCampaign>) {
        items = newItems.toMutableList()
        val diffUtil = DiffUtilCallback(items, newItems)
        val res = DiffUtil.calculateDiff(diffUtil)
        res.dispatchUpdatesTo(this)
    }

    fun getItems(): List<ModelCampaign> {
        return items
    }

    fun setItems(campaigns: List<ModelCampaign>?) {
        items = campaigns?.toMutableList() ?: emptyList()
        notifyDataSetChanged()
    }

    inner class DiffUtilCallback(
        private val oldList: List<ModelCampaign>,
        private val newList: List<ModelCampaign>,
    ) : DiffUtil.Callback() {

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition] ||
                oldList[oldItemPosition] === newList[newItemPosition]
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }
    }
}
