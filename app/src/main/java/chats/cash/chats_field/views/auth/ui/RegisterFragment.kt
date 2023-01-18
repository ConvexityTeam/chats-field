package chats.cash.chats_field.views.auth.ui

import android.Manifest
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.FragmentRegisterBinding
import chats.cash.chats_field.model.ModelCampaign
import chats.cash.chats_field.utils.*
import chats.cash.chats_field.utils.Utils.toCountryCode
import chats.cash.chats_field.views.auth.login.LoginDialog
import chats.cash.chats_field.views.auth.viewmodel.RegisterViewModel
import kotlinx.coroutines.InternalCoroutinesApi
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber
import java.util.*

@InternalCoroutinesApi
class RegisterFragment : Fragment(R.layout.fragment_register) {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val preferenceUtil: PreferenceUtil by inject()
    private val organizationId: Int by lazy { preferenceUtil.getNGOId() }
    private val viewModel by sharedViewModel<RegisterViewModel>()
    private val myCalendar: Calendar = Calendar.getInstance()
    private var campaign: ModelCampaign? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setFragmentResultListener(ChatsFieldConstants.FRAGMENT_CAMPAIGN_RESULT_LISTENER) { _, bundle ->
            campaign = bundle.getParcelable(ChatsFieldConstants.CAMPAIGN_BUNDLE_KEY)
            binding.registerCampaignEdit.setText(campaign?.title)
        }
    }

    private fun openNFCCardScanner(isOffline: Boolean,email:String) {
        val bottomSheetDialogFragment = NfcScanFragment.newInstance(isOffline,email)
        bottomSheetDialogFragment.isCancelable = isOffline
        bottomSheetDialogFragment.setTargetFragment(this, 7080)
        bottomSheetDialogFragment.show(requireFragmentManager().beginTransaction(),
            "BottomSheetDialog")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRegisterBinding.bind(view)

        openNFCCardScanner(true,"${System.currentTimeMillis()}@gmail.com")

        viewModel.nfc = null
        viewModel.allFinger = null
        viewModel.profileImage = null
        viewModel.getAllCampaigns()

        binding.run {
            backBtn.setOnClickListener {
                findNavController().navigateUp()
            }
            registerCampaignEdit.setOnClickListener {
                findNavController().safeNavigate(RegisterFragmentDirections.toCampaignDialog())
            }
            registerCampaignLayout.setEndIconOnClickListener {
                findNavController().safeNavigate(RegisterFragmentDirections.toCampaignDialog())
            }
            val adapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_selectable_list_item, listOf("Male", "Female"))
            registerGenderEdit.setAdapter(adapter)

            observeLoginDone()

            val specialAdapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_selectable_list_item, listOf("No", "Yes"))
            registerSpecialCaseEdit.setAdapter(specialAdapter)
            registerNINLayout.hide()
            txtNin.hide()
            registerSpecialCaseEdit.setOnItemClickListener { _, _, position, _ ->
                if (position == 0) {
                    registerNINLayout.hide()
                    txtNin.hide()
                } else {
                    registerNINLayout.show()
                    txtNin.show()
                }
            }

            changeAccountText.setOnClickListener {
                openLogin(true)
                doLogout()
            }
            if (organizationId == 0) {
                openLogin()
                changeLoggedOutText()
            } else {
                changeLoggedInText()
            }
            registerDateEdit.setOnClickListener {
                openCalendar()
            }
            if (ActivityCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            ) {
                findNavController().navigateUp()
                showToast("Location permission is required.")
                return
            }
            registerNextButton.setOnClickListener {
                checkInputs()
            }
        }
    }

    private fun observeLoginDone() {
        viewModel.login.observe(viewLifecycleOwner) {
            Timber.v("XXXlogin vn reached ")
            when (it) {
                is ApiResponse.Success -> {
                    Timber.v("XXXlogin vn reached ")
                    findNavController().safeNavigate(R.id.action_registerFragment_to_onboardingFragment)
                }
                else -> {}
            }
        }
    }

    private fun changeLoggedInText() = with(binding) {
        loggedInText.text = "Logged In "
        changeAccountText.text = "Change Account? "
    }

    private fun changeLoggedOutText() = with(binding) {
        loggedInText.text = "Not Logged In? "
        changeAccountText.text = "Log In"
    }

    private fun checkInputs() = with(binding) {
        val firstName: String
        val lastName: String
        val email: String
        val phone: String
        val date: String
        val pin: String
        if (organizationId == 0) {
            showToast("Please Log In")
            openLogin()
            return
        }
        if (registerFirstNameEdit.isValid()) {
            firstName = registerFirstNameEdit.text.toString()
            registerFirstNameLayout.error = ""
        } else {
            registerFirstNameLayout.error = "First name is required"
            return
        }
        if (registerLastNameEdit.isValid()) {
            registerLastNameLayout.error = ""
            lastName = registerLastNameEdit.text.toString()
        } else {
            registerLastNameLayout.error = "Last name is required"
            return
        }
        if (registerEmailEdit.text.isNullOrBlank()) {
            email = "${System.currentTimeMillis()}@gmail.com"
            registerEmailLayout.error = ""
        } else {
            if (registerEmailEdit.text.toString().isEmailValid()) {
                registerEmailLayout.error = ""
                email = registerEmailEdit.text.toString()
            } else {
                registerEmailLayout.error = "Invalid email address"
                return@with
            }
        }
        if (registerPhoneEdit.isValid() && registerPhoneEdit.text.toString().isValidPhoneNo()) {
            registerPhoneLayout.error = ""
            phone = registerPhoneEdit.text.toString()
        } else {
            registerPhoneLayout.error = "Phone number is required"
            return
        }
        if (inputPinEdit.isValid() && inputPinEdit.text.toString().isValidPin()) {
            inputPinLayout.error = ""
            pin = inputPinEdit.text.toString()
        } else {
            inputPinLayout.error = "A 4-digit PIN is required"
            return
        }
        if (registerDateEdit.isValid()) {
            registerDateLayout.error = ""
            date = registerDateEdit.text.toString()
        } else {
            registerDateLayout.error = "Date of birth is required"
            return
        }

        if (campaign?.id == null) {
            registerCampaignLayout.error = "Select a campaign"
            return
        } else {
            registerCampaignLayout.error = ""
        }
        val gender = registerGenderEdit.text.toString()
        if (gender.isBlank()) {
            registerGenderLayout.error = "Gender is required"
            return
        } else {
            registerGenderLayout.error = ""
        }
        val password: String = Utils.generatePassword()
        Timber.v(password)
        campaign?.let { viewModel.campaign = it.id.toString() }

        val isSpecialCase = registerSpecialCaseEdit.text.toString() == "Yes"
        viewModel.specialCase = isSpecialCase

        if (isSpecialCase) {
            when {
                registerNINEdit.text.isNullOrBlank() -> {
                    showToast("NIN is required.")
                    return
                }
                registerNINEdit.text.toString().length in 10..16 -> {
                    viewModel.nin = registerNINEdit.text.toString()
                }
                else -> {
                    showToast("NIN is not valid")
                    return
                }
            }
        }
        findNavController().safeNavigate(RegisterFragmentDirections.toRegisterVerifyFragment(
            firstName = firstName,
            lastName = lastName,
            email = email,
            phone = phone.toCountryCode(),
            password = password,
            latitude = preferenceUtil.getLatLong().first.toString(),
            longitude = preferenceUtil.getLatLong().second.toString(),
            organizationId = organizationId,
            gender = gender,
            date = date,
            pin = pin
        ))
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (preferenceUtil.getNGOId() == 0) {
            openLogin()
            changeLoggedOutText()
        } else {
            changeLoggedInText()
        }
    }

    private fun openCalendar() {
        val date =
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                myCalendar[Calendar.YEAR] = year
                myCalendar[Calendar.MONTH] = monthOfYear
                myCalendar[Calendar.DAY_OF_MONTH] = dayOfMonth
                updateLabel()
            }
        val datePicker = DatePickerDialog(
            requireContext(), date, myCalendar[Calendar.YEAR], myCalendar[Calendar.MONTH],
            myCalendar[Calendar.DAY_OF_MONTH]
        )
        datePicker.setButton(DialogInterface.BUTTON_POSITIVE, "OK", datePicker)
        datePicker.setButton(DialogInterface.BUTTON_NEGATIVE, "CANCEL", datePicker)
        datePicker.datePicker.maxDate = System.currentTimeMillis()

        datePicker.show()
    }

    private fun updateLabel() {
        val date = myCalendar.time
        binding.registerDateEdit.setText(date.convertDateToString())
    }

    private fun openLogin(isCancelable: Boolean = true) {
        val bottomSheetDialogFragment = LoginDialog.newInstance()
        bottomSheetDialogFragment.isCancelable = isCancelable
        bottomSheetDialogFragment.show(parentFragmentManager, "BottomSheetDialog")
    }

    private fun doLogout() {
        preferenceUtil.clearPreference()
        changeLoggedOutText()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
