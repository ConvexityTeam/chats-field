package chats.cash.chats_field.views.beneficiary_onboarding.campaigns

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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.DialogCampaignBinding
import chats.cash.chats_field.model.ModelCampaign
import chats.cash.chats_field.utils.ChatsFieldConstants.CAMPAIGN_BUNDLE_KEY
import chats.cash.chats_field.utils.ChatsFieldConstants.FRAGMENT_CAMPAIGN_RESULT_LISTENER
import chats.cash.chats_field.views.auth.viewmodel.RegisterViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class CampaignDialog : BottomSheetDialogFragment() {

    private var _binding: DialogCampaignBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModel<CampaignViewModel>()
    private val registerViewModel by activityViewModel<RegisterViewModel>()

    private val campaignAdapter by lazy {
        CampaignAdapter(onCampaignClick = {
            setFragmentResult(
                FRAGMENT_CAMPAIGN_RESULT_LISTENER,
                bundleOf(CAMPAIGN_BUNDLE_KEY to it),
            )
            dismiss()
        })
    }

    private val cashForWorkAdapter by lazy {
        CampaignAdapter(onCampaignClick = {
            setFragmentResult(
                FRAGMENT_CAMPAIGN_RESULT_LISTENER,
                bundleOf(CAMPAIGN_BUNDLE_KEY to it),
            )
            dismiss()
        })
    }
    private val concatAdapter: ConcatAdapter by lazy { ConcatAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
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
                dialog.findViewById<FrameLayout>(
                    com.google.android.material.R.id.design_bottom_sheet,
                )
            bottomSheet.setBackgroundColor(Color.TRANSPARENT)

            val params = bottomSheet.layoutParams
            val height: Int = Resources.getSystem().displayMetrics.heightPixels
            params.height = height
            bottomSheet.layoutParams = params
        }
        return dialog
    }

    private fun setupCampaignData() {
        binding.campaignList.apply {
            adapter = concatAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(DividerItemDecoration(requireContext(), RecyclerView.VERTICAL))
        }
        viewModel.campaigns.observe(viewLifecycleOwner, ::handleCampaignList)
        viewModel.cashForWorkCampaigns.observe(viewLifecycleOwner, ::handleCashForWorkList)
    }

    private fun handleCampaignList(campaigns: List<ModelCampaign>?) = with(binding) {
        if (campaigns.isNullOrEmpty()) {
            lifecycleScope.launch {
                registerViewModel.getAllCampaigns2()
            }
        }
        campaignAdapter.submitList(campaigns)
        if (campaigns.isNullOrEmpty().not()) {
            concatAdapter.addAdapter(CampaignHeaderAdapter("Select a Campaign"))
            concatAdapter.addAdapter(campaignAdapter)
        }
    }

    private fun handleCashForWorkList(campaigns: List<ModelCampaign>?) = with(binding) {
        cashForWorkAdapter.submitList(campaigns)
        if (campaigns.isNullOrEmpty().not()) {
            concatAdapter.addAdapter(CampaignHeaderAdapter("Select Cash-for-Work"))
            concatAdapter.addAdapter(cashForWorkAdapter)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
