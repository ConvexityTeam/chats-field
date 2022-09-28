package chats.cash.chats_field.views.beneficiary_list

import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.FragmentBeneficiaryListBinding
import chats.cash.chats_field.network.response.beneficiary_onboarding.Beneficiary
import chats.cash.chats_field.utils.showToast
import org.koin.androidx.viewmodel.ext.android.viewModel

class BeneficiaryListFragment : Fragment(R.layout.fragment_beneficiary_list) {

    private var _binding: FragmentBeneficiaryListBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModel<BeneficiaryListViewModel>()
    private val beneficiaryListAdapter: BeneficiaryListAdapter by lazy {
        BeneficiaryListAdapter(onSelectClick = ::addBeneficiaryToCampaign)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentBeneficiaryListBinding.bind(view)
        setupObservers()
        binding.backButton.setOnClickListener { findNavController().navigateUp() }
    }

    private fun setupObservers() {
        viewModel.uiState.observe(viewLifecycleOwner, ::handleState)
    }

    private fun handleState(state: BeneficiaryListViewModel.BeneficiaryListUiState) =
        with(binding) {
            fun handleSuccess(beneficiaries: List<Beneficiary>) {
                loadingLayout.root.isGone = true
                beneficiariesList.adapter = beneficiaryListAdapter
                beneficiaryListAdapter.submitList(beneficiaries)
            }

            fun handleLoading() {
                loadingLayout.root.isVisible = true
            }

            fun handleError(errorMessage: String?) {
                showToast(errorMessage)
                loadingLayout.root.isGone = true
            }
            when (state) {
                is BeneficiaryListViewModel.BeneficiaryListUiState.Error -> handleError(state.errorMessage)
                BeneficiaryListViewModel.BeneficiaryListUiState.Loading -> handleLoading()
                is BeneficiaryListViewModel.BeneficiaryListUiState.Success -> handleSuccess(state.beneficiaries)
            }
        }

    private fun addBeneficiaryToCampaign(beneficiary: Beneficiary) {
        showToast("Todo: Add beneficiary: '${beneficiary.firstName}' to campaign")
    }
}
