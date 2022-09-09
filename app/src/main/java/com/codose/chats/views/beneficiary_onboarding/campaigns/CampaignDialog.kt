package com.codose.chats.views.beneficiary_onboarding.campaigns

import android.app.Dialog
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codose.chats.R
import com.codose.chats.databinding.DialogCampaignBinding
import com.codose.chats.model.ModelCampaign
import com.codose.chats.utils.BluetoothConstants.CAMPAIGN_BUNDLE_KEY
import com.codose.chats.utils.BluetoothConstants.FRAGMENT_CAMPAIGN_RESULT_LISTENER
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class CampaignDialog : BottomSheetDialogFragment() {

    private var _binding: DialogCampaignBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModel<CampaignViewModel>()
    private val campaignAdapter by lazy {
        CampaignAdapter(onCampaignClick = {
            setFragmentResult(FRAGMENT_CAMPAIGN_RESULT_LISTENER, bundleOf(CAMPAIGN_BUNDLE_KEY to it))
            dismiss()
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            DialogCampaignBinding.bind(inflater.inflate(R.layout.dialog_campaign, container, false))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCampaignData()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener {
            val bottomSheet =
                dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet.setBackgroundColor(Color.TRANSPARENT)

            val params = bottomSheet.layoutParams
            val height: Int = Resources.getSystem().displayMetrics.heightPixels
            params.height = height
            bottomSheet.layoutParams = params
        }
        return dialog
    }

    private fun setupCampaignData() {
        viewModel.campaigns.observe(viewLifecycleOwner, ::handleCampaignList)
    }

    private fun handleCampaignList(campaigns: List<ModelCampaign>?) = with(binding) {
        campaignList.apply {
            adapter = campaignAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(DividerItemDecoration(requireContext(), RecyclerView.VERTICAL))
        }
        campaignAdapter.submitList(campaigns)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
