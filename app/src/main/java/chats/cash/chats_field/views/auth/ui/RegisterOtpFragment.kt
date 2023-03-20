package chats.cash.chats_field.views.auth.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.FragmentRegisterOtpBinding


class RegisterOtpFragment : Fragment() {

    private lateinit var binding:FragmentRegisterOtpBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentRegisterOtpBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        class OTPTextWatcher constructor(val v: View) : TextWatcher {
            override fun afterTextChanged(editable: Editable) {
                // TODO Auto-generated method stub
                val text = editable.toString()
                when (v.id) {
                    R.id.otp1 -> if (text.length == 1) binding.otp2.requestFocus()
                    R.id.otp2 -> if (text.length == 1) binding.otp3.requestFocus() else if (text.isEmpty()) binding.otp1.requestFocus()
                    R.id.otp3 -> if (text.length == 1) binding.otp4.requestFocus() else if (text.isEmpty()) binding.otp2.requestFocus()
                    R.id.otp4 -> if (text.isEmpty()) binding.otp3.requestFocus()
                }
            }

            override fun beforeTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {
                // TODO Auto-generated method stub
            }

            override fun onTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {
                // TODO Auto-generated method stub
            }

        }

        binding.otp1.addTextChangedListener(OTPTextWatcher(binding.otp1));
        binding.otp2.addTextChangedListener(OTPTextWatcher(binding.otp2));
        binding.otp3.addTextChangedListener(OTPTextWatcher(binding.otp3));
        binding.otp4.addTextChangedListener(OTPTextWatcher(binding.otp4));

        binding.registerOtpVerifyButton.setOnClickListener {
//            findNavController().navigate(RegisterOtpFragmentDirections.actionRegisterOtpFragmentToRegisterVerifyFragment())
        }

    }


}