package com.codose.chats.views.auth.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.codose.chats.R
import com.codose.chats.databinding.FragmentOnboardingBinding
import com.codose.chats.utils.PrefUtils
import com.codose.chats.utils.hide
import com.codose.chats.utils.show
import com.codose.chats.utils.toast
import com.codose.chats.views.auth.adapter.OnBoarding
import com.codose.chats.views.auth.adapter.OnboardingAdapter
import com.codose.chats.views.auth.dialog.LoginDialog
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_auth.*
import kotlinx.coroutines.InternalCoroutinesApi
import timber.log.Timber

@InternalCoroutinesApi
class OnboardingFragment : Fragment(R.layout.fragment_onboarding) {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: OnboardingAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentOnboardingBinding.bind(view)

        adapter = OnboardingAdapter(requireContext())
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
            TabLayoutMediator(onboardingTab, onboardingViewPager) { tab, position ->

            }.attach()

            Timber.v("token is " + PrefUtils.getNGOId().toString())
            Timber.v("token is " + PrefUtils.getNGOToken().toString())
            onboardingCashForWorkBtn.setOnClickListener {
                openCashForWork()
            }

            onboardingSignUpBtn.setOnClickListener {
                findNavController().navigate(OnboardingFragmentDirections.toBeneficiaryTypeFragment())
            }

            logoutBtn.setOnClickListener {
                doLogout()
            }

            if (PrefUtils.getNGOId() == 0) {
                logoutBtn.hide()
            } else {
                logoutBtn.show()
            }

            onboardingLogInBtn.setOnClickListener {
                findNavController().navigate(OnboardingFragmentDirections.actionOnboardingFragmentToVendorFragment())
            }
        }

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                ImageCaptureFragment.REQUIRED_PERMISSIONS,
                ImageCaptureFragment.REQUEST_CODE_PERMISSIONS)
        }
    }

    private fun doLogout() {
        PrefUtils.setNGO(0, "")
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
                findNavController().navigate(RegisterImageFragmentDirections.actionRegisterImageFragmentToImageCaptureFragment())
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
        if (PrefUtils.getNGOId() == 0) {
            openLogin()
        } else {
            findNavController().navigate(OnboardingFragmentDirections.actionOnboardingFragmentToCashForWorkFragment())
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireActivity().baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun openLogin(isCancelable: Boolean = true) {
        val bottomSheetDialogFragment = LoginDialog.newInstance()
        bottomSheetDialogFragment.isCancelable = isCancelable
        bottomSheetDialogFragment.setTargetFragment(this, 700)
        bottomSheetDialogFragment.show(requireFragmentManager().beginTransaction(),
            "BottomSheetDialog")
    }

}