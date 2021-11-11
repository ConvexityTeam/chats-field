package com.codose.chats.views.auth.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.codose.chats.R
import kotlinx.android.synthetic.main.fragment_register_otp.*

class RegisterOtpFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register_otp, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        class OTPTextWatcher constructor(val v: View) : TextWatcher {
            override fun afterTextChanged(editable: Editable) {
                // TODO Auto-generated method stub
                val text = editable.toString()
                when (v.id) {
                    R.id.otp1 -> if (text.length == 1) otp2.requestFocus()
                    R.id.otp2 -> if (text.length == 1) otp3.requestFocus() else if (text.isEmpty()) otp1.requestFocus()
                    R.id.otp3 -> if (text.length == 1) otp4.requestFocus() else if (text.isEmpty()) otp2.requestFocus()
                    R.id.otp4 -> if (text.isEmpty()) otp3.requestFocus()
                }
            }

            override fun beforeTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {
                // TODO Auto-generated method stub
            }

            override fun onTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {
                // TODO Auto-generated method stub
            }

        }

        otp1.addTextChangedListener(OTPTextWatcher(otp1));
        otp2.addTextChangedListener(OTPTextWatcher(otp2));
        otp3.addTextChangedListener(OTPTextWatcher(otp3));
        otp4.addTextChangedListener(OTPTextWatcher(otp4));

        registerOtpVerifyButton.setOnClickListener {
//            findNavController().navigate(RegisterOtpFragmentDirections.actionRegisterOtpFragmentToRegisterVerifyFragment())
        }

    }


}