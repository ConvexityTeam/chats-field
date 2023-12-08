package chats.cash.chats_field.views.auth

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.ActivityAuthBinding
import chats.cash.chats_field.network.NetworkResponse
import chats.cash.chats_field.network.body.groupBeneficiary.GroupBeneficiaryBody
import chats.cash.chats_field.offline.Beneficiary
import chats.cash.chats_field.offline.OfflineViewModel
import chats.cash.chats_field.utils.ChatsFieldConstants
import chats.cash.chats_field.utils.ChatsFieldConstants.BENEFICIARY_TYPE
import chats.cash.chats_field.utils.ChatsFieldConstants.VENDOR_TYPE
import chats.cash.chats_field.utils.ImageUploadCallback
import chats.cash.chats_field.utils.PreferenceUtil
import chats.cash.chats_field.utils.PreferenceUtilInterface
import chats.cash.chats_field.utils.dialogs.AlertDialog
import chats.cash.chats_field.utils.location.LocationManager
import chats.cash.chats_field.utils.onClearListener
import chats.cash.chats_field.utils.permissions.POST_NOTIFICATION_PERMISSION_REQUEST_CODE
import chats.cash.chats_field.utils.permissions.RequestNotificationPermission
import chats.cash.chats_field.utils.show
import chats.cash.chats_field.utils.toast
import chats.cash.chats_field.views.auth.viewmodel.RegisterViewModel
import chats.cash.chats_field.views.core.dialogs.getPermissionDialogs
import chats.cash.chats_field.views.core.permissions.CAMERA_PERMISSION
import chats.cash.chats_field.views.core.permissions.COARSE_LOCATION
import chats.cash.chats_field.views.core.permissions.FINE_LOCATION
import chats.cash.chats_field.views.core.permissions.PermissionManager
import chats.cash.chats_field.views.core.permissions.PermissionResultReceiver
import chats.cash.chats_field.views.core.permissions.READ_STORAGE_PERMISSION
import chats.cash.chats_field.views.core.permissions.WRITE_STORAGE_PERMISSION
import chats.cash.chats_field.views.core.permissions.checkPermission
import chats.cash.chats_field.views.core.permissions.openAppSystemSettings
import chats.cash.chats_field.views.core.showErrorSnackbar
import chats.cash.chats_field.views.core.showSuccessSnackbar
import com.google.android.gms.tasks.CancellationTokenSource
import com.treebo.internetavailabilitychecker.InternetAvailabilityChecker
import com.treebo.internetavailabilitychecker.InternetConnectivityListener
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import retrofit2.HttpException
import timber.log.Timber
import java.io.FileNotFoundException
import kotlin.coroutines.resume

@InternalCoroutinesApi
class AuthActivity :
    OnSharedPreferenceChangeListener,
    AppCompatActivity(),
    InternetConnectivityListener,
    ImageUploadCallback,
    PermissionResultReceiver,
    onClearListener {

    private var _binding: ActivityAuthBinding? = null
    private val binding: ActivityAuthBinding
        get() = _binding!!
    private val offlineViewModel by viewModel<OfflineViewModel>()
    val permissionManager: PermissionManager = PermissionManager(this, this)
    private val mainViewModel by viewModel<RegisterViewModel>()
    private lateinit var internetAvailabilityChecker: InternetAvailabilityChecker
    private val preferenceUtil: PreferenceUtilInterface by inject()

    private val cancellationTokenSource by lazy { CancellationTokenSource() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(_binding!!.root)
        permissionListener()
        RequestNotificationPermission(this).onCreate()
        val locationManager = LocationManager(this)
        lifecycleScope.launch {
            preferenceUtil.setClearListner(this@AuthActivity)
            locationManager.getLastKnownLocationAsync().await()?.let {
                preferenceUtil.setLatLong(
                    latitude = it.latitude,
                    longitude = it.longitude,
                )
            } ?: toast(getString(R.string.location_access_was_rejected))
        }

        internetAvailabilityChecker = InternetAvailabilityChecker.getInstance()
        if (internetAvailabilityChecker.currentInternetAvailabilityStatus) {
            lifecycleScope.launch {
                startUpload()
            }
        }
        internetAvailabilityChecker.addInternetConnectivityListener(this)
    }

//    private fun openNFCCardScanner(isOffline: Boolean, data: String) {
//        val bottomSheetDialogFragment = NfcScanFragment.newInstance(isOffline, data)
//        bottomSheetDialogFragment.isCancelable = false
////        this.setFragmentResultListener(NFC_REQUEST_KEY) { _, _ ->
////            registerViewModel.resetOnboardState()
////            findNavController().safeNavigate(
////                R.id.to_onboardingFragmentSubmit,
////            )
////        }
//        bottomSheetDialogFragment.show(
//            supportFragmentManager.beginTransaction(),
//            "BottomSheetDialog",
//        )
//    }

    override fun onInternetConnectivityChanged(isConnected: Boolean) {
        if (isConnected) {
            Timber.v("Internet connectivity available")
        }
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (!permissionsList.contains(WRITE_STORAGE_PERMISSION) || !permissionsList.contains(
                    READ_STORAGE_PERMISSION,
                )
            ) {
                permissionsList.addAll(listOf(WRITE_STORAGE_PERMISSION, READ_STORAGE_PERMISSION))
            }
        }
        if (!alertDialog.isShowing && !cameraRationale.isShowing) {
            permissionManager.checkPermissions(permissionsList)
        }

        addSharedPreferenceListener()
    }

    override fun onPause() {
        super.onPause()
        removeSharedPreferenceListener()
    }

    private val permissionsList = mutableListOf(CAMERA_PERMISSION, FINE_LOCATION)

    private fun getNavController(): NavController {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment) as NavHostFragment
        return navHostFragment.navController
    }

    private fun permissionListener() {
        val navController = getNavController()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (permissionsList.contains(WRITE_STORAGE_PERMISSION) || !permissionsList.contains(
                    READ_STORAGE_PERMISSION,
                )
            ) {
                permissionsList.addAll(listOf(WRITE_STORAGE_PERMISSION, READ_STORAGE_PERMISSION))
            }
        }
        navController.addOnDestinationChangedListener { _, _, _ ->
            permissionManager.checkPermissions(permissionsList)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        internetAvailabilityChecker.removeAllInternetConnectivityChangeListeners()
        cancellationTokenSource.cancel()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            POST_NOTIFICATION_PERMISSION_REQUEST_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isEmpty() &&
                    grantResults[0] != PackageManager.PERMISSION_GRANTED
                ) {
                    Toast.makeText(
                        this,
                        getString(R.string.permission_denied),
                        Toast.LENGTH_LONG,
                    ).show()
                }
                return
            }
        }
    }

    private suspend fun uploadBeneficiaryAsync(beneficiary: Beneficiary): Boolean =
        suspendCancellableCoroutine { continuation ->
            mainViewModel.onboardBeneficiary(
                beneficiary,
                internetAvailabilityChecker.currentInternetAvailabilityStatus,
            )
            try {
                CoroutineScope(Dispatchers.Main).launch {
                    mainViewModel.onboardBeneficiaryResponse.collect {
                        if (it != null) {
                            handleUploadResponse(it, beneficiary, continuation, this)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                uploading.value = false
            }
        }

    private suspend fun uploadGroupBeneficiaryAsync(beneficiary: GroupBeneficiaryBody): Boolean =
        suspendCancellableCoroutine { continuation ->
            mainViewModel.onboardGroupBeneficiary(
                beneficiary,
                internetAvailabilityChecker.currentInternetAvailabilityStatus,
            )
            try {
                CoroutineScope(Dispatchers.Main).launch {
                    mainViewModel.onboardBeneficiaryResponse.collect {
                        if (it != null) {
                            handleUploadResponse(it, beneficiary, continuation, this)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                uploading.value = false
            }
        }

    val uploading = MutableStateFlow(false)
    private var index = 0

    private suspend fun uploadUser(beneficiary: Beneficiary, list: List<Beneficiary>) {
        Timber.v("index $index")
        when (beneficiary.type) {
            VENDOR_TYPE -> {
                registerVendor(beneficiary)
            }

            BENEFICIARY_TYPE -> {
                uploadBeneficiaryAsync(beneficiary)
            }
        }
        if (index == (list.size - 1)) {
            uploading.value = false
        }
        index += 1
    }

    private suspend fun uploadGroupUser(
        beneficiary: GroupBeneficiaryBody,
        list: List<GroupBeneficiaryBody>,
    ) {
        Timber.v("index $index")
        uploadGroupBeneficiaryAsync(beneficiary)

        if (index == (list.size - 1)) {
            uploading.value = false
        }
        index += 1
    }

    var size = 0

    suspend fun startUpload() {
        offlineViewModel.getBeneficiaries.observe(this) { list ->
            if (list.isNullOrEmpty().not()) {
                if (!uploading.value) {
                    CoroutineScope(Dispatchers.IO).launch {
                        uploading.value = true
                        size = list.size
                        index = 0
                        while (index <= (list.size - 1)) {
                            val beneficiary = list[index]
                            if (beneficiary.isGroup.not()) {
                                uploadUser(beneficiary, list)
                            }
                        }
                        uploading.value = false
                        startUploadGroup()
                    }
                }
            } else {
                startUploadGroup()
            }
        }
    }

    private fun startUploadGroup() {
        lifecycleScope.launch {
            offlineViewModel.getGroupBeneficiaries.observe(this@AuthActivity) { list ->
                if (!uploading.value) {
                    CoroutineScope(Dispatchers.IO).launch {
                        uploading.value = true
                        size = list.size
                        index = 0
                        while (index <= (list.size - 1)) {
                            val beneficiary = list[index]
                            uploadGroupUser(beneficiary, list)
                        }

                        uploading.value = false
                    }
                }
            }
        }
    }

    private suspend fun registerVendor(beneficiary: Beneficiary): Boolean =
        suspendCancellableCoroutine { continuation ->
            mainViewModel.vendorOnboarding(
                beneficiary,
                internetAvailabilityChecker.currentInternetAvailabilityStatus,
            )
            try {
                CoroutineScope(Dispatchers.Main).launch {
                    mainViewModel.onboardVendorResponse.cancellable().collect {
                        handleUploadResponse(it, beneficiary, continuation, this)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                uploading.value = false
            }
        }

    private fun <T> deleteBeneficiary(beneficiary: T) {
        if (beneficiary is Beneficiary) {
            offlineViewModel.delete(beneficiary)
        } else if (beneficiary is GroupBeneficiaryBody) {
            offlineViewModel.delete(beneficiary)
        }
    }

    private fun <T> handleUploadResponse(
        it: NetworkResponse<Any>,
        beneficiary: T,
        continuation: CancellableContinuation<Boolean>,
        job: CoroutineScope,
    ) {
        Timber.tag("UPLOAD").v("upoading $beneficiary $it")

        if (it is NetworkResponse.Success) {
            Timber.v("success")
            deleteBeneficiary(beneficiary)
            if (index == (size - 1)) {
                showSuccessSnackbar(R.string.text_user_onboarded_success, binding.root)
            }
            continuation.resume(true)
            job.cancel()
        } else if (it is NetworkResponse.Error) {
            showErrorSnackbar(R.string.upload_failed, binding.root)
            Timber.v((it)._message)
            if (it._message.startsWith("4")) {
                Timber.v("deleting")
                deleteBeneficiary(beneficiary)
            } else if (it.e is FileNotFoundException) {
                deleteBeneficiary(beneficiary)
                showErrorSnackbar(R.string.file_deleted, binding.root)
            } else if (it.e is HttpException) {
                Timber.v("deleting")
                deleteBeneficiary(beneficiary)
                showErrorSnackbar(it._message, binding.root)
            } else {
                showErrorSnackbar(R.string.upload_failed, binding.root)
                Timber.v("deleting")
                deleteBeneficiary(beneficiary)
            }
            continuation.resume(false)
            job.cancel()
        } else if (it is NetworkResponse.SimpleError) {
            showErrorSnackbar(R.string.upload_failed, binding.root)
            Timber.v((it)._message)
            if (it._message.startsWith("4")) {
                Timber.v("deleting")
                deleteBeneficiary(beneficiary)
            } else {
                when (it.code) {
                    in 400..499 -> {
                        Timber.v("deleting")
                        deleteBeneficiary(beneficiary)
                    }

                    else -> {
                        Timber.v("deleting")
                        deleteBeneficiary(beneficiary)
                    }
                }
            }
            continuation.resume(false)
            job.cancel()
        } else {
        }
    }

    override fun onProgressUpdate(percentage: Int) {
        Timber.v(percentage.toString())
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onGranted() {
    }

    override fun notGranted(permission: String) {
        if (permissionManager.shouldShowRationale(permission)) {
            showRationale(permission)
        } else {
            if (!checkPermission(CAMERA_PERMISSION) && !checkPermission(FINE_LOCATION) && !checkPermission(
                    READ_STORAGE_PERMISSION,
                )
            ) {
                permissionManager.getPermissions(
                    listOf(
                        CAMERA_PERMISSION,
                        FINE_LOCATION,
                        COARSE_LOCATION,
                        READ_STORAGE_PERMISSION,
                    ),
                )
            } else if (permission == FINE_LOCATION) {
                permissionManager.getPermissions(listOf(FINE_LOCATION, COARSE_LOCATION))
            } else {
                permissionManager.getPermission(permission)
            }
        }
    }

    private val alertDialog by lazy {
        AlertDialog(
            this,
            getString(R.string.permission_denied),
            getString(R.string.permission_was_denied_permanently_please_grant_us_permission),
            onNegativeClicked = {
                finish()
            },
            onPostiveClicked = {
                openAppSystemSettings()
            },
        ).create()
    }

    override fun onDenied(permission: String) {
        if (!alertDialog.isShowing) {
            alertDialog.show()
            Timber.v(permission)
        }
    }

    private val cameraRationale by lazy {
        getPermissionDialogs(
            desc = R.string.camera_permission_desc,
            icon = R.drawable.camera_permission_icon,
            onDismiss = {
                finish()
            },
        ) {
            permissionManager.getPermission(CAMERA_PERMISSION)
        }
    }

    override fun showRationale(permission: String) {
        when (permission) {
            CAMERA_PERMISSION -> {
                if (!cameraRationale.isShowing) {
                    cameraRationale.show()
                }
            }

            READ_STORAGE_PERMISSION -> {
            }

            WRITE_STORAGE_PERMISSION -> {
            }
        }
    }

    private fun addSharedPreferenceListener() {
        val pref =
            getSharedPreferences(ChatsFieldConstants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)
        pref.registerOnSharedPreferenceChangeListener(this)
    }

    private fun removeSharedPreferenceListener() {
        val pref =
            getSharedPreferences(ChatsFieldConstants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)
        pref.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        Timber.v(key)
        if (key == PreferenceUtil.NGO_TOKEN) {
            val token = sharedPreferences?.getString(PreferenceUtil.NGO_TOKEN, "")
            if (token.isNullOrEmpty()) {
                val navController = getNavController()
                val startDestinationId: Int = navController.graph.startDestinationId
                Timber.tag("NAVIGATING").v(key)
                Timber.tag("STARTDESTINATION").v(startDestinationId.toString())
                navController.popBackStack(R.id.onboardingFragment, true)
                navController.navigate(R.id.loginFragment)
            }
        }
    }

    override fun onClear() {
        val navController = getNavController()
        Timber.tag("NAVIGATING").v("cleared")
        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                //   navController.popBackStack()
                navController.popBackStack(R.id.onboardingFragment, true)
                //   navController.popBackStack()
                navController.navigate(R.id.loginFragment)
            }
        }
    }
}
