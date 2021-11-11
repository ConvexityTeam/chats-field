package com.codose.chats.views.auth

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.codose.chats.R
import com.codose.chats.network.body.LocationBody
import com.codose.chats.offline.Beneficiary
import com.codose.chats.offline.OfflineViewModel
import com.codose.chats.utils.*
import com.codose.chats.utils.BluetoothConstants.VENDOR_TYPE
import com.codose.chats.utils.Utils.checkAppPermission
import com.codose.chats.views.auth.viewmodel.RegisterViewModel
import com.google.gson.Gson
import com.treebo.internetavailabilitychecker.InternetAvailabilityChecker
import com.treebo.internetavailabilitychecker.InternetConnectivityListener
import kotlinx.android.synthetic.main.activity_auth.*
import kotlinx.coroutines.InternalCoroutinesApi
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.io.File


@InternalCoroutinesApi
class AuthActivity : AppCompatActivity(), InternetConnectivityListener, ImageUploadCallback {
    private val offlineViewModel by viewModel<OfflineViewModel>()
    private val mainViewModel by viewModel<RegisterViewModel>()
    private lateinit var internetAvailabilityChecker: InternetAvailabilityChecker
    private var currentBeneficiary = Beneficiary()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        this.checkAppPermission()
        internetAvailabilityChecker = InternetAvailabilityChecker.getInstance()
        if(internetAvailabilityChecker.currentInternetAvailabilityStatus){
            startUpload()
        }
        internetAvailabilityChecker.addInternetConnectivityListener(this)
        mainViewModel.onboardUser.observe(this, {
            when (it) {
                is ApiResponse.Loading -> {
                    if(pendingUploadTextView.isVisible){
                        pendingProgress.show()
                    }
                }
                is ApiResponse.Success -> {
                    pendingProgress.hide()
                    try {
                        File(currentBeneficiary.leftLittle).delete()
                        File(currentBeneficiary.leftIndex).delete()
                        File(currentBeneficiary.leftThumb).delete()
                        File(currentBeneficiary.rightLittle).delete()
                        File(currentBeneficiary.rightIndex).delete()
                        File(currentBeneficiary.rightThumb).delete()
                        File(currentBeneficiary.profilePic).delete()
                    }catch (t : Throwable){

                    }
                    offlineViewModel.delete(beneficiary = currentBeneficiary)
                }
                is ApiResponse.Failure -> {
                    pendingProgress.hide()
                    if(it.code == 400){
                        try {
                            File(currentBeneficiary.leftLittle).delete()
                            File(currentBeneficiary.leftIndex).delete()
                            File(currentBeneficiary.leftThumb).delete()
                            File(currentBeneficiary.rightLittle).delete()
                            File(currentBeneficiary.rightIndex).delete()
                            File(currentBeneficiary.rightThumb).delete()
                            File(currentBeneficiary.profilePic).delete()
                        }catch (t : Throwable){

                        }
                        offlineViewModel.delete(beneficiary = currentBeneficiary)
                    }
                }
            }
        })

        offlineViewModel.getBeneficiaries.observe(this, {
            val count = it.count()
            pendingUploadTextView.text = "$count Pending uploads"
            if(count > 0){
                pendingUploadTextView.setOnClickListener {
                    if(internetAvailabilityChecker.currentInternetAvailabilityStatus){
                        startUpload()
                    }else{
                        this.toast("Internet not available.")
                    }
                }
            }else{
                pendingUploadTextView.setOnClickListener {
                    this.toast("No pending uploads")
                }
            }
        })
    }

    override fun onInternetConnectivityChanged(isConnected: Boolean) {
        if(isConnected){
            Timber.v("Internet connectivity available")
            startUpload()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        internetAvailabilityChecker.removeAllInternetConnectivityChangeListeners()
    }

    private fun startUpload() {
        offlineViewModel.getBeneficiaries.observe(this, {
            if(it.isNotEmpty()){
                Timber.v("List is not empty")
                if(currentBeneficiary != it.first()){
                    Timber.v("Initial = $currentBeneficiary")
                    currentBeneficiary = it.first()
                    Timber.v("Current = $currentBeneficiary")
                    if(currentBeneficiary.type == VENDOR_TYPE){
                        registerVendor(currentBeneficiary)
                    }else{
                        postOnboardData(beneficiary = currentBeneficiary)
                    }
                }else{
                    if(currentBeneficiary.type == VENDOR_TYPE){
                        registerVendor(currentBeneficiary)
                    }else{
                        postOnboardData(beneficiary = currentBeneficiary)
                    }
                }
            }
        })
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
//        val mOrganizationId = RequestBody.create("multipart/form-data".toMediaTypeOrNull(),beneficiary.organizationId.toString())
        val mProfilePic = beneficiary.profilePic.toFile()
        val profilePicBody = ProgressRequestBody(beneficiary.profilePic.toFile(), "image/jpg", this)

        val locationBody =
            LocationBody(coordinates = listOf(beneficiary.longitude, beneficiary.latitude),
                country = "Nigeria")
        val location = Gson().toJson(locationBody)
        val mLocation = location.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val mCampaign =
            beneficiary.campaignId.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val mNin =
            beneficiary.nin.toRequestBody("multipart/form-data".toMediaTypeOrNull())
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
            val mBody = ProgressRequestBody(f,"image/jpg",this)
            val finger = MultipartBody.Part.createFormData(
                "fingerprints",
                f.absolutePath.substringAfterLast("/"),
                mBody
            )
            prints.add(finger)
        }

        if(beneficiary.isSpecialCase){
            mainViewModel.onboardSpecialUser(firstName = mFirstName,
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
                nin = mNin)
        }else{
            mainViewModel.onboardUser(
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
                campaign = mCampaign
            )
        }
    }


    private fun registerVendor(currentBeneficiary: Beneficiary) {
        mainViewModel.vendorOnboarding (
            currentBeneficiary.storeName,
            currentBeneficiary.email,
            currentBeneficiary.phone,
            currentBeneficiary.password,
            currentBeneficiary.pin.toString(),
            currentBeneficiary.bvn,
            currentBeneficiary.firstName,
            currentBeneficiary.lastName
        )
    }

    override fun onProgressUpdate(percentage: Int) {
        Timber.v(percentage.toString())
    }

}