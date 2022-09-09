package com.codose.chats.views.beneficiary_onboarding

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.codose.chats.R
import com.codose.chats.databinding.FragmentExistingBeneficiaryBinding
import com.codose.chats.model.ModelCampaign
import com.codose.chats.utils.BluetoothConstants
import com.codose.chats.utils.BluetoothConstants.CAMPAIGN_BUNDLE_KEY
import com.codose.chats.utils.BluetoothConstants.FRAGMENT_BENEFICIARY_RESULT_LISTENER
import com.codose.chats.utils.BluetoothConstants.FRAGMENT_CAMPAIGN_RESULT_LISTENER
import com.codose.chats.views.beneficiary_onboarding.ExistingBeneficiaryViewModel.ExistingBeneficiaryUiState
import com.codose.chats.views.beneficiary_search.BeneficiaryUi
import org.koin.androidx.viewmodel.ext.android.viewModel

class ExistingBeneficiaryFragment : Fragment(R.layout.fragment_existing_beneficiary) {

    private var _binding: FragmentExistingBeneficiaryBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModel<ExistingBeneficiaryViewModel>()

    private var beneficiaryId: Int? = null
    private var campaign: ModelCampaign? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setFragmentResultListener(FRAGMENT_BENEFICIARY_RESULT_LISTENER) { _, bundle ->
            val result =
                bundle.getParcelable<BeneficiaryUi>(BluetoothConstants.BENEFICIARY_BUNDLE_KEY)
            result?.let { populateFieldsWithExistingBeneficiary(beneficiary = it) }
            beneficiaryId = result?.id
        }

        setFragmentResultListener(FRAGMENT_CAMPAIGN_RESULT_LISTENER) { _, bundle ->
            campaign = bundle.getParcelable(CAMPAIGN_BUNDLE_KEY)
            binding.registerCampaignEdit.setText(campaign?.title)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentExistingBeneficiaryBinding.bind(view)

        setupClickListeners()
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.uiState.observe(viewLifecycleOwner) {
            when (it) {
                is ExistingBeneficiaryUiState.Error -> handleError(it.errorMessage)
                ExistingBeneficiaryUiState.Loading -> handleLoading()
                is ExistingBeneficiaryUiState.Success -> handleSuccess(it.message)
            }
        }
    }

    private fun setupClickListeners() = with(binding) {
        backBtn.setOnClickListener { findNavController().navigateUp() }
        searchBeneficiaryEditText.setOnClickListener {
            findNavController().navigate(ExistingBeneficiaryFragmentDirections.toBeneficiarySearchDialog())
        }
        registerCampaignEdit.setOnClickListener {
            findNavController().navigate(ExistingBeneficiaryFragmentDirections.toCampaignDialog())
        }
        addBeneficiaryButton.setOnClickListener {
            if (campaign == null && beneficiaryId == null) {
                Toast.makeText(requireContext(),
                    "Kindly fill all required fields",
                    Toast.LENGTH_SHORT).show()
            } else if (campaign == null && beneficiaryId != null) {
                Toast.makeText(requireContext(),
                    "Select a campaign",
                    Toast.LENGTH_SHORT).show()
            } else if (campaign != null && beneficiaryId == null) {
                Toast.makeText(requireContext(),
                    "Search and select a beneficiary",
                    Toast.LENGTH_SHORT).show()
            } else {
                campaign?.let { c ->
                    beneficiaryId?.let { bId ->
                        viewModel.addBeneficiaryToCampaign(campaignId = c.id, beneficiaryId = bId)
                    }
                }
            }
        }
    }

    private fun populateFieldsWithExistingBeneficiary(beneficiary: BeneficiaryUi) = with(binding) {
        registerFirstNameEdit.setText(beneficiary.firstName)
        registerLastNameEdit.setText(beneficiary.lastName)
        registerEmailEdit.setText(beneficiary.email)
        registerPhoneEdit.setText(beneficiary.phone)
        registerDateEdit.setText(beneficiary.dob)
        registerGenderEdit.setText(beneficiary.gender)
        beneficiaryDetailsGroup.isVisible = true
    }

    private fun handleLoading() = with(binding) {
        loader.progressIndicator.isVisible = true
        addBeneficiaryButton.isInvisible = true
    }

    private fun handleError(errorMessage: String?) = with(binding) {
        loader.progressIndicator.isInvisible = true
        addBeneficiaryButton.isVisible = true
        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
    }

    private fun handleSuccess(message: String) = with(binding) {
        loader.progressIndicator.isInvisible = true
        addBeneficiaryButton.isVisible = true
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        findNavController().navigate(ExistingBeneficiaryFragmentDirections.toOnboardingFragment())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
