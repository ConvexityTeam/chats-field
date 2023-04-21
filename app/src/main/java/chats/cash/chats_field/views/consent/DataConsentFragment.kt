package chats.cash.chats_field.views.consent

import android.os.Bundle
import android.text.method.LinkMovementMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.FragmentDataConsentBinding
import chats.cash.chats_field.utils.safeNavigate

class DataConsentFragment : Fragment() {

    private lateinit var _binding:FragmentDataConsentBinding
    private val binding:FragmentDataConsentBinding
        get() = _binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentDataConsentBinding.inflate(inflater,container,false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.checkbox.addOnCheckedStateChangedListener { checkBox, state ->
            binding.continues.isEnabled = checkBox.isChecked
        }
        binding.continues.setOnClickListener {
            findNavController().safeNavigate(DataConsentFragmentDirections.actionDataConsentFragmentToRegisterFragment())
        }
        binding.backBtn.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.consentForm.movementMethod = LinkMovementMethod.getInstance()
    }


}