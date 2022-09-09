package com.codose.chats.views.beneficiary_onboarding

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.codose.chats.R
import com.codose.chats.databinding.FragmentBeneficiaryTypeBinding

class BeneficiaryTypeFragment : Fragment(R.layout.fragment_beneficiary_type) {

    private var _binding: FragmentBeneficiaryTypeBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentBeneficiaryTypeBinding.bind(view)

        setupClickListeners()
    }

    private fun setupClickListeners() = with(binding) {
        backBtn.setOnClickListener { findNavController().navigateUp() }
        newBeneficiaryButton.setOnClickListener {
            findNavController().navigate(BeneficiaryTypeFragmentDirections.toRegisterFragment())
        }
        existingBeneficiaryButton.setOnClickListener {
            findNavController().navigate(BeneficiaryTypeFragmentDirections.toExistingBeneficiaryFragment())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
