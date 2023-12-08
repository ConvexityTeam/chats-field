package chats.cash.chats_field.views.auth.ui

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import chats.cash.chats_field.R
import chats.cash.chats_field.utils.PreferenceUtilInterface
import chats.cash.chats_field.utils.safeNavigate
import org.koin.android.ext.android.inject

class SplashFragment : Fragment() {
    var isAnim = false
    private val preferenceUtil: PreferenceUtilInterface by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Handler().postDelayed({
            isAnim = true
            if (preferenceUtil.getNGOId() == 0) {
                findNavController().safeNavigate(R.id.action_splashFragment_to_loginFragment)
            } else {
                try {
                    findNavController().safeNavigate(SplashFragmentDirections.actionSplashFragmentToOnboardingFragment())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }, 5739)
    }

    override fun onResume() {
        super.onResume()
        if (isAnim) {
            if (preferenceUtil.getNGOId() == 0) {
                findNavController().safeNavigate(R.id.action_splashFragment_to_loginFragment)
            } else {
                findNavController().safeNavigate(
                    SplashFragmentDirections.actionSplashFragmentToOnboardingFragment(),
                )
            }
        }
    }
}
