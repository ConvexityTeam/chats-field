package chats.cash.chats_field.views.beneficiary_search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.ItemExistingBeneficiaryBinding

class BeneficiarySearchAdapter(private val onBeneficiaryClick: (BeneficiaryUi) -> Unit) :
    ListAdapter<BeneficiaryUi, BeneficiarySearchAdapter.BeneficiarySearchViewHolder>(
        BENEFICIARY_DIFF_UTIL,
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BeneficiarySearchViewHolder {
        return BeneficiarySearchViewHolder(
            ItemExistingBeneficiaryBinding.bind(
                LayoutInflater.from(
                    parent.context,
                ).inflate(R.layout.item_existing_beneficiary, parent, false),
            ),
        )
    }

    override fun onBindViewHolder(holder: BeneficiarySearchViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BeneficiarySearchViewHolder(private val binding: ItemExistingBeneficiaryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(beneficiary: BeneficiaryUi) = with(binding) {
            nameValue.text = String.format("${beneficiary.firstName} %s", beneficiary.lastName)
            phoneValue.text = beneficiary.phone
            emailValue.text = beneficiary.email
            root.setOnClickListener { onBeneficiaryClick.invoke(beneficiary) }
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
