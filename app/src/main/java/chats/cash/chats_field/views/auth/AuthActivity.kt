package chats.cash.chats_field.views.auth

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.ActivityAuthBinding
import chats.cash.chats_field.network.NetworkResponse
import chats.cash.chats_field.offline.Beneficiary
import chats.cash.chats_field.offline.OfflineViewModel
import chats.cash.chats_field.utils.*
import chats.cash.chats_field.utils.ChatsFieldConstants.BENEFICIARY_TYPE
import chats.cash.chats_field.utils.ChatsFieldConstants.VENDOR_TYPE
import chats.cash.chats_field.utils.Utils.checkAppPermission
import chats.cash.chats_field.utils.location.LocationManager
import chats.cash.chats_field.utils.permissions.POST_NOTIFICATION_PERMISSION_REQUEST_CODE
import chats.cash.chats_field.utils.permissions.RequestNotificationPermission
import chats.cash.chats_field.views.auth.viewmodel.RegisterViewModel
import chats.cash.chats_field.views.core.showErrorSnackbar
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.treebo.internetavailabilitychecker.InternetAvailabilityChecker
import com.treebo.internetavailabilitychecker.InternetConnectivityListener
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collect
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.io.FileNotFoundException
import kotlin.coroutines.resume


@OptIn(ExperimentalCoroutinesApi::class)
@InternalCoroutinesApi
class AuthActivity : AppCompatActivity(), InternetConnectivityListener, ImageUploadCallback {

    private  var _binding: ActivityAuthBinding?=null
    private  val binding: ActivityAuthBinding
        get() = _binding!!
    private val offlineViewModel by viewModel<OfflineViewModel>()
    private val mainViewModel by viewModel<RegisterViewModel>()
    private lateinit var internetAvailabilityChecker: InternetAvailabilityChecker
    private val preferenceUtil: PreferenceUtil by inject()

    private val cancellationTokenSource by lazy { CancellationTokenSource() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(_binding!!.root)

        this.checkAppPermission()

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
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {

                } else {
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

    private suspend fun uploadUser(beneficiary: Beneficiary) {
        Timber.v("index $index")
        when (beneficiary.type) {
            VENDOR_TYPE -> {
                registerVendor(beneficiary)
                index += 1
            }
            BENEFICIARY_TYPE -> {
                uploadBeneficiaryAsync(beneficiary)
                index += 1
            }
        }

    }
    private suspend fun startUpload() {
        offlineViewModel.getBeneficiaries.observe(this) {list ->
            if(!uploading) {
                CoroutineScope(Dispatchers.IO).launch {
                    uploading = true
                    while(index <=(list.size-1)){
                            val beneficiary = list[index]
                            uploadUser(beneficiary)
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

    private fun setupLocationProviderClient(context: Context) {
        try {
            val fusedLocationClient: FusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY,
                cancellationTokenSource.token)
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        preferenceUtil.setLatLong(
                            latitude = location.latitude,
                            longitude = location.longitude
                        )
                    } else {
                        toast("Location is turned off on this device")
                    }
                }.addOnFailureListener {
                    toast(it.localizedMessage)
                    FirebaseCrashlytics.getInstance().recordException(it)
            }
        } catch (e: SecurityException) {
            toast(e.localizedMessage)
            Timber.e(e)
            FirebaseCrashlytics.getInstance().recordException(e)
        } catch (t: Throwable) {
            Timber.e(t)
            FirebaseCrashlytics.getInstance().recordException(t)
        }
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
}
