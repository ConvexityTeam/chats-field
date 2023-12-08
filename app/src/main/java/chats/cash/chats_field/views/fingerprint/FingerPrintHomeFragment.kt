package chats.cash.chats_field.views.fingerprint

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.FragmentFingerprintHomeBinding

class FingerPrintHomeFragment :
    Fragment(R.layout.fragment_fingerprint_home) {

    private var _binding: FragmentFingerprintHomeBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFingerprintHomeBinding.bind(view)
        init()
    }

    private fun init() {
        binding.appbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.startScanning.setOnClickListener {
            findNavController().navigate(R.id.action_fingerPrintHomeFragment_to_fingerPrintScanHomeFragment)
        }
    }
}
