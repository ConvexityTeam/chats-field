package chats.cash.chats_field.views.nin

import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.FragmentEnterNinBinding
import chats.cash.chats_field.views.auth.viewmodel.RegisterViewModel
import chats.cash.chats_field.views.impact_report.disable
import chats.cash.chats_field.views.impact_report.enable
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class EnterNinFragment :
    Fragment(R.layout.fragment_enter_nin) {

    private var _binding: FragmentEnterNinBinding? = null
    private val binding get() = _binding!!

    val viewModel by activityViewModel<RegisterViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEnterNinBinding.bind(view)
        init()
    }

    private fun init() {
        binding.appbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.enterNin.addTextChangedListener {
            if (it.isNullOrBlank() || it.toString().length !in 10..16) {
                binding.enterNinParent.isErrorEnabled = true
                binding.enterNinParent.error = getString(R.string.invalid_nin)
                binding.ninGetStarted.disable()
            } else {
                binding.enterNinParent.isErrorEnabled = false
                binding.enterNinParent.error = ""
                viewModel.nin = it.toString()
                viewModel.specialCase = true
                binding.ninGetStarted.enable()
            }
        }

        binding.ninGetStarted.setOnClickListener {
            findNavController().navigate(R.id.action_enterNinFragment_to_submitBeneficiary)
        }
    }
}
