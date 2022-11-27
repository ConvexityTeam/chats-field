package chats.cash.chats_field.views.auth.onboarding

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.FragmentOnboardingBinding
import chats.cash.chats_field.utils.*
import chats.cash.chats_field.utils.ChatsFieldConstants.FRAGMENT_LOGIN_RESULT_KEY
import chats.cash.chats_field.utils.ChatsFieldConstants.LOGIN_BUNDLE_KEY
import chats.cash.chats_field.views.auth.adapter.OnBoarding
import chats.cash.chats_field.views.auth.adapter.OnboardingAdapter
import chats.cash.chats_field.views.auth.login.LoginDialog
import chats.cash.chats_field.views.auth.ui.ImageCaptureFragment
import chats.cash.chats_field.views.auth.ui.RegisterImageFragmentDirections
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_auth.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class OnboardingFragment : Fragment(R.layout.fragment_onboarding) {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModel<OnboardingViewModel>()
    private val adapter: OnboardingAdapter by lazy { OnboardingAdapter() }
    private val preferenceUtil: PreferenceUtil by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setFragmentResultListener(FRAGMENT_LOGIN_RESULT_KEY) { _, bundle ->
            val result = bundle.getBoolean(LOGIN_BUNDLE_KEY)
            binding.logoutBtn.isVisible = result
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentOnboardingBinding.bind(view)

        requireActivity().pendingUploadTextView.show()
        val onboardings = arrayListOf<OnBoarding>()
        onboardings.add(OnBoarding(getString(R.string.onb_title_1),
            getString(R.string.onb_desc_1),
            R.drawable.ic_onboard_one))
        onboardings.add(OnBoarding(getString(R.string.onb_title_2),
            getString(R.string.onb_desc_2),
            R.drawable.ic_onboarding_two))
        onboardings.add(OnBoarding(getString(R.string.onb_title_3),
            getString(R.string.onb_desc_3),
            R.drawable.ic_onboarding_three))
        adapter.submitList(onboardings)
        binding.run {
            onboardingViewPager.adapter = adapter
            TabLayoutMediator(onboardingTab, onboardingViewPager) { _, _ -> }.attach()

            if (preferenceUtil.getNGOId() == 0) {
                logoutBtn.hide()
            } else {
                logoutBtn.show()
            }
        }

        setupClickListeners()

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                ImageCaptureFragment.REQUIRED_PERMISSIONS,
                ImageCaptureFragment.REQUEST_CODE_PERMISSIONS)
        }
    }

    private fun setupClickListeners() = with(binding) {
        onboardingCashForWorkBtn.setOnClickListener { openCashForWork() }
        beneficiaryOnboardingBtn.setOnClickListener { openBeneficiaryOnboarding() }
        logoutBtn.setOnClickListener { doLogout() }
        vendorOnboardingBtn.setOnClickListener { openVendorOnboarding() }
    }

    private fun doLogout() {
        preferenceUtil.clearPreference()
        viewModel.clearAllTables()
        binding.logoutBtn.hide()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().pendingUploadTextView.hide()
        _binding = null
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        if (requestCode == ImageCaptureFragment.REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                findNavController().safeNavigate(RegisterImageFragmentDirections.toImageCaptureFragment())
            } else {
                requireContext().toast("Permissions not granted by the user.")
                findNavController().navigateUp()
            }
        }
    }

    companion object {
        val REQUIRED_PERMISSIONS =
            arrayOf(Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    private fun openCashForWork() {
        if (preferenceUtil.getNGOId() == 0) {
            openLogin()
        } else {
            findNavController().safeNavigate(OnboardingFragmentDirections.toCashForWorkFragment())
        }
    }

    private fun openBeneficiaryOnboarding() {
        if (preferenceUtil.getNGOId() == 0) {
            openLogin()
        } else {
            findNavController().safeNavigate(OnboardingFragmentDirections.toBeneficiaryTypeFragment())
        }
    }

    private fun openVendorOnboarding() {
        if (preferenceUtil.getNGOId() == 0) {
            openLogin()
        } else {
            findNavController().safeNavigate(OnboardingFragmentDirections.toVendorFragment())
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireActivity().baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun openLogin(isCancelable: Boolean = true) {
        val bottomSheetDialogFragment = LoginDialog.newInstance()
        bottomSheetDialogFragment.isCancelable = isCancelable
        bottomSheetDialogFragment.show(parentFragmentManager, "BottomSheetDialog")
    }
}
