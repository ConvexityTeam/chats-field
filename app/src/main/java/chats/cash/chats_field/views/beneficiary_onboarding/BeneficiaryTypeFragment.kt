package chats.cash.chats_field.views.beneficiary_onboarding

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.FragmentBeneficiaryTypeBinding
import chats.cash.chats_field.utils.safeNavigate
import chats.cash.chats_field.views.auth.viewmodel.RegisterViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class BeneficiaryTypeFragment : Fragment(R.layout.fragment_beneficiary_type) {

    private var _binding: FragmentBeneficiaryTypeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RegisterViewModel by activityViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentBeneficiaryTypeBinding.bind(view)

        setupClickListeners()

        binding.appbar.title = viewModel.campaign?.title
    }

    private fun setupClickListeners() = with(binding) {
        appbar.setNavigationOnClickListener { findNavController().navigateUp() }
        newBeneficiaryButton.setOnClickListener {
            findNavController().safeNavigate(R.id.to_dataConsentFragment)
        }
        existingBeneficiaryButton.setOnClickListener {
            findNavController().safeNavigate(
                BeneficiaryTypeFragmentDirections.toExistingBeneficiaryFragment(),
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
