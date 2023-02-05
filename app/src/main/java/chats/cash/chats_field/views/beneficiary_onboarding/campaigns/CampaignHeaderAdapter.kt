package chats.cash.chats_field.views.beneficiary_onboarding.campaigns

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.ItemCampaignHeaderBinding

class CampaignHeaderAdapter(private val title: String) :
    RecyclerView.Adapter<CampaignHeaderAdapter.CampaignHeaderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CampaignHeaderViewHolder {
        return CampaignHeaderViewHolder(ItemCampaignHeaderBinding.bind(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_campaign_header, parent, false)))
    }

    override fun onBindViewHolder(holder: CampaignHeaderViewHolder, position: Int) {
        holder.bind(title)
    }

    override fun getItemCount(): Int {
        return 1
    }

    inner class CampaignHeaderViewHolder(private val binding: ItemCampaignHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(title: String) {
            binding.textView18.text = title
        }
    }
}
