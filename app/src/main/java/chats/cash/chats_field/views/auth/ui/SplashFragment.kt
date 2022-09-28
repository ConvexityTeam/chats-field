package chats.cash.chats_field.views.auth.ui

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import chats.cash.chats_field.R
import chats.cash.chats_field.utils.AES
import timber.log.Timber

class SplashFragment : Fragment() {
    var isAnim = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Handler().postDelayed({
            isAnim = true
            findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToOnboardingFragment())
        }, 5739)

        val aes = AES()
        val encrypted = aes.encrypt("Hello".toByteArray())
        val decrypted = aes.decrypt(encrypted)
        Timber.v("Decrypted Text : $decrypted ; Encrypted Text : ${encrypted.toString()}")
    }

    override fun onResume() {
        super.onResume()
        if(isAnim){
            findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToOnboardingFragment())
        }
    }

}