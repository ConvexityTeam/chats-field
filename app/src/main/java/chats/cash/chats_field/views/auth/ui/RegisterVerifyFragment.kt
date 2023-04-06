package chats.cash.chats_field.views.auth.ui

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.FragmentRegisterVerifyBinding
import chats.cash.chats_field.model.ModelCampaign
import chats.cash.chats_field.network.NetworkResponse
import chats.cash.chats_field.network.body.LocationBody
import chats.cash.chats_field.network.repository.OFFLINE_RESPONSE
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
import id.zelory.compressor.Compressor
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
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
    private val mViewModel by activityViewModels<RegisterViewModel>()
    private var beneficiary: Beneficiary? = null
    private val registerViewModel by activityViewModels<RegisterViewModel>()
    private val offlineViewModel by activityViewModels<OfflineViewModel>()
    private lateinit var internetAvailabilityChecker: InternetAvailabilityChecker

    private lateinit var binding:FragmentRegisterVerifyBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentRegisterVerifyBinding.inflate(inflater,container,false)
        return binding.root
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
        binding.backBtn.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.checkImage.hide()
        if (mViewModel.specialCase) {
            binding.verifyPrintCard.hide()
        }
        internetAvailabilityChecker = InternetAvailabilityChecker.getInstance()
        if (mViewModel.profileImage != null) {
            profileImage = mViewModel.profileImage
            binding.checkImage.show()
        } else {
            binding.checkImage.hide()
        }

        if (mViewModel.allFinger != null) {
            allFingers = mViewModel.allFinger
            binding.checkPrint.show()
        } else {
            binding.checkPrint.hide()
        }

        if (mViewModel.allFinger != null) {
            binding.checkPrint.show()
        } else {
            binding.checkPrint.hide()
        }

        if (mViewModel.specialCase) {
            if (profileImage != null) {
               binding.completedImageIcon.setImageResource(R.drawable.ic_check)
            } else {
               binding.completedImageIcon.setImageResource(R.drawable.ic_uncheck)
            }
        } else {
            if (allFingers != null && profileImage != null) {
               binding.completedImageIcon.setImageResource(R.drawable.ic_check)
            } else {
               binding.completedImageIcon.setImageResource(R.drawable.ic_uncheck)
            }
        }


        binding.registerVerifyBtn.setOnClickListener {

            if (mViewModel.specialCase) {
                if (profileImage != null) {
                    binding.registerVerifyBtn.isEnabled=false
                    binding.verifyProgress.show()
                        postOnboardData()
                } else {
                    showToast("All fields are required")
                }
            } else {
                if (allFingers != null && profileImage != null) {
                    binding.registerVerifyBtn.isEnabled=false
                    binding.verifyProgress.show()
                    postOnboardData()
                } else {
                    showToast("All fields are required")
                }
            }
        }



        binding.verifyPrintCard.setOnClickListener {
            findNavController().safeNavigate(RegisterVerifyFragmentDirections.toRegisterPrintFragment())
        }
        binding.pictureCard.setOnClickListener {
            findNavController().safeNavigate(RegisterVerifyFragmentDirections.toRegisterImageFragment())
        }

        setObservers()

    }

    private fun setObservers() =lifecycleScope.launch{
        registerViewModel.onboardBeneficiaryResponse.collect {
            when (it) {
                is NetworkResponse.Loading -> {
                    binding.verifyProgress.show()
                    binding. registerVerifyBtn.isEnabled = false
                }
                is NetworkResponse.Success -> {
                    binding.registerVerifyBtn.isEnabled=true
                    binding.verifyProgress.hide()
                    val data = it.body

                        campaign.ck8?.let {
                            val encrypt = AES256Encrypt(it).encrypt(email)
                            Timber.d(encrypt.toString())
                            Timber.d(AES256Encrypt(it).decrypt(encrypt).toString())
                            encrypt?.let { it1 -> openNFCCardScanner(false, it1) }
                        }
                }
                is NetworkResponse.SimpleError -> {
                    showSnackbarWithAction(
                        it._message, binding.root, R.string.dismiss,
                        ContextCompat.getColor(requireContext(), R.color.design_default_color_error)
                    ) {
                        findNavController().safeNavigate(RegisterVerifyFragmentDirections.toOnboardingFragment())
                    }
                    binding. verifyProgress.hide()
                    binding.registerVerifyBtn.isEnabled = false
                }
                is NetworkResponse.Error -> {
                    showSnackbarWithAction(
                        it.e.message?:"Error", binding.root, R.string.dismiss,
                        ContextCompat.getColor(requireContext(), R.color.design_default_color_error)
                    ) {
                        findNavController().safeNavigate(RegisterVerifyFragmentDirections.toOnboardingFragment())
                    }
                    binding. verifyProgress.hide()
                    binding.registerVerifyBtn.isEnabled = false
                }
                is NetworkResponse.Offline -> {
                    campaign.ck8?.let {
                        val encrypt = AES256Encrypt(it).encrypt(email)
                        Timber.d(encrypt.toString())
                        Timber.d(AES256Encrypt(it).decrypt(encrypt).toString())
                        offlineViewModel.insert(beneficiary!!)
                        showSnackbarWithAction(
                            R.string.no_internet, binding.root, R.string.dismiss,
                            ContextCompat.getColor(requireContext(), R.color.colorPrimary)
                        ) {
                            encrypt?.let { it1 ->
                                openNFCCardScanner(true, it1)
                            }

                        }
                    }
                }
            }
        }


    }

    private fun postOnboardData() =lifecycleScope.launch{
        val profilePic =  async {  return@async Compressor.compress(requireContext(), File(profileImage!!)).path}
        beneficiary = Beneficiary(
            id = organizationId,
            firstName = firstName,
            lastName = lastname,
            email = email,
            phone = phone,
            longitude = long.toDouble(),
            latitude = lat.toDouble(),
            password = password,
            nfc = nfc!!,
            profilePic =profilePic.await(),
            gender = gender,
            date = date,
            campaignId = campaign.id.toString(),
            pin = pin,
            nin = mViewModel.nin,
            isSpecialCase = mViewModel.specialCase,
            type = BENEFICIARY_TYPE

        )

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
            val mBody = ProgressRequestBody(f, "image/jpg", object:ImageUploadCallback{
                override fun onProgressUpdate(percentage: Int) {

                }

            })
            val finger = MultipartBody.Part.createFormData(
                "fingerprints",
                f.absolutePath.substringAfterLast("/"),
                mBody
            )
            prints.add(finger)
        }
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


        registerViewModel.onboardBeneficiary(
            beneficiary!!,internetAvailabilityChecker.currentInternetAvailabilityStatus,
        )


    }

    override fun onProgressUpdate(percentage: Int) {
        Timber.v("Percentage for upload: $percentage")
    }


    private fun openNFCCardScanner(isOffline: Boolean, emails: String) {
        val bottomSheetDialogFragment = NfcScanFragment.newInstance(isOffline, emails)
        bottomSheetDialogFragment.isCancelable = false
        this.setFragmentResultListener(NFC_REQUEST_KEY) { string, bundle ->
            findNavController().safeNavigate(RegisterVerifyFragmentDirections.toOnboardingFragment())
            if(!isOffline){
                showSuccessSnackbar(
                    R.string.text_user_onboarded_success,
                    binding.root
                )
            }
        }
        bottomSheetDialogFragment.show(
            parentFragmentManager.beginTransaction(),
            "BottomSheetDialog"
        )

    }
}


const val NFC_REQUEST_KEY = "7080"