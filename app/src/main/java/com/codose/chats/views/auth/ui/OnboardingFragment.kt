package com.codose.chats.views.auth.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.codose.chats.R
import com.codose.chats.offline.OfflineViewModel
import com.codose.chats.utils.PrefUtils
import com.codose.chats.utils.hide
import com.codose.chats.utils.show
import com.codose.chats.utils.toast
import com.codose.chats.views.auth.adapter.OnBoarding
import com.codose.chats.views.auth.adapter.OnboardingAdapter
import com.codose.chats.views.auth.dialog.LoginDialog
import com.codose.chats.views.base.BaseFragment
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_auth.*
import kotlinx.android.synthetic.main.fragment_onboarding.*
import kotlinx.coroutines.InternalCoroutinesApi
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber

@InternalCoroutinesApi
class  OnboardingFragment : BaseFragment() {
    private lateinit var adapter : OnboardingAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_onboarding, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = OnboardingAdapter(requireContext())
        requireActivity().pendingUploadTextView.show()
        val onboardings = arrayListOf<OnBoarding>()
        onboardings.add(OnBoarding(getString(R.string.onb_title_1), getString(R.string.onb_desc_1), R.drawable.ic_onboard_one))
        onboardings.add(OnBoarding(getString(R.string.onb_title_2), getString(R.string.onb_desc_2), R.drawable.ic_onboarding_two))
        onboardings.add(OnBoarding(getString(R.string.onb_title_3), getString(R.string.onb_desc_3), R.drawable.ic_onboarding_three))
        adapter.submitList(onboardings)
        onboarding_viewPager.adapter = adapter
        TabLayoutMediator(onboarding_tab, onboarding_viewPager) { tab, position ->

        }.attach()

        Timber.v("token is "+PrefUtils.getNGOId().toString())
        Timber.v("token is "+PrefUtils.getNGOToken().toString())
        onboardingCashForWorkBtn.setOnClickListener {
            openCashForWork()
        }

        onboardingSignUpBtn.setOnClickListener {
            findNavController().navigate(OnboardingFragmentDirections.actionOnboardingFragmentToRegisterFragment())
        }

        logoutBtn.setOnClickListener {
            doLogout()
        }

        if(PrefUtils.getNGOId() == 0){
            logoutBtn.hide()
        }else{
            logoutBtn.show()
        }

        onboardingLogInBtn.setOnClickListener {
            findNavController().navigate(OnboardingFragmentDirections.actionOnboardingFragmentToVendorFragment())
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
        logoutBtn.hide()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().pendingUploadTextView.hide()
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

    companion object{
        val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    private fun openCashForWork(){
        if(PrefUtils.getNGOId() == 0){
            openLogin()
        }else{
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