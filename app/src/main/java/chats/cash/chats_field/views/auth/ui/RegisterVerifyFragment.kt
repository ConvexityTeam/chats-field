package chats.cash.chats_field.views.auth.ui

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import chats.cash.chats_field.R
import chats.cash.chats_field.model.ModelCampaign
import chats.cash.chats_field.network.body.LocationBody
import chats.cash.chats_field.offline.Beneficiary
import chats.cash.chats_field.offline.OfflineViewModel
import chats.cash.chats_field.utils.*
import chats.cash.chats_field.utils.ChatsFieldConstants.BENEFICIARY_TYPE
import chats.cash.chats_field.utils.encryption.AES256Encrypt
import chats.cash.chats_field.views.auth.viewmodel.RegisterViewModel
import chats.cash.chats_field.views.base.BaseFragment
import chats.cash.chats_field.views.core.showSnackbarWithAction
import chats.cash.chats_field.views.core.showSuccessSnackbar
import com.google.gson.Gson
import com.treebo.internetavailabilitychecker.InternetAvailabilityChecker
import kotlinx.android.synthetic.main.fragment_register_verify.*
import kotlinx.coroutines.InternalCoroutinesApi
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.io.File
import java.util.*

@InternalCoroutinesApi
class RegisterVerifyFragment : BaseFragment(), ImageUploadCallback {

    private var profileImage: String? = null
    private var allFingers: ArrayList<Bitmap>? = null
    private var nfc: String? = null
    private lateinit var firstName: String
    private lateinit var campaign: ModelCampaign
    private lateinit var lastname: String
    private lateinit var email: String
    private lateinit var password: String
    private lateinit var phone: String
    private lateinit var lat: String
    private lateinit var long: String
    private lateinit var gender: String
    private lateinit var date: String
    private lateinit var pin: String
    private var organizationId: Int = 0
    private val mViewModel by sharedViewModel<RegisterViewModel>()
    private var beneficiary: Beneficiary? = null
    private val registerViewModel by viewModel<RegisterViewModel>()
    private val offlineViewModel by viewModel<OfflineViewModel>()
    private lateinit var internetAvailabilityChecker: InternetAvailabilityChecker

    private var encryptedEmail: String? = null

    private var userId = 0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register_verify, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = RegisterVerifyFragmentArgs.fromBundle(requireArguments())
        nfc = ""
        firstName = args.firstName
        lastname = args.lastName
        email = args.email
        campaign = args.campaign
        password = args.password
        phone = args.phone
        lat = args.latitude
        long = args.longitude
        organizationId = args.organizationId
        gender = args.gender
        date = args.date
        pin = args.pin
        back_btn.setOnClickListener {
            findNavController().navigateUp()
        }
        check_image.hide()
        if (mViewModel.specialCase) {
            verifyPrintCard.hide()
        }
        internetAvailabilityChecker = InternetAvailabilityChecker.getInstance()
        if (mViewModel.profileImage != null) {
            profileImage = mViewModel.profileImage
            check_image.show()
        } else {
            check_image.hide()
        }

        if (mViewModel.allFinger != null) {
            allFingers = mViewModel.allFinger
            check_print.show()
        } else {
            check_print.hide()
        }

        if (mViewModel.allFinger != null) {
            check_print.show()
        } else {
            check_print.hide()
        }

        if (mViewModel.specialCase) {
            if (profileImage != null) {
                completedImageIcon.setImageResource(R.drawable.ic_check)
            } else {
                completedImageIcon.setImageResource(R.drawable.ic_uncheck)
            }
        } else {
            if (allFingers != null && profileImage != null) {
                completedImageIcon.setImageResource(R.drawable.ic_check)
            } else {
                completedImageIcon.setImageResource(R.drawable.ic_uncheck)
            }
        }


        registerVerifyBtn.setOnClickListener {
            if (mViewModel.specialCase) {
                if (profileImage != null) {
                    if (internetAvailabilityChecker.currentInternetAvailabilityStatus.not()) {

                        postOnboardData()

                    } else {
                        postOnboardData()
                    }
                } else {
                    showToast("All fields are required")
                }
            } else {
                if (allFingers != null && profileImage != null) {
                    if (!internetAvailabilityChecker.currentInternetAvailabilityStatus) {
                        if (registerViewModel.nfc == null) {

                            postOnboardData()
                        }
                    } else {
                        postOnboardData()
                    }
                } else {
                    showToast("All fields are required")
                }
            }
        }



        verifyPrintCard.setOnClickListener {
            findNavController().safeNavigate(RegisterVerifyFragmentDirections.toRegisterPrintFragment())
        }
        pictureCard.setOnClickListener {
            findNavController().safeNavigate(RegisterVerifyFragmentDirections.toRegisterImageFragment())
        }

        setObservers()

    }

    private fun setObservers() {
        registerViewModel.onboardUser.observe(viewLifecycleOwner) {
            when (it) {
                is ApiResponse.Loading -> {
                    verifyProgress.show()
                    registerVerifyBtn.isEnabled = false
                }
                is ApiResponse.Success -> {

                    campaign.ck8?.let {
                        val encrypt = AES256Encrypt(it).encrypt(email)
                        Timber.d(encrypt.toString())
                        Timber.d(AES256Encrypt(it).decrypt(encrypt).toString())
                        showSuccessSnackbar(
                            R.string.text_user_onboarded_success,
                            this.requireView()
                        )
                        encrypt?.let { it1 -> openNFCCardScanner(false, it1) }
                    }

                    verifyProgress.hide()
                    registerVerifyBtn.isEnabled = true
                }
                is ApiResponse.Failure -> {
                    showSnackbarWithAction(
                        it.message, this.requireView(), R.string.dismiss,
                        ContextCompat.getColor(requireContext(), R.color.design_default_color_error)
                    ) {
                        findNavController().safeNavigate(RegisterVerifyFragmentDirections.toOnboardingFragment())
                    }
                    verifyProgress.hide()
                    registerVerifyBtn.isEnabled = false
                }
            }
        }


    }

    private fun postOnboardData() {
        val mFirstName = firstName.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val mLastName = lastname.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val mEmail = email.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val mLatitude = lat.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val mLongitude = long.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val mPhone = phone.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val mPassword = password.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val mNfc = nfc!!.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val mStatus = 5.toString().toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val mGender =
            gender.lowercase(Locale.ROOT).toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val mDate = date.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val mOrganizationId =
            organizationId.toString().toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val mProfilePic = File(profileImage!!)
        val locationBody =
            LocationBody(coordinates = listOf(long.toDouble(), lat.toDouble()), country = "Nigeria")
        val location = Gson().toJson(locationBody)
        val mLocation = location.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val mCampaign = mViewModel.campaign.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val mNin = mViewModel.nin
        val mPin = pin.toRequestBody("multipart/form-data".toMediaTypeOrNull())

        val mFingers = ArrayList<File>()

        allFingers?.forEachIndexed { index, bitmap ->
            val f = bitmap.toFile(
                requireContext(),
                "finger_print" + System.currentTimeMillis() + "$index" + ".png"
            )
            mFingers.add(f)
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
        beneficiary = Beneficiary()
        beneficiary!!.type = BENEFICIARY_TYPE
        beneficiary!!.firstName = firstName
        beneficiary!!.lastName = lastname
        beneficiary!!.email = email
        beneficiary!!.phone = phone
        beneficiary!!.password = password
        beneficiary!!.gender = gender.lowercase()
        beneficiary!!.date = date
        beneficiary!!.latitude = lat.toDouble()
        beneficiary!!.longitude = long.toDouble()
        beneficiary!!.nfc = nfc!!
        beneficiary!!.profilePic = profileImage!!
        beneficiary!!.isSpecialCase = mViewModel.specialCase
        beneficiary!!.nin = mViewModel.nin
        beneficiary!!.pin = pin
        if (!mViewModel.specialCase) {
            beneficiary!!.leftThumb =
                writeBitmapToFile(requireContext(), allFingers!![0]).absolutePath
            beneficiary!!.leftIndex =
                writeBitmapToFile(requireContext(), allFingers!![1]).absolutePath
            beneficiary!!.leftLittle =
                writeBitmapToFile(requireContext(), allFingers!![2]).absolutePath
            beneficiary!!.rightThumb =
                writeBitmapToFile(requireContext(), allFingers!![3]).absolutePath
            beneficiary!!.rightIndex =
                writeBitmapToFile(requireContext(), allFingers!![4]).absolutePath
            beneficiary!!.rightLittle =
                writeBitmapToFile(requireContext(), allFingers!![5]).absolutePath
        }

        if (internetAvailabilityChecker.currentInternetAvailabilityStatus) {
            if (mViewModel.specialCase) {
                registerViewModel.onboardSpecialUser(
                    organizationId.toString(),
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
                    nin = mNin,
                    pin = mPin
                )
            } else {
                registerViewModel.onboardUser(
                    organizationId.toString(),
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
        } else {

            campaign.ck8?.let {
                val encrypt = AES256Encrypt(it).encrypt(email)
                Timber.d(encrypt.toString())
                Timber.d(AES256Encrypt(it).decrypt(encrypt).toString())
                offlineViewModel.insert(beneficiary!!)
                showSnackbarWithAction(
                    R.string.no_internet, this.requireView(), R.string.dismiss,
                    ContextCompat.getColor(requireContext(), R.color.colorPrimary)
                ) {
                    encrypt?.let { it1 ->
                        openNFCCardScanner(true, it1)
                    }

                }
            }
        }
    }

    override fun onProgressUpdate(percentage: Int) {
        Timber.v("Percentage for upload: $percentage")
    }


    private fun openNFCCardScanner(isOffline: Boolean, emails: String) {
        val bottomSheetDialogFragment = NfcScanFragment.newInstance(isOffline, emails)
        bottomSheetDialogFragment.isCancelable = false
        this.setFragmentResultListener(NFC_REQUEST_KEY) { string, bundle ->
            findNavController().safeNavigate(RegisterVerifyFragmentDirections.toOnboardingFragment())
            Timber.v("On Result")
        }
        bottomSheetDialogFragment.show(
            parentFragmentManager.beginTransaction(),
            "BottomSheetDialog"
        )

    }
}

const val NFC_REQUEST_KEY = "7080"