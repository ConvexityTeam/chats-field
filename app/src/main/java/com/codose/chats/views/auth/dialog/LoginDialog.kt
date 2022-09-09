package com.codose.chats.views.auth.dialog

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.codose.chats.R
import com.codose.chats.databinding.DialogLoginBinding
import com.codose.chats.network.api.SessionManager
import com.codose.chats.network.body.login.LoginBody
import com.codose.chats.utils.*
import com.codose.chats.views.auth.viewmodel.RegisterViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_onboarding.*
import kotlinx.coroutines.InternalCoroutinesApi
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

@InternalCoroutinesApi
class LoginDialog : BottomSheetDialogFragment() {

    private lateinit var binding: DialogLoginBinding
    private val viewModel  by viewModel<RegisterViewModel>()

    // Handling token bearer for field agent
    private lateinit var sessionManager: SessionManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheet)
        sessionManager = context?.let { SessionManager(it) }!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogLoginBinding.bind(inflater.inflate(R.layout.dialog_login, container, false))
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loginButton.setOnClickListener {
            validateFields()
        }

        binding.forgotPasswordText.setOnClickListener {
            openForgot()
        }

        viewModel.login.observe(viewLifecycleOwner) {
            when (it) {
                is ApiResponse.Loading -> {
                    binding.loginProgress.show()
                }
                is ApiResponse.Success -> {
                    val data = it.data.data
                    PrefUtils.setNGOToken("Bearer " + data.token)
                    Timber.d(data.token)
                    PrefUtils.setNGO(data.user.associatedOrganisations.first().OrganisationId, "")
                    targetFragment?.onActivityResult(targetRequestCode,
                        Activity.RESULT_OK,
                        Intent().putExtra("login", true))
                    findNavController().navigate(R.id.action_registerFragment_to_onboardingFragment)
                    dismiss()
                }
                is ApiResponse.Failure -> {
                    binding.loginProgress.hide()
                    if (it.code == 401) {
                        doLogout()
                        openLogin()
                    }
                    requireContext().toast(it.message)
                }
            }
        }

        viewModel.userDetails.observe(viewLifecycleOwner) {
            when (it) {
                is ApiResponse.Loading -> {
                    binding.loginProgress.show()
                }
                is ApiResponse.Success -> {
                    val data = it.data.data
                    binding.loginProgress.hide()


                }
                is ApiResponse.Failure -> {
                    binding.loginProgress.hide()
                    requireContext().toast("An error occurred")
                }
            }
        }

    }

    private fun validateFields() = with(binding) {
        val email = loginEmailEdit.text.toString()
        val password = loginPasswordEdit.text.toString()
        if (loginEmailEdit.text.isNullOrBlank()) {
            loginEmailLayout.error = "Email is required"
            return
        } else {
            loginEmailLayout.error = ""
        }
        if (loginPasswordEdit.text.isNullOrBlank()) {
            loginPasswordLayout.error = "Password is required"
            return
        } else {
            loginPasswordLayout.error = ""
        }
        val loginBody = LoginBody(email, password)
        viewModel.loginNGO(loginBody)
    }

    companion object {
        fun newInstance(): LoginDialog =
            LoginDialog().apply {
                arguments = Bundle().apply {

                }
            }
    }

    private fun doLogout() {
        PrefUtils.setNGO(0, "")
        logoutBtn.hide()
    }

    private fun openForgot(isCancelable : Boolean = true) {
        val bottomSheetDialogFragment = ForgotDialog.newInstance()
        bottomSheetDialogFragment.isCancelable = isCancelable
        bottomSheetDialogFragment.setTargetFragment(this, 700)
        bottomSheetDialogFragment.show(requireFragmentManager().beginTransaction(),"BottomSheetDialog")
    }

    private fun openLogin(isCancelable: Boolean = true) {
        val bottomSheetDialogFragment = LoginDialog.newInstance()
        bottomSheetDialogFragment.isCancelable = isCancelable
        bottomSheetDialogFragment.setTargetFragment(this, 700)
        bottomSheetDialogFragment.show(requireFragmentManager().beginTransaction(),
            "BottomSheetDialog")
    }
}