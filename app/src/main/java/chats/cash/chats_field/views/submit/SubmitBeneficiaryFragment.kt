package chats.cash.chats_field.views.submit

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.FragmentSubmitBeneficiaryBinding
import chats.cash.chats_field.model.ModelCampaign
import chats.cash.chats_field.network.NetworkResponse
import chats.cash.chats_field.offline.Beneficiary
import chats.cash.chats_field.offline.OfflineViewModel
import chats.cash.chats_field.utils.ChatsFieldConstants.BENEFICIARY_TYPE
import chats.cash.chats_field.utils.encryption.AES256Encrypt
import chats.cash.chats_field.utils.hide
import chats.cash.chats_field.utils.network.NetworkStatusTracker
import chats.cash.chats_field.utils.safeNavigate
import chats.cash.chats_field.utils.show
import chats.cash.chats_field.utils.writeBitmapToFile
import chats.cash.chats_field.views.auth.ui.NfcScanFragment
import chats.cash.chats_field.views.auth.viewmodel.RegisterViewModel
import chats.cash.chats_field.views.base.BaseFragment
import id.zelory.compressor.Compressor
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import timber.log.Timber
import java.io.File
import java.util.*

@InternalCoroutinesApi
class SubmitBeneficiaryFragment : BaseFragment() {

    private var profileImage: String? = null
    private var allFingers: List<Bitmap>? = null
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
    private var organizationId: Int = 0
    private val mViewModel by activityViewModel<RegisterViewModel>()
    private var beneficiary: Beneficiary? = null
    private val registerViewModel by activityViewModels<RegisterViewModel>()
    private val offlineViewModel by activityViewModels<OfflineViewModel>()

    private val internetAvailabilityChecker: NetworkStatusTracker by inject()

    private lateinit var binding: FragmentSubmitBeneficiaryBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSubmitBeneficiaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = registerViewModel.tempBeneficiary!!
        nfc = ""
        firstName = args.firstName
        lastname = args.lastName
        email = args.email
        campaign = registerViewModel.campaign!!
        password = args.password
        phone = args.phone
        lat = args.latitude.toString()
        long = args.longitude.toString()
        organizationId = args.id!!
        gender = args.gender
        date = args.date
        // pin = args.pin
        profileImage = registerViewModel.profileImage
        allFingers = registerViewModel.fingerPrintsScanned.value
        setObservers()
    }

    private fun setObservers() = lifecycleScope.launch {
        registerViewModel.onboardBeneficiaryResponse.collect {
            when (it) {
                is NetworkResponse.Loading -> {
                    binding.loadingGroup.show()
                    binding.saveForLater.hide()
                    binding.successsGroup.hide()
                    binding.errorGroup.hide()
                    binding.appbar.navigationIcon = null
                }

                is NetworkResponse.Success -> {
                    binding.loadingGroup.hide()
                    binding.successsGroup.show()
                    binding.saveForLater.hide()
                    binding.errorGroup.hide()
                    Timber.v(campaign.toString())
                    campaign.ck8?.let { ck8 ->
                        val encrypt = AES256Encrypt(ck8).encrypt("$email")
                        Timber.d(encrypt.toString())
                        Timber.d(AES256Encrypt(ck8).decrypt(encrypt).toString())
                        binding.continueButton.setOnClickListener {
                            encrypt?.let { it1 -> openNFCCardScanner(false, it1 + ",${campaign.id}") }
                        }
                    }

                    binding.successDesc.text = getString(R.string.text_user_onboarded_success)
                }

                is NetworkResponse.SimpleError -> {
                    binding.loadingGroup.hide()
                    binding.saveForLater.hide()
                    binding.successsGroup.hide()
                    binding.errorGroup.show()
                    binding.errorDesc.text = it._message

                    binding.errorHomeButton.setOnClickListener {
                        registerViewModel.resetOnboardState()

                        findNavController().safeNavigate(
                            R.id.to_onboardingFragmentSubmit,
                        )
                    }
                    binding.tryagainButton.setOnClickListener {
                        postOnboardData()
                    }
                    addAPpbar()
                }

                is NetworkResponse.Error -> {
                    binding.loadingGroup.hide()
                    binding.successsGroup.hide()
                    binding.saveForLater.hide()
                    binding.errorGroup.show()
                    binding.errorDesc.text = it._message

                    binding.errorHomeButton.setOnClickListener {
                        registerViewModel.resetOnboardState()
                        findNavController().safeNavigate(
                            R.id.to_onboardingFragmentSubmit,
                        )
                    }
                    binding.tryagainButton.setOnClickListener {
                        postOnboardData()
                    }
                    addAPpbar()
                }

                is NetworkResponse.Offline -> {
                    binding.loadingGroup.hide()
                    binding.successsGroup.show()
                    binding.saveForLater.hide()
                    binding.errorGroup.hide()
                    binding.successDesc.text = getString(R.string.no_internet)
                    campaign.ck8?.let { ck8 ->
                        val encrypt = AES256Encrypt(ck8).encrypt("$email")
                        Timber.d(encrypt.toString())
                        Timber.d(AES256Encrypt(ck8).decrypt(encrypt).toString())
                        offlineViewModel.insert(beneficiary!!)
                        binding.continueButton.setOnClickListener {
                            encrypt?.let { it1 ->
                                openNFCCardScanner(true, it1 + ",${campaign.id}")
                            }
                        }
                    }
                    binding.appbar.navigationIcon = null
                }

                is NetworkResponse.NetworkError -> {
                    binding.loadingGroup.hide()
                    binding.successsGroup.hide()
                    binding.errorGroup.show()
                    binding.errorDesc.text = getString(R.string.pleas_check_your_network_connection_make_sure_you_re_connected_to_a_good_network)
                    binding.saveForLater.apply {
                        show()
                        setOnClickListener {
                            registerViewModel.saveForOffline(beneficiary)
                            findNavController().safeNavigate(R.id.to_onboardingFragmentSubmit)
                        }
                    }

                    binding.errorHomeButton.setOnClickListener {
                        registerViewModel.resetOnboardState()
                        findNavController().safeNavigate(
                            R.id.to_onboardingFragmentSubmit,
                        )
                    }
                    binding.tryagainButton.setOnClickListener {
                        postOnboardData()
                    }
                    addAPpbar()
                }

                else -> {
                    postOnboardData()
                    binding.appbar.navigationIcon = null
                }
            }
        }
    }

    private fun addAPpbar() {
        binding.appbar.navigationIcon =
            ResourcesCompat.getDrawable(resources, R.drawable.ic_back, requireActivity().theme)
        binding.appbar.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun postOnboardData() = lifecycleScope.launch {
        val profilePic =
            async {
                return@async Compressor.compress(
                    requireContext(),
                    File(profileImage ?: ""),
                ).path
            }
        beneficiary = Beneficiary(
            id = organizationId,
            firstName = firstName.trim(),
            lastName = lastname.trim(),
            email = email.trim(),
            phone = phone.trim(),
            longitude = long.toDouble(),
            latitude = lat.toDouble(),
            password = password,
            iris = mViewModel.iris,
            nfc = nfc!!,
            profilePic = profilePic.await(),
            gender = gender.lowercase().trim(),
            date = date,
            campaignId = campaign.id.toString(),
            //pin = pin,
            nin = mViewModel.nin.trim(),
            isSpecialCase = mViewModel.specialCase,
            type = BENEFICIARY_TYPE,

        )

        if (!mViewModel.specialCase && allFingers.isNullOrEmpty().not()) {
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
            beneficiary!!,
            internetAvailabilityChecker.isNetworkAvailable(),
        )
    }

    private fun openNFCCardScanner(isOffline: Boolean, data: String) {
        val bottomSheetDialogFragment = NfcScanFragment.newInstance(isOffline, data)
        bottomSheetDialogFragment.isCancelable = false
        this.setFragmentResultListener(NFC_REQUEST_KEY) { _, _ ->
            registerViewModel.resetOnboardState()
            findNavController().safeNavigate(
                R.id.to_onboardingFragmentSubmit,
            )
        }
        bottomSheetDialogFragment.show(
            parentFragmentManager.beginTransaction(),
            "BottomSheetDialog",
        )
    }
}

const val NFC_REQUEST_KEY = "7080"
