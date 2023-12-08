package chats.cash.chats_field.views.auth.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.DialogForgotBinding
import chats.cash.chats_field.network.NetworkResponse
import chats.cash.chats_field.utils.hide
import chats.cash.chats_field.utils.show
import chats.cash.chats_field.utils.toast
import chats.cash.chats_field.views.auth.viewmodel.RegisterViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class ForgotDialog : BottomSheetDialogFragment() {
    private val viewModel by viewModel<RegisterViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheet)
    }

    private lateinit var binding: DialogForgotBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = DialogForgotBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.forgotButton.setOnClickListener {
            validateFields()
        }

        viewModel.forgot.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkResponse.Loading -> {
                    binding.forgotProgress.show()
                }
                is NetworkResponse.Success -> {
                    val data = it.body
                    binding.forgotProgress.hide()
                    requireContext().toast(data.message)
                    dismiss()
                }
                is NetworkResponse.Error -> {
                    binding.forgotProgress.hide()
                    requireContext().toast(it._message)
                } is NetworkResponse.SimpleError -> {
                    binding.forgotProgress.hide()
                    requireContext().toast(it._message)
                }
                else -> {
                }
            }
        }
    }

    private fun validateFields() {
        val email = binding.forgotEmailEdit.text.toString()
        if (binding.forgotEmailEdit.text.isNullOrBlank()) {
            binding.forgotEmailLayout.error = "Email is required"
            return
        } else {
            binding.forgotEmailLayout.error = ""
        }
        viewModel.sendForgotEmail(email)
    }

    companion object {
        fun newInstance(): ForgotDialog =
            ForgotDialog().apply {
                arguments = Bundle().apply {
                }
            }
    }
}
