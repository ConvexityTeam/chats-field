package chats.cash.chats_field.views.beneficiary_onboarding.select_beneficiary

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.FragmentSelectBeneficiaryTypeBinding
import chats.cash.chats_field.utils.safeNavigate
import chats.cash.chats_field.views.auth.viewmodel.RegisterViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class SelectBeneficiaryTypeFragment : Fragment(R.layout.fragment_select_beneficiary_type) {

    private var _binding: FragmentSelectBeneficiaryTypeBinding? = null
    private val registerViewModel by activityViewModel<RegisterViewModel>()
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSelectBeneficiaryTypeBinding.bind(view)
        setUpClickListeners()
    }

    private fun setUpClickListeners() {
        registerViewModel.isGroupBeneficiary = false

        binding.appbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.multipleBeneficiary.setOnClickListener {
            registerViewModel.isGroupBeneficiary = true
            findNavController().navigate(R.id.action_selectBeneficiaryTypeFragment_to_beneficiaryGroupDetailsFragment)
        }

        binding.singleBeneficiary.setOnClickListener {
            registerViewModel.isGroupBeneficiary = false
            findNavController().safeNavigate(R.id.action_select_beneficiary_type_to_register_fragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
