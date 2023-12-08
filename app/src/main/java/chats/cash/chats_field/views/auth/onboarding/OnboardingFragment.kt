package chats.cash.chats_field.views.auth.onboarding

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.FragmentOnboardingBinding
import chats.cash.chats_field.utils.ChatsFieldConstants.REQUEST_CODE_PERMISSIONS
import chats.cash.chats_field.utils.PreferenceUtilInterface
import chats.cash.chats_field.utils.hide
import chats.cash.chats_field.utils.network.NetworkStatusTracker
import chats.cash.chats_field.utils.safeNavigate
import chats.cash.chats_field.utils.show
import chats.cash.chats_field.views.auth.AuthActivity
import chats.cash.chats_field.views.auth.login.LoginDialog
import chats.cash.chats_field.views.auth.login.LoginViewModel
import chats.cash.chats_field.views.auth.ui.loadGlide
import chats.cash.chats_field.views.core.showErrorSnackbar
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

@OptIn(InternalCoroutinesApi::class)
class OnboardingFragment : Fragment(R.layout.fragment_onboarding) {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!
    private val viewModel by activityViewModel<LoginViewModel>()
    private val preferenceUtil: PreferenceUtilInterface by inject()

    private val internetAvailabilityChecker: NetworkStatusTracker by inject()
    val userProfile by lazy {
        viewModel.getUserProfile()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (preferenceUtil.getNGOId() == 0) {
            findNavController().safeNavigate(OnboardingFragmentDirections.actionOnboardingFragmentToLoginFragment())
        }
    }

    var count1 = 0
    var count2 = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentOnboardingBinding.bind(view)

        viewModel.getBeneficiaries.observe(viewLifecycleOwner) {
            count1 = it.size
            Timber.v("$count1")
            if (count1 > 0) {
                binding.offlineCard.show()
            } else if (count2 < 1) {
                binding.offlineCard.hide()
            }
        }

        viewModel.getGroupBeneficiaries.observe(viewLifecycleOwner) {
            count2 = it.size
            Timber.v("$count2")
            if (count2 > 0) {
                binding.offlineCard.show()
            } else if (count1 < 1) {
                binding.offlineCard.hide()
            }
        }

        binding.impactReport.setOnClickListener {
            findNavController().navigate(R.id.action_onboardingFragment_to_impactReportFragment)
        }

        binding.uploadOffline.setOnClickListener {
            lifecycleScope.launch {
                if (internetAvailabilityChecker.isNetworkAvailable()) {
                    lifecycleScope.launch {
                        (requireActivity() as AuthActivity).startUpload()
                    }
                } else {
                    showErrorSnackbar(R.string.no_internet, binding.root)
                }
            }
            lifecycleScope.launch {
                (requireActivity() as AuthActivity).uploading.collect {
                    if (it) {
                        binding.uploadingView.show()
                        binding.uploadOffline.hide()
                    } else {
                        binding.uploadingView.hide()
                        binding.uploadOffline.show()
                    }
                }
            }
        }

        setupClickListeners()

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS,
            )
        }

        binding.apply {
            binding.name.text = "${userProfile?.firstName} ${userProfile?.lastName}"
            userProfile?.profilePic?.let {
                binding.profile.loadGlide(it)
            }
            binding.profile.setOnClickListener {
                findNavController().safeNavigate(R.id.action_onboardingFragment_to_userProfileFragment)
            }
        }
    }

    private fun setupClickListeners() = with(binding) {
        cashForWork.setOnClickListener { openCashForWork() }
        beneficiaryOnboarding.setOnClickListener { openBeneficiaryOnboarding() }
        //  logoutBtn.setOnClickListener { doLogout() }
        vendorOnboarding.setOnClickListener { openVendorOnboarding() }
    }

//    private fun doLogout() {
//        preferenceUtil.clearPreference()
//        viewModel.clearAllTables()
//        binding.logoutBtn.hide()
//    }

    @OptIn(InternalCoroutinesApi::class)
    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    override fun onResume() {
        super.onResume()
    }

    companion object {
        val REQUIRED_PERMISSIONS =
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
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
            findNavController().safeNavigate(
                OnboardingFragmentDirections.toSelectCampaignFragment(true),
            )
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
            requireActivity().baseContext,
            it,
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun openLogin(isCancelable: Boolean = true) {
        val bottomSheetDialogFragment = LoginDialog.newInstance()
        bottomSheetDialogFragment.isCancelable = isCancelable
        bottomSheetDialogFragment.show(parentFragmentManager, "BottomSheetDialog")
    }
}
