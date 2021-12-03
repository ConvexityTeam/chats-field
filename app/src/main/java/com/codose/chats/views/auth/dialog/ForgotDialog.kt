package com.codose.chats.views.auth.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.codose.chats.R
import com.codose.chats.utils.ApiResponse
import com.codose.chats.utils.hide
import com.codose.chats.utils.show
import com.codose.chats.utils.toast
import com.codose.chats.views.auth.viewmodel.RegisterViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_forgot.*
import kotlinx.coroutines.InternalCoroutinesApi
import org.koin.androidx.viewmodel.ext.android.viewModel

@InternalCoroutinesApi
class ForgotDialog : BottomSheetDialogFragment() {
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
        return inflater.inflate(R.layout.dialog_forgot, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        forgotButton.setOnClickListener {
            validateFields()
        }

        viewModel.forgot.observe(viewLifecycleOwner, {
            when (it) {
                is ApiResponse.Loading -> {
                    forgotProgress.show()
                }
                is ApiResponse.Success -> {
                    val data = it.data
                    forgotProgress.hide()
                    requireContext().toast(data.message)
//                    targetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_OK, Intent().putExtra("login",true))
                    dismiss()
                }
                is ApiResponse.Failure -> {
                    forgotProgress.hide()
                    if(it.code in 400..499){
                        requireContext().toast("Unauthorized. Please check your credentials.")
                    }else{
                        requireContext().toast("An error occurred")
                    }
                }
            }
        })
    }

    private fun validateFields() {
        val email = forgotEmailEdit.text.toString()
        if(forgotEmailEdit.text.isNullOrBlank()){
            forgotEmailLayout.error = "Email is required"
            return
        }else{
            forgotEmailLayout.error = ""
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
