package chats.cash.chats_field.views.beneficiary_onboarding.campaigns

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.ItemCampaignBinding
import chats.cash.chats_field.model.ModelCampaign

class CampaignAdapter(private val onCampaignClick: (ModelCampaign) -> Unit) :
    ListAdapter<ModelCampaign, CampaignAdapter.CampaignViewHolder>(CAMPAIGN_DIFF_UTIL) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CampaignViewHolder {
        return CampaignViewHolder(
            ItemCampaignBinding.bind(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_campaign, parent, false),
            ),
        )
    }

    override fun onBindViewHolder(holder: CampaignViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CampaignViewHolder(private val binding: ItemCampaignBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(campaign: ModelCampaign) {
            binding.campaignName.text = campaign.title
            binding.root.setOnClickListener { onCampaignClick(campaign) }
        }
    }

    companion object {
        val CAMPAIGN_DIFF_UTIL = object : DiffUtil.ItemCallback<ModelCampaign>() {
            override fun areContentsTheSame(oldItem: ModelCampaign, newItem: ModelCampaign) = false

            override fun areItemsTheSame(oldItem: ModelCampaign, newItem: ModelCampaign) = false
        }
    }
}
