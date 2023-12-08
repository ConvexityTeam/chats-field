package chats.cash.chats_field.views.nin

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.FragmentRegisterNinHomeBinding

class NinHomeFragment :
    Fragment(R.layout.fragment_register_nin_home) {

    private var _binding: FragmentRegisterNinHomeBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRegisterNinHomeBinding.bind(view)
        init()
    }

    private fun init() {
        binding.appbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.ninGetStarted.setOnClickListener {
            findNavController().navigate(R.id.action_ninHomeFragment_to_enterNinFragment)
        }
    }
}
