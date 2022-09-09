package com.codose.chats.views.beneficiary_search

import android.app.Dialog
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import com.codose.chats.R
import com.codose.chats.databinding.DialogBeneficiarySearchBinding
import com.codose.chats.utils.BluetoothConstants.BENEFICIARY_BUNDLE_KEY
import com.codose.chats.utils.BluetoothConstants.FRAGMENT_BENEFICIARY_RESULT_LISTENER
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class BeneficiarySearchDialog : BottomSheetDialogFragment() {

    private var _binding: DialogBeneficiarySearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModel<BeneficiarySearchViewModel>()

    private val beneficiaryAdapter: BeneficiarySearchAdapter by lazy {
        BeneficiarySearchAdapter(onBeneficiaryClick = {
            val result = bundleOf(BENEFICIARY_BUNDLE_KEY to it)
            setFragmentResult(FRAGMENT_BENEFICIARY_RESULT_LISTENER, result)
            dismiss()
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding =
            DialogBeneficiarySearchBinding.bind(inflater.inflate(R.layout.dialog_beneficiary_search,
                container,
                false))

        setupClickListeners()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                BeneficiarySearchViewModel.BeneficiarySearchUiState.EmptyBeneficiaries -> handleEmptyState()
                is BeneficiarySearchViewModel.BeneficiarySearchUiState.Error -> handleError(state.errorMessage)
                BeneficiarySearchViewModel.BeneficiarySearchUiState.Loading -> handleLoading()
                is BeneficiarySearchViewModel.BeneficiarySearchUiState.Success -> {
                    handleSuccess(state.beneficiaries)
                }
            }
        }
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

    private fun setupClickListeners() = with(binding) {
        searchButton.setOnClickListener {
            viewModel.loadBeneficiaries(
                firstName = firstNameEdit.text.toString(),
                lastName = lastNameEdit.text.toString(),
                email = emailEditText.text.toString(),
                phone = phoneEditText.text.toString(),
                nin = ninEditText.text.toString()
            )
        }
    }

    private fun handleSuccess(beneficiaries: List<BeneficiaryUi>) = with(binding) {
        beneficiariesList.isVisible = true
        beneficiariesList.adapter = beneficiaryAdapter
        beneficiaryAdapter.submitList(beneficiaries)
        loader.progressIndicator.isInvisible = true
        searchButton.isVisible = true
        emptyState.isGone = true
    }

    private fun handleLoading() = with(binding) {
        loader.progressIndicator.isVisible = true
        searchButton.isInvisible = true
        emptyState.isGone = true
    }

    private fun handleEmptyState() = with(binding) {
        loader.progressIndicator.isInvisible = true
        searchButton.isVisible = true
        emptyState.isVisible = true
        beneficiariesList.isGone = true
    }

    private fun handleError(errorMessage: String?) = with(binding) {
        loader.progressIndicator.isInvisible = true
        searchButton.isVisible = true
        emptyState.isGone = true
        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
