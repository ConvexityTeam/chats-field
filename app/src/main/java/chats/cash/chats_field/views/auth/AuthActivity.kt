package chats.cash.chats_field.views.auth

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import chats.cash.chats_field.databinding.ActivityAuthBinding
import chats.cash.chats_field.network.body.LocationBody
import chats.cash.chats_field.offline.Beneficiary
import chats.cash.chats_field.offline.OfflineViewModel
import chats.cash.chats_field.utils.*
import chats.cash.chats_field.utils.ChatsFieldConstants.VENDOR_TYPE
import chats.cash.chats_field.utils.Utils.checkAppPermission
import chats.cash.chats_field.views.auth.viewmodel.RegisterViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.treebo.internetavailabilitychecker.InternetAvailabilityChecker
import com.treebo.internetavailabilitychecker.InternetConnectivityListener
import kotlinx.coroutines.InternalCoroutinesApi
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.io.File

@InternalCoroutinesApi
class AuthActivity : AppCompatActivity(), InternetConnectivityListener, ImageUploadCallback {

    private lateinit var binding: ActivityAuthBinding
    private val offlineViewModel by viewModel<OfflineViewModel>()
    private val mainViewModel by viewModel<RegisterViewModel>()
    private lateinit var internetAvailabilityChecker: InternetAvailabilityChecker
    private var currentBeneficiary = Beneficiary()
    private val preferenceUtil: PreferenceUtil by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        this.checkAppPermission()

        val locationPermissionRequest =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                when {
                    permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false -> {
                        setupLocationProviderClient(this)
                    }
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                        setupLocationProviderClient(this)
                    }
                    else -> {
                        toast("Location access was rejected.")
                    }
                }
            }

        when {
            ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                setupLocationProviderClient(this)
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION) -> {

            }
            else -> {
                locationPermissionRequest.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION))
            }
        }

        internetAvailabilityChecker = InternetAvailabilityChecker.getInstance()
        if (internetAvailabilityChecker.currentInternetAvailabilityStatus) {
            startUpload()
        }
        internetAvailabilityChecker.addInternetConnectivityListener(this)
        mainViewModel.onboardUser.observe(this) {
            when (it) {
                is ApiResponse.Loading -> {
                    if (binding.pendingUploadTextView.isVisible) {
                        binding.pendingProgress.show()
                    }
                }
                is ApiResponse.Success -> {
                    binding.pendingProgress.hide()
                    try {
                        File(currentBeneficiary.leftLittle).delete()
                        File(currentBeneficiary.leftIndex).delete()
                        File(currentBeneficiary.leftThumb).delete()
                        File(currentBeneficiary.rightLittle).delete()
                        File(currentBeneficiary.rightIndex).delete()
                        File(currentBeneficiary.rightThumb).delete()
                        File(currentBeneficiary.profilePic).delete()
                    } catch (t: Throwable) {

                    }
                    offlineViewModel.delete(beneficiary = currentBeneficiary)
                }
                is ApiResponse.Failure -> {
                    binding.pendingProgress.hide()
                    if (it.code == 400) {
                        try {
                            File(currentBeneficiary.leftLittle).delete()
                            File(currentBeneficiary.leftIndex).delete()
                            File(currentBeneficiary.leftThumb).delete()
                            File(currentBeneficiary.rightLittle).delete()
                            File(currentBeneficiary.rightIndex).delete()
                            File(currentBeneficiary.rightThumb).delete()
                            File(currentBeneficiary.profilePic).delete()
                        } catch (t: Throwable) {

                        }
                        offlineViewModel.delete(beneficiary = currentBeneficiary)
                    }
                }
            }
        }

        offlineViewModel.getBeneficiaries.observe(this) {
            val count = it.count()
            binding.pendingUploadTextView.text = "$count Pending uploads"
            if (count > 0) {
                binding.pendingUploadTextView.setOnClickListener {
                    if (internetAvailabilityChecker.currentInternetAvailabilityStatus) {
                        startUpload()
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
            startUpload()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        internetAvailabilityChecker.removeAllInternetConnectivityChangeListeners()
    }

    private fun startUpload() {
        offlineViewModel.getBeneficiaries.observe(this) {
            if (it.isNotEmpty()) {
                Timber.v("List is not empty")
                if (currentBeneficiary != it.first()) {
                    Timber.v("Initial = $currentBeneficiary")
                    currentBeneficiary = it.first()
                    Timber.v("Current = $currentBeneficiary")
                    if (currentBeneficiary.type == VENDOR_TYPE) {
                        registerVendor(currentBeneficiary)
                    } else {
                        postOnboardData(beneficiary = currentBeneficiary)
                    }
                } else {
                    if (currentBeneficiary.type == VENDOR_TYPE) {
                        registerVendor(currentBeneficiary)
                    } else {
                        postOnboardData(beneficiary = currentBeneficiary)
                    }
                }
            }
        }
    }

    private fun postOnboardData(beneficiary: Beneficiary) {
        val mFirstName =
            beneficiary.firstName.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val mLastName =
            beneficiary.lastName.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val mEmail = beneficiary.email.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val mLatitude =
            beneficiary.latitude.toString().toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val mLongitude = beneficiary.longitude.toString()
            .toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val mPhone = beneficiary.phone.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val mPassword =
            beneficiary.password.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val mNfc = beneficiary.nfc.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val mStatus = 0.toString().toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val mGender = beneficiary.gender.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val mDate = beneficiary.date.toRequestBody("multipart/form-data".toMediaTypeOrNull())
//        val mOrganizationId = RequestBody.create("multipart/form-data".toMediaTypeOrNull(),beneficiary.id.toString())
        val mProfilePic = beneficiary.profilePic.toFile()
        val profilePicBody = ProgressRequestBody(beneficiary.profilePic.toFile(), "image/jpg", this)

        val locationBody =
            LocationBody(coordinates = listOf(beneficiary.longitude, beneficiary.latitude),
                country = "Nigeria")
        val location = Gson().toJson(locationBody)
        val mLocation = location.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val mCampaign =
            beneficiary.campaignId.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val mNin = beneficiary.nin
        val mPin =
            beneficiary.pin.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val mFingers = ArrayList<File>()
        if (!beneficiary.isSpecialCase) {
            mFingers.add(beneficiary.leftThumb.toFile())
            mFingers.add(beneficiary.leftIndex.toFile())
            mFingers.add(beneficiary.leftLittle.toFile())
            mFingers.add(beneficiary.rightThumb.toFile())
            mFingers.add(beneficiary.rightIndex.toFile())
            mFingers.add(beneficiary.rightLittle.toFile())
        }

        val prints = ArrayList<MultipartBody.Part>()

        mFingers.forEachIndexed { _, f ->
            val mBody = ProgressRequestBody(f, "image/jpg", this)
            val finger = MultipartBody.Part.createFormData(
                "fingerprints",
                f.absolutePath.substringAfterLast("/"),
                mBody
            )
            prints.add(finger)
        }

        if (beneficiary.isSpecialCase) {
            mainViewModel.onboardSpecialUser(
                beneficiary.id.toString(),
                firstName = mFirstName,
                lastName = mLastName,
                email = mEmail,
                phone = mPhone,
                password = mPassword,
                lat = mLatitude,
                long = mLongitude,
                nfc = mNfc,
                status = mStatus,
                profile_pic = mProfilePic,
                mGender = mGender,
                mDate = mDate,
                location = mLocation,
                campaign = mCampaign,
                pin = mPin,
                nin = mNin
            )
        } else {
            mainViewModel.onboardUser(
                beneficiary.id.toString(),
                firstName = mFirstName,
                lastName = mLastName,
                email = mEmail,
                phone = mPhone,
                password = mPassword,
                lat = mLatitude,
                long = mLongitude,
                nfc = mNfc,
                status = mStatus,
                profile_pic = mProfilePic,
                prints = prints,
                mGender = mGender,
                mDate = mDate,
                location = mLocation,
                campaign = mCampaign,
                pin = mPin
            )
        }
    }


    private fun registerVendor(currentBeneficiary: Beneficiary) {
        mainViewModel.vendorOnboarding(
            businessName = currentBeneficiary.storeName,
            email = currentBeneficiary.email,
            phone = currentBeneficiary.phone,
//            password = currentBeneficiary.password,
//            pin = currentBeneficiary.pin.toString(),
//            bvn = currentBeneficiary.bvn,
            firstName = currentBeneficiary.firstName,
            lastName = currentBeneficiary.lastName,
            address = currentBeneficiary.address,
            country = currentBeneficiary.country,
            state = currentBeneficiary.state,
            coordinates = listOf(preferenceUtil.getLatLong().first,
                preferenceUtil.getLatLong().second)
        )
    }

    override fun onProgressUpdate(percentage: Int) {
        Timber.v(percentage.toString())
    }

    private fun setupLocationProviderClient(context: Context) {
        try {
            val fusedLocationClient: FusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation.addOnCompleteListener { locationTask ->
                if (locationTask.isSuccessful) {
                    preferenceUtil.setLatLong(
                        latitude = locationTask.result.latitude,
                        longitude = locationTask.result.longitude
                    )
                } else {
                    toast(locationTask.exception?.localizedMessage)
                }
            }
        } catch (e: SecurityException) {
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }
}
