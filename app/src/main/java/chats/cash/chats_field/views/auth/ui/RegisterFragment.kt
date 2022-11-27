package chats.cash.chats_field.views.auth.ui

import android.Manifest
import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
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
import com.robin.locationgetter.EasyLocation
import kotlinx.coroutines.InternalCoroutinesApi
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber
import java.util.*

@InternalCoroutinesApi
class RegisterFragment : Fragment(R.layout.fragment_register) {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private lateinit var locationManager: LocationManager
    private var latitude: Double = 6.465422
    private var longitude: Double = 3.406448
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRegisterBinding.bind(view)

        locationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
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
                ArrayAdapter(requireContext(), R.layout.spinner_drop_down, listOf("Male", "Female"))
            registerGenderEdit.setAdapter(adapter)

            observeLoginDone()

            val specialAdapter =
                ArrayAdapter(requireContext(), R.layout.spinner_drop_down, listOf("No", "Yes"))
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
            try {
                initLocation()
            } catch (t: Throwable) {
                showToast("Please Ensure that the Device GPS is turned on")
                findNavController().navigateUp()
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

    private fun initLocation() {
        EasyLocation(requireActivity(), object : EasyLocation.EasyLocationCallBack {
            override fun permissionDenied() {

            }

            override fun locationSettingFailed() {


            }

            override fun getLocation(location: Location) {
                latitude = location.latitude
                longitude = location.longitude
            }
        })
    }

    private fun checkInputs() = with(binding) {
        val firstName: String
        val lastName: String
        val email: String
        val phone: String
        val date: String
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
        if (registerPhoneEdit.isValid()) {
            registerPhoneLayout.error = ""
            phone = registerPhoneEdit.text.toString()
        } else {
            registerPhoneLayout.error = "Phone number is required"
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
        val gender: String = registerGenderEdit.text.toString()
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
                registerNINEdit.text.toString().length in 10..18 -> {
                    viewModel.nin = registerNINEdit.text.toString()
                }
                else -> {
                    showToast("NIN not valid")
                    return
                }
            }
        }
        findNavController().safeNavigate(RegisterFragmentDirections.toRegisterVerifyFragment(
            firstName,
            lastName,
            email,
            phone.toCountryCode(),
            password,
            latitude.toString(),
            longitude.toString(),
            organizationId,
            gender,
            date
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
