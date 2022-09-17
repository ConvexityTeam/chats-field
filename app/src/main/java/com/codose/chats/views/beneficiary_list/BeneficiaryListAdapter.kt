package com.codose.chats.views.beneficiary_list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codose.chats.R
import com.codose.chats.databinding.ItemExistingBeneficiaryBinding
import com.codose.chats.network.response.beneficiary_onboarding.Beneficiary
import com.codose.chats.utils.toTitleCase

class BeneficiaryListAdapter(private val onSelectClick: (Beneficiary) -> Unit) :
    ListAdapter<Beneficiary, BeneficiaryListAdapter.BeneficiaryListViewHolder>(BENEFICIARY_DIFF_UTIL) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BeneficiaryListViewHolder {
        return BeneficiaryListViewHolder(ItemExistingBeneficiaryBinding.bind(LayoutInflater.from(
            parent.context).inflate(
            R.layout.item_existing_beneficiary, parent, false))
        )
    }

    override fun onBindViewHolder(holder: BeneficiaryListViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BeneficiaryListViewHolder(private val binding: ItemExistingBeneficiaryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(beneficiary: Beneficiary) = with(binding) {
            nameValue.text = String.format("${beneficiary.firstName.toTitleCase()} %s",
                beneficiary.lastName.toTitleCase())
            emailValue.text = beneficiary.email
            phoneValue.text = beneficiary.phone
            selectButton.apply {
                isVisible = true
                setOnClickListener { onSelectClick.invoke(beneficiary) }
            }
        }
    }

    companion object {
        val BENEFICIARY_DIFF_UTIL = object : DiffUtil.ItemCallback<Beneficiary>() {
            override fun areItemsTheSame(oldItem: Beneficiary, newItem: Beneficiary): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Beneficiary, newItem: Beneficiary): Boolean {
                return oldItem == newItem
            }
        }
    }
}
