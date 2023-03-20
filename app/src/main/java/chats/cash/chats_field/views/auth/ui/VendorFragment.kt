package chats.cash.chats_field.views.auth.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.FragmentVendorBinding
import chats.cash.chats_field.offline.Beneficiary
import chats.cash.chats_field.offline.OfflineViewModel
import chats.cash.chats_field.utils.*
import chats.cash.chats_field.utils.ChatsFieldConstants.VENDOR_TYPE
import chats.cash.chats_field.utils.Utils.toCountryCode
import chats.cash.chats_field.views.auth.login.LoginDialog
import chats.cash.chats_field.views.auth.viewmodel.RegisterViewModel
import chats.cash.chats_field.views.base.BaseFragment
import com.treebo.internetavailabilitychecker.InternetAvailabilityChecker
import kotlinx.coroutines.InternalCoroutinesApi
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

@InternalCoroutinesApi
class VendorFragment : BaseFragment() {

    private val beneficiary: Beneficiary = Beneficiary()
    private val registerViewModel by viewModel<RegisterViewModel>()
    private val offlineViewModel by viewModel<OfflineViewModel>()
    private lateinit var internetAvailabilityChecker: InternetAvailabilityChecker
    private val preferenceUtil: PreferenceUtil by inject()

    private lateinit var binding:FragmentVendorBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentVendorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loggedInText2.show()
        binding.changeAccountText2.show()

        binding.backBtn.setOnClickListener {
            findNavController().navigateUp()
        }

        internetAvailabilityChecker = InternetAvailabilityChecker.getInstance()

        binding.changeAccountText2.setOnClickListener {
            openLogin(true)
        }

        binding.vendorNextButton.setOnClickListener {
            checkInputs()
        }

        setObservers()
    }

    private fun setObservers() {
        registerViewModel.vendorOnboardingState.observe(viewLifecycleOwner) {
            when (it) {
                is RegisterViewModel.VendorOnboardingState.Error -> {
                    binding.vendorProgress.hide()
                    showToast(it.errorMessage)
                    binding.vendorNextButton.isEnabled = true
                }
                RegisterViewModel.VendorOnboardingState.Loading -> {
                    binding.vendorProgress.show()
                    binding.vendorNextButton.isEnabled = false
                }
                RegisterViewModel.VendorOnboardingState.Success -> {
                    binding.vendorProgress.hide()
                    binding.vendorNextButton.isEnabled = true
                    showToast("Success")
                    findNavController().navigateUp()
                }
            }
        }
    }

    private fun checkInputs() {
        val firstName = binding.vendorFirstNameEdit.text.toString()
        val lastName = binding.vendorLastNameEdit.text.toString()
        val businessName = binding.vendorBusinessNameEdit.text.toString()
        val email = binding.vendorEmailEdit.text.toString()
        val phone = binding.vendorPhoneEdit.text.toString()
        val password = Utils.generatePassword()
        val pin = Utils.generatePIN()
        val bvn = binding.vendorBvnEdit.text.toString()
        val address = binding.vendorAddressEdit.text.toString()
        val country = binding.vendorCountryEdit.text.toString()
        val state = binding.vendorStateEdit.text.toString()

        if (binding.vendorFirstNameEdit.text.isNullOrBlank()) {
           binding.vendorFirstNameLayout.error = "First name is required"
            return
        } else {
            binding.vendorFirstNameLayout.error = ""
        }

        if (binding.vendorLastNameEdit.text.isNullOrBlank()) {
            binding.vendorLastNameLayout.error = "Last name is required"
            return
        } else {
            binding.vendorLastNameLayout.error = ""
        }

        if (binding.vendorBusinessNameEdit.text.isNullOrBlank()) {
            binding.vendorBusinessNameEdit.error = "Business name is required"
            return
        } else {
            binding.vendorBusinessNameEdit.error = ""
        }

        if (binding.vendorEmailEdit.text.isNullOrBlank()) {
            binding.vendorEmailLayout.error = "Email Address is required"
            return
        } else {
            binding.vendorEmailLayout.error = ""
        }



        if (binding.vendorPhoneEdit.text.isNullOrBlank()) {
            binding.vendorPhoneLayout.error = "Phone number is required"
            return
        } else {
            if (binding.vendorPhoneEdit.text.toString().length != 11) {
                binding.vendorPhoneLayout.error = "Invalid Phone Number"
                return
            }
            binding.vendorPhoneLayout.error = ""
        }

        if (binding.vendorBvnEdit.text.isNullOrBlank()) {
            binding.vendorBvnLayout.error = "BVN is required"
            return
        } else {
            if (binding.vendorBvnEdit.text.toString().length in 10..11) {
                binding.vendorBvnLayout.error = ""
            } else {
                binding.vendorBvnLayout.error = "Invalid BVN"
                return
            }
        }

        beneficiary.storeName = businessName
        beneficiary.email = email
        beneficiary.phone = phone.toCountryCode()
        beneficiary.password = password
        beneficiary.pin = pin
        beneficiary.bvn = bvn
        beneficiary.firstName = firstName
        beneficiary.lastName = lastName
//        beneficiary.organizationId = PrefUtils.getNGOId()
        beneficiary.type = VENDOR_TYPE
        beneficiary.address = address
        beneficiary.country = country
        beneficiary.state = state

        if (internetAvailabilityChecker.currentInternetAvailabilityStatus) {
            registerViewModel.vendorOnboarding(
                businessName = businessName,
                email = email,
                phone = phone.toCountryCode(),
                firstName = firstName,
                lastName = lastName,
                address = address,
                country = country,
                state = state,
                coordinates = listOf(preferenceUtil.getLatLong().first, preferenceUtil.getLatLong().second)
            )
        } else {
            offlineViewModel.insert(beneficiary)
            showToast(getString(R.string.no_internet))
            findNavController().navigateUp()
        }
    }

    private fun openLogin(isCancelable: Boolean = true) {
        val bottomSheetDialogFragment = LoginDialog.newInstance()
        bottomSheetDialogFragment.isCancelable = isCancelable
        bottomSheetDialogFragment.show(parentFragmentManager, "BottomSheetDialog")
    }
}