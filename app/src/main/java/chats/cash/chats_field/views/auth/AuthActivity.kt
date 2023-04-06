package chats.cash.chats_field.views.auth

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.ActivityAuthBinding
import chats.cash.chats_field.network.NetworkResponse
import chats.cash.chats_field.offline.Beneficiary
import chats.cash.chats_field.offline.OfflineViewModel
import chats.cash.chats_field.utils.*
import chats.cash.chats_field.utils.ChatsFieldConstants.BENEFICIARY_TYPE
import chats.cash.chats_field.utils.ChatsFieldConstants.VENDOR_TYPE
import chats.cash.chats_field.utils.dialogs.AlertDialog
import chats.cash.chats_field.utils.location.LocationManager
import chats.cash.chats_field.utils.permissions.POST_NOTIFICATION_PERMISSION_REQUEST_CODE
import chats.cash.chats_field.utils.permissions.RequestNotificationPermission
import chats.cash.chats_field.views.auth.ui.NFC_REQUEST_KEY
import chats.cash.chats_field.views.auth.ui.NfcScanFragment
import chats.cash.chats_field.views.auth.ui.RegisterVerifyFragmentDirections
import chats.cash.chats_field.views.auth.viewmodel.RegisterViewModel
import chats.cash.chats_field.views.core.dialogs.getPermissionDialogs
import chats.cash.chats_field.views.core.permissions.*
import chats.cash.chats_field.views.core.showErrorSnackbar
import chats.cash.chats_field.views.core.showSuccessSnackbar
import com.google.android.gms.tasks.CancellationTokenSource
import com.treebo.internetavailabilitychecker.InternetAvailabilityChecker
import com.treebo.internetavailabilitychecker.InternetConnectivityListener
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.cancellable
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.io.FileNotFoundException
import kotlin.coroutines.resume


@InternalCoroutinesApi
class AuthActivity : AppCompatActivity(), InternetConnectivityListener, ImageUploadCallback, PermissionResultReceiver {

    private  var _binding: ActivityAuthBinding?=null
    private  val binding: ActivityAuthBinding
        get() = _binding!!
    private val offlineViewModel by viewModel<OfflineViewModel>()
    val permissionManager: PermissionManager = PermissionManager(this,this)
    private val mainViewModel by viewModel<RegisterViewModel>()
    private lateinit var internetAvailabilityChecker: InternetAvailabilityChecker
    private val preferenceUtil: PreferenceUtil by inject()

    private val cancellationTokenSource by lazy { CancellationTokenSource() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(_binding!!.root)
        permissionListener()
        RequestNotificationPermission(this).onCreate()
        val locationManager = LocationManager(this)
        lifecycleScope.launch {
            locationManager.getLastKnownLocationAsync().await()?.let {
                preferenceUtil.setLatLong(
                    latitude = it.latitude,
                    longitude = it.longitude
                )
            }?: toast("Location access was rejected.")
        }

        internetAvailabilityChecker = InternetAvailabilityChecker.getInstance()
        if (internetAvailabilityChecker.currentInternetAvailabilityStatus) {
            lifecycleScope.launch {
                startUpload()
            }
        }
        internetAvailabilityChecker.addInternetConnectivityListener(this)


        offlineViewModel.getBeneficiaries.observe(this) {
            val count = it.count()
            binding.pendingUploadTextView.text = "$count Pending uploads"
            if (count > 0) {
                binding.pendingUploadTextView.setOnClickListener {
                    if (internetAvailabilityChecker.currentInternetAvailabilityStatus) {
                        lifecycleScope.launch {
                            startUpload()
                        }
                    } else {
                        this.toast("Internet not available.")
                    }
                }
            } else {
                binding.pendingUploadTextView.setOnClickListener {
                    this.toast("No pending uploads")
                }
            }
        }
    }

    override fun onInternetConnectivityChanged(isConnected: Boolean) {
        if (isConnected) {
            Timber.v("Internet connectivity available")
        }
    }

    override fun onResume() {
        super.onResume()
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
            if(!permissionsList.contains(WRITE_STORAGE_PERMISSION) || !permissionsList.contains(
                    READ_STORAGE_PERMISSION) ) {
                permissionsList.addAll(listOf(WRITE_STORAGE_PERMISSION,READ_STORAGE_PERMISSION))
            }
        }
        if(!alertDialog.isShowing && !cameraRationale.isShowing){
            permissionManager.checkPermissions(permissionsList)
        }
    }
private val permissionsList= mutableListOf(CAMERA_PERMISSION, FINE_LOCATION)
    private fun permissionListener(){
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment) as NavHostFragment
        val navController = navHostFragment.navController
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
            if(permissionsList.contains(WRITE_STORAGE_PERMISSION) || !permissionsList.contains(
                    READ_STORAGE_PERMISSION)) {
                permissionsList.addAll(listOf(WRITE_STORAGE_PERMISSION,READ_STORAGE_PERMISSION))
            }
        }
        navController.addOnDestinationChangedListener{_,_,_ ->
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
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show()
                }
                return
            }
        }
    }


    private suspend fun uploadBeneficiaryAsync(beneficiary: Beneficiary):Boolean=suspendCancellableCoroutine { continuation ->
        mainViewModel.onboardBeneficiary(beneficiary,internetAvailabilityChecker.currentInternetAvailabilityStatus)
        try {
            CoroutineScope(Dispatchers.Main).launch {
                mainViewModel.onboardBeneficiaryResponse.cancellable().collect {
                    handleUploadResponse(it,beneficiary,continuation,this)

                }
            }
        }
        catch (e:Exception){
            e.printStackTrace()
        }

    }

    private var uploading=false
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
        if(index==(list.size-1)){
            showSuccessSnackbar(R.string.text_user_onboarded_success,binding.root)
        }

        index += 1


    }
    private suspend fun startUpload() {
        offlineViewModel.getBeneficiaries.observe(this) {list ->
            if(!uploading) {
                CoroutineScope(Dispatchers.IO).launch {
                    uploading = true
                    while(index <=(list.size-1)){
                        val beneficiary = list[index]
                        uploadUser(beneficiary, list)
                    }
                    uploading = false
                }
            }
        }
    }



    private suspend fun registerVendor(beneficiary: Beneficiary):Boolean=suspendCancellableCoroutine { continuation ->
        mainViewModel.vendorOnboarding(beneficiary,
            internetAvailabilityChecker.currentInternetAvailabilityStatus
        )
        try {
            CoroutineScope(Dispatchers.Main).launch {
                mainViewModel.onboardVendorResponse.cancellable().collect {
                    handleUploadResponse(it,beneficiary,continuation,this)
                }
            }
        }
        catch (e:Exception){
            e.printStackTrace()
        }

    }

    private fun handleUploadResponse(it:NetworkResponse<Any>,beneficiary: Beneficiary,
                                     continuation:CancellableContinuation<Boolean>,
                                     job:CoroutineScope){
        Timber.v("upoading $beneficiary $it")
        binding.pendingProgress.hide()
        if (it is NetworkResponse.Success) {
            Timber.v("success")
            offlineViewModel.delete(beneficiary)
            continuation.resume(true)
            job.cancel()
        } else if (it is NetworkResponse.Error) {
            showErrorSnackbar(R.string.upload_failed, binding.root)
            Timber.v((it)._message)
            if (it._message.contains("400")) {
                Timber.v("deleting")
                offlineViewModel.delete(beneficiary)
            }
            if (it.e is FileNotFoundException) {
                offlineViewModel.delete(beneficiary)
                showErrorSnackbar(R.string.file_deleted, binding.root)
            }
            continuation.resume(false)
            job.cancel()
        } else if (it is NetworkResponse.SimpleError) {
            showErrorSnackbar(R.string.upload_failed, binding.root)
            Timber.v((it)._message)
            if (it._message.contains("400")) {
                Timber.v("deleting")
                offlineViewModel.delete(beneficiary)
            } else {
                when (it.code) {
                    in 400..499 -> {
                        Timber.v("deleting")
                        offlineViewModel.delete(beneficiary)
                    }
                }
            }
            continuation.resume(false)
            job.cancel()
        } else {
            if (binding.pendingUploadTextView.isVisible) {
                binding.pendingProgress.show()
            }
        }

    }

    override fun onProgressUpdate(percentage: Int) {
        Timber.v(percentage.toString())
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    fun hidePendingUpload() {
        _binding?.let {
            binding.pendingUploadTextView.hide()
        }
    }

    fun showPendingUpload() {
        _binding?.let {
            binding.pendingUploadTextView.show()
        }
    }

    override fun onGranted() {

    }

    override fun notGranted(permission: String) {
        if(permissionManager.shouldShowRationale(permission)){
            showRationale(permission)
        }
        else {
             if(!checkPermission(CAMERA_PERMISSION) && !checkPermission(FINE_LOCATION) && !checkPermission(
                    READ_STORAGE_PERMISSION)) {
                permissionManager.getPermissions(listOf(CAMERA_PERMISSION, FINE_LOCATION, COARSE_LOCATION, READ_STORAGE_PERMISSION))
            }
            else if(permission == FINE_LOCATION){
                permissionManager.getPermissions(listOf(FINE_LOCATION, COARSE_LOCATION))
            }
            else {
                permissionManager.getPermission(permission)
            }
        }
    }
    private val alertDialog by lazy {
        AlertDialog(this,
            "Permission denied",
            "Permission was denied permanently, please grant us permission",
            onNegativeClicked = {
                finish()
            },
            onPostiveClicked = {
                openAppSystemSettings()
            }).create()
    }
        override fun onDenied(permission: String) {
            if (!alertDialog.isShowing) {
                alertDialog.show()
                Timber.v(permission)
            }
        }


    private val cameraRationale by lazy {
        getPermissionDialogs(desc = R.string.camera_permission_desc,
            icon = R.drawable.camera_permission_icon, onDismiss = {
                finish()
            }) {
            permissionManager.getPermission(CAMERA_PERMISSION)
        }
    }
    override fun showRationale(permission: String) {
        when (permission) {
            CAMERA_PERMISSION -> {
                if(!cameraRationale.isShowing){
                    cameraRationale.show()
                }
            }
            READ_STORAGE_PERMISSION -> {

            }

            WRITE_STORAGE_PERMISSION -> {

            }
        }
    }

}
