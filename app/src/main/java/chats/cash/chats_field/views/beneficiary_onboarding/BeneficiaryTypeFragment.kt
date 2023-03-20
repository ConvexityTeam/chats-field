package chats.cash.chats_field.views.beneficiary_onboarding

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.FragmentBeneficiaryTypeBinding
import chats.cash.chats_field.utils.PreferenceUtil
import chats.cash.chats_field.utils.safeNavigate
import chats.cash.chats_field.utils.showToast
import chats.cash.chats_field.views.auth.login.LoginDialog
import org.koin.android.ext.android.inject

class BeneficiaryTypeFragment : Fragment(R.layout.fragment_beneficiary_type) {

    private var _binding: FragmentBeneficiaryTypeBinding? = null
    private val binding get() = _binding!!
    private val preferenceUtil: PreferenceUtil by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentBeneficiaryTypeBinding.bind(view)

        setupClickListeners()

        if (preferenceUtil.getNGOId() == 0) {
            openLogin()
        }
    }

    private fun setupClickListeners() = with(binding) {
        backBtn.setOnClickListener { findNavController().navigateUp() }
        newBeneficiaryButton.setOnClickListener {
            if (preferenceUtil.getNGOId() == 0) {
                showToast("Please Log In")
                openLogin()
                return@setOnClickListener
            }
            findNavController().safeNavigate(R.id.to_dataConsentFragment)
        }
        existingBeneficiaryButton.setOnClickListener {
            if (preferenceUtil.getNGOId() == 0) {
                showToast("Please Log In")
                openLogin()
                return@setOnClickListener
            }
            findNavController().safeNavigate(BeneficiaryTypeFragmentDirections.toExistingBeneficiaryFragment())
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
