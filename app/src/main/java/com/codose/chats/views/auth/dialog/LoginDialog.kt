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
import com.codose.chats.network.body.login.LoginBody
import com.codose.chats.utils.*
import com.codose.chats.views.auth.viewmodel.RegisterViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_login.*
import kotlinx.coroutines.InternalCoroutinesApi
import org.koin.androidx.viewmodel.ext.android.viewModel

@InternalCoroutinesApi
class LoginDialog : BottomSheetDialogFragment() {
    private val viewModel  by viewModel<RegisterViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheet)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_login, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loginButton.setOnClickListener {
            validateFields()
        }

        forgotPasswordText.setOnClickListener {
            openForgot()
        }

        viewModel.login.observe(viewLifecycleOwner, {
            when (it) {
                is ApiResponse.Loading -> {
                    loginProgress.show()
                }
                is ApiResponse.Success -> {
                    val data = it.data.data
                    PrefUtils.setNGOToken("Bearer "+data.token)
                    PrefUtils.setNGO(data.user.associatedOrganisations.first().OrganisationId, "")
                    targetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_OK, Intent().putExtra("login",true))
                    findNavController().navigate(R.id.action_registerFragment_to_onboardingFragment)
                    dismiss()
                }
                is ApiResponse.Failure -> {
                    loginProgress.hide()
                    requireContext().toast(it.message)
                }
            }
        })

        viewModel.userDetails.observe(viewLifecycleOwner, {
            when (it) {
                is ApiResponse.Loading -> {
                    loginProgress.show()
                }
                is ApiResponse.Success -> {
                    val data = it.data.data
                    loginProgress.hide()


                }
                is ApiResponse.Failure -> {
                    loginProgress.hide()
                    requireContext().toast("An error occurred")
                }
            }
        })

    }

    private fun validateFields() {
        val email = loginEmailEdit.text.toString()
        val password = loginPasswordEdit.text.toString()
        if(loginEmailEdit.text.isNullOrBlank()){
            loginEmailLayout.error = "Email is required"
            return
        }else{
            loginEmailLayout.error = ""
        }
        if(loginPasswordEdit.text.isNullOrBlank()){
            loginPasswordLayout.error = "Password is required"
            return
        }else{
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

    private fun openForgot(isCancelable : Boolean = true) {
        val bottomSheetDialogFragment = ForgotDialog.newInstance()
        bottomSheetDialogFragment.isCancelable = isCancelable
        bottomSheetDialogFragment.setTargetFragment(this, 700)
        bottomSheetDialogFragment.show(requireFragmentManager().beginTransaction(),"BottomSheetDialog")
    }
}