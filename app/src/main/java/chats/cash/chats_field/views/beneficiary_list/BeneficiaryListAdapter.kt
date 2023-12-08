package chats.cash.chats_field.views.beneficiary_list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.ItemExistingBeneficiaryBinding
import chats.cash.chats_field.utils.toTitleCase

class BeneficiaryListAdapter(private val onSelectClick: (BeneficiaryUi) -> Unit) :
    ListAdapter<BeneficiaryUi, BeneficiaryListAdapter.BeneficiaryListViewHolder>(
        BENEFICIARY_DIFF_UTIL,
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BeneficiaryListViewHolder {
        return BeneficiaryListViewHolder(
            ItemExistingBeneficiaryBinding.bind(
                LayoutInflater.from(
                    parent.context,
                ).inflate(
                    R.layout.item_existing_beneficiary,
                    parent,
                    false,
                ),
            ),
        )
    }

    override fun onBindViewHolder(holder: BeneficiaryListViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BeneficiaryListViewHolder(private val binding: ItemExistingBeneficiaryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(beneficiary: BeneficiaryUi) = with(binding) {
            nameValue.text = String.format(
                "${beneficiary.firstName.toTitleCase()} %s",
                beneficiary.lastName.toTitleCase(),
            )
            emailValue.text = beneficiary.email
            phoneValue.text = beneficiary.phone
            selectButton.apply {
                isVisible = beneficiary.isAdded.not()
                setOnClickListener { onSelectClick.invoke(beneficiary) }
            }
        }
    }

    companion object {
        val BENEFICIARY_DIFF_UTIL = object : DiffUtil.ItemCallback<BeneficiaryUi>() {
            override fun areItemsTheSame(oldItem: BeneficiaryUi, newItem: BeneficiaryUi): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: BeneficiaryUi, newItem: BeneficiaryUi): Boolean {
                return oldItem == newItem
            }
        }
    }
}
