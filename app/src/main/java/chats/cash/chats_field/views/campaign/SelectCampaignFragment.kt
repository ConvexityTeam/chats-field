package chats.cash.chats_field.views.campaign

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.SelectCampaignFragmentBinding
import chats.cash.chats_field.offline.OfflineViewModel
import chats.cash.chats_field.utils.hide
import chats.cash.chats_field.utils.safeNavigate
import chats.cash.chats_field.utils.show
import chats.cash.chats_field.views.auth.viewmodel.RegisterViewModel
import chats.cash.chats_field.views.campaign.adapter.SelectCampaignAdapter
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import timber.log.Timber

class SelectCampaignFragment :
    Fragment(R.layout.select_campaign_fragment),
    AdapterView.OnItemSelectedListener,
    AdapterView.OnItemClickListener {

    private var _binding: SelectCampaignFragmentBinding? = null
    private val registerViewModel by activityViewModel<RegisterViewModel>()
    private val offlineViewModel by activityViewModel<OfflineViewModel>()
    private val binding get() = _binding!!

    val adapter by lazy {
        SelectCampaignAdapter(requireContext()) {
            setFragmentResult(SELECT_CAMPAIGN_KEY, bundleOf(SELECT_CAMPAIGN_BUNDLE_KEY to it))
            registerViewModel.campaign = it
            if (args.isOnboardBeneficiary) {
                findNavController().safeNavigate(R.id.action_selectCampaignFragment_to_dataConsentFragment)
            } else {
                findNavController().safeNavigate(R.id.action_selectCampaignFragment_to_impactReportUploadFragment)
            }
        }
    }

    val args: SelectCampaignFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = SelectCampaignFragmentBinding.bind(view)
        registerViewModel.campaign = null
        setUpClickListeners()
        lifecycleScope.launch {
            registerViewModel.getAllCampaigns2()
        }
    }

    private fun setUpClickListeners() {
        val dropdown: AutoCompleteTextView = binding.campaignDropdown
        val _adapter: ArrayAdapter<String> = ArrayAdapter(
            requireContext(),
            R.layout.item_beneficiary_group,
            list,
        )
        dropdown.setSelection(0)
        dropdown.listSelection = 0
        dropdown.setAdapter(_adapter)
        dropdown.onItemSelectedListener = this
        dropdown.onItemClickListener = this

        binding.selectCampaignAppbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.swipeToRefresh.setOnRefreshListener {
            lifecycleScope.launch {
                registerViewModel.getAllCampaigns2()
            }
        }

        registerViewModel.getAllCampaignState.observe(viewLifecycleOwner) {
            if (it.loading) {
                binding.progress.show()
                binding.mainLayout.hide()
            } else {
//                binding.progress.hide()
//                binding.mainLayout.show()
                binding.swipeToRefresh.isRefreshing = false
            }
        }

        val linearLayout =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        binding.beneficiariesRecyclerview.layoutManager = linearLayout
        binding.beneficiariesRecyclerview.adapter = adapter

        lifecycleScope.launch {
            offlineViewModel.allCampaigns.observe(viewLifecycleOwner) { campaigns ->

                if (campaigns.isNotEmpty()) {
                    binding.progress.hide()
                    binding.mainLayout.show()
                }
                if (campaigns != adapter.getItems()) {
                    Timber.tag("LIST").v(campaigns.toString())
                    adapter.setItems(campaigns)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    val list by lazy { resources.getStringArray(R.array.campaign_types_list) }
    var selectIndex = -1

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        Timber.v("selected $position")
        val item = list.getOrNull(position)
        selectIndex = position
        item?.let {
            Timber.v("selected $it")
            offlineViewModel.allCampaigns.value?.let { camapignss ->
                Timber.v("list is  $camapignss")
                when (selectIndex) {
                    0 -> adapter.setItems(camapignss)
                    1 -> adapter.setItems(camapignss.filter { it.type.equals("cash-for-work", true) })
                    2 -> adapter.setItems(camapignss.filter { it.type.equals("item", true) })
                }
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        Timber.v("clicked $position")
        val item = list.getOrNull(position)
        selectIndex = position
        item?.let {
            Timber.v("selected $it")
            offlineViewModel.allCampaigns.value?.let { camapignss ->
                Timber.v("list is  $camapignss")
                when (selectIndex) {
                    0 -> adapter.setItems(camapignss)
                    1 -> adapter.setItems(camapignss.filter { it.type.equals("cash-for-work", true) })
                    2 -> adapter.setItems(camapignss.filter { it.type.equals("item", true) })
                }
            }
        }
    }
}

const val SELECT_CAMPAIGN_KEY = "1298"
const val SELECT_CAMPAIGN_BUNDLE_KEY = "selected"
