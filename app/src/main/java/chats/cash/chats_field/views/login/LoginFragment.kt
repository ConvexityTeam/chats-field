package chats.cash.chats_field.views.login

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.FragmentLoginBinding
import chats.cash.chats_field.network.body.login.LoginBody
import chats.cash.chats_field.utils.ChatsFieldConstants
import chats.cash.chats_field.utils.isEmailValid
import chats.cash.chats_field.utils.makeLinks
import chats.cash.chats_field.utils.safeNavigate
import chats.cash.chats_field.utils.showToast
import chats.cash.chats_field.utils.toast
import chats.cash.chats_field.views.auth.login.LoginViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class LoginFragment :
    Fragment(R.layout.fragment_login) {

    private var _binding: FragmentLoginBinding? = null
    private val viewModel by activityViewModel<LoginViewModel>()
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLoginBinding.bind(view)
        init()
    }

    private fun resetSystemBars() {
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        //     showBars()
        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, true)
    }

    override fun onResume() {
        super.onResume()
//        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, false)
//        requireActivity() . window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
//        requireActivity().  window.statusBarColor = Color.TRANSPARENT
//        requireActivity().setNavigationBarColor(Color.WHITE)
//            val decorView = requireActivity().window.decorView
//            decorView.systemUiVisibility =
//                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
//                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
//                        0
//        unset()
    }

    fun set() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowInsetsController = requireActivity().window.insetsController
            windowInsetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
            )
            windowInsetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
            )
        } else {
            requireActivity().window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }
    }

    private fun unset() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowInsetsController = requireActivity().window.insetsController
            windowInsetsController?.setSystemBarsAppearance(
                0,
                APPEARANCE_LIGHT_STATUS_BARS,
            )
            windowInsetsController?.setSystemBarsAppearance(
                0,
                WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
            )
        } else {
            requireActivity().window.decorView.systemUiVisibility =
                0 or 0
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //    showBars()
    }

    private fun showBars() {
        WindowInsetsControllerCompat(requireActivity().window, binding.root).show(WindowInsetsCompat.Type.systemBars())
        requireActivity().window.statusBarColor = Color.WHITE
        set()
        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowInsetsController = requireActivity().window.insetsController
            windowInsetsController?.setSystemBarsAppearance(0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
            windowInsetsController?.setSystemBarsAppearance(0, WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireActivity().window.decorView.systemUiVisibility = 0
        }

        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, true)
        resetSystemBars()
    }

    private fun init() {
        setupObservers()
        binding.loginButton.setOnClickListener {
            validateFields()
        }
        setupObservers()

        binding.terms.makeLinks(
            getString(R.string.terms_and_conditions) to View.OnClickListener {
                openLink(ChatsFieldConstants.TERMS_OF_USE)
            },
            getString(R.string.privacy_policy) to View.OnClickListener {
                openLink(ChatsFieldConstants.PRIVACY_POLICY)
            },
        )

        binding.loginPasswordEdit.addTextChangedListener {
            if (it.isNullOrBlank() || it.length < 3) {
                binding.loginButton.isEnabled = false
            } else {
                isEmailValid()
            }
        }

        binding.loginEmailEdit.addTextChangedListener {
            if (binding.loginPasswordEdit.text.isNullOrBlank() || binding.loginPasswordEdit.text.toString().length < 3) {
                return@addTextChangedListener
            }
            isEmailValid()
        }
    }

    private fun isEmailValid() {
        binding.loginButton.isEnabled = binding.loginEmailEdit.text.isNullOrBlank().not() && binding.loginEmailEdit.text.toString().isEmailValid()
    }

    private fun openLink(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    private fun setupObservers() = with(binding) {
        lifecycleScope.launch {
            viewModel.uiState.collect() {
                when (it) {
                    is LoginViewModel.LoginState.Error -> {
                        loginProgress.isVisible = false
                        loginButton.isVisible = true
                        requireContext().toast(it.errorMessage)
                    }

                    LoginViewModel.LoginState.Loading -> {
                        loginProgress.isVisible = true
                        loginButton.isInvisible = true
                    }

                    is LoginViewModel.LoginState.Success -> {
                        //dismiss()
                        showToast(getString(R.string.login_successful))
                        loginButton.isVisible = true
                        //   showBars()
                        findNavController().safeNavigate(LoginFragmentDirections.actionLoginFragmentToOnboardingFragment())
                    }

                    else -> {
                    }
                }
            }
        }
    }

    private fun validateFields() {
        with(binding) {
            val email = loginEmailEdit.text.toString()
            val password = loginPasswordEdit.text.toString()
            if (loginEmailEdit.text.isNullOrBlank()) {
                loginEmailLayout.error = getString(R.string.email_is_required)
                return
            } else {
                loginEmailLayout.error = ""
            }
            if (loginPasswordEdit.text.isNullOrBlank()) {
                loginPasswordLayout.error = getString(R.string.password_is_required)
                return
            } else {
                loginPasswordLayout.error = ""
            }
            val loginBody = LoginBody(email, password)
            viewModel.login(loginBody)
        }
    }
}
