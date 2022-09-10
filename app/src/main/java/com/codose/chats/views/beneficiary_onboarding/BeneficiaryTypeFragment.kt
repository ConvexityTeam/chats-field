package com.codose.chats.views.beneficiary_onboarding

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.codose.chats.R
import com.codose.chats.databinding.FragmentBeneficiaryTypeBinding
import com.codose.chats.utils.PrefUtils
import com.codose.chats.utils.showToast
import com.codose.chats.views.auth.dialog.LoginDialog

class BeneficiaryTypeFragment : Fragment(R.layout.fragment_beneficiary_type) {

    private var _binding: FragmentBeneficiaryTypeBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentBeneficiaryTypeBinding.bind(view)

        setupClickListeners()

        if (PrefUtils.getNGOId() == 0) {
            openLogin()
        }
    }

    private fun setupClickListeners() = with(binding) {
        backBtn.setOnClickListener { findNavController().navigateUp() }
        newBeneficiaryButton.setOnClickListener {
            if (PrefUtils.getNGOId() == 0) {
                showToast("Please Log In")
                openLogin()
                return@setOnClickListener
            }
            findNavController().navigate(BeneficiaryTypeFragmentDirections.toRegisterFragment())
        }
        existingBeneficiaryButton.setOnClickListener {
            if (PrefUtils.getNGOId() == 0) {
                showToast("Please Log In")
                openLogin()
                return@setOnClickListener
            }
            findNavController().navigate(BeneficiaryTypeFragmentDirections.toExistingBeneficiaryFragment())
        }
    }

    private fun openLogin(isCancelable: Boolean = true) {
        val bottomSheetDialogFragment = LoginDialog.newInstance()
        bottomSheetDialogFragment.isCancelable = isCancelable
        bottomSheetDialogFragment.setTargetFragment(this, 700)
        bottomSheetDialogFragment.show(requireFragmentManager().beginTransaction(),
            "BottomSheetDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
