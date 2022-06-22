package com.codose.chats.views.auth.ui

import android.Manifest
import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.codose.chats.R
import com.codose.chats.model.ModelCampaign
import com.codose.chats.network.response.organization.campaign.Campaign
import com.codose.chats.offline.OfflineRepository
import com.codose.chats.utils.*
import com.codose.chats.views.auth.dialog.LoginDialog
import com.codose.chats.views.auth.viewmodel.RegisterViewModel
import com.codose.chats.views.base.BaseFragment
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.robin.locationgetter.EasyLocation
import kotlinx.android.synthetic.main.dialog_login.*
import kotlinx.android.synthetic.main.fragment_register.*
import kotlinx.coroutines.InternalCoroutinesApi
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList

@InternalCoroutinesApi
class RegisterFragment : BaseFragment() {
    private lateinit var locationManager: LocationManager
    private var latitude: Double = 6.465422
    private var longitude: Double = 3.406448
    private var organizationId: Int? = null
    private val viewModel by sharedViewModel<RegisterViewModel>()
    private val myCalendar: Calendar = Calendar.getInstance()
    private var campaignId: String? = null
    private lateinit var arrayAdapter: ArrayAdapter<String>
    private lateinit var campaignSpinner: MaterialAutoCompleteTextView

    private lateinit var phoneNumberWithCountryCode: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        viewModel.nfc = null
        viewModel.allFinger = null
        viewModel.profileImage = null
        viewModel.getAllCampaigns()
        back_btn.setOnClickListener {
            findNavController().navigateUp()
        }
        val adapter = ArrayAdapter(requireContext(),R.layout.spinner_drop_down, listOf("Male","Female"))
        registerGenderEdit.setAdapter(adapter)
        campaignSpinner = requireActivity().findViewById(R.id.registerCampaignEdit)
        viewModel.getCampaigns.observe(viewLifecycleOwner) {
            val array: ArrayList<String> = ArrayList()
            for (campaign in it){
                array.add(campaign.title!!)
            }
            arrayAdapter = ArrayAdapter(requireContext(),R.layout.spinner_drop_down, array)
            campaignSpinner.setAdapter(arrayAdapter)
            campaignSpinner.setOnItemClickListener { _, _, position, _ ->
                campaignId = it[position].id.toString()
            }
        }

        observeLoginDone()

        val specialAdapter = ArrayAdapter(requireContext(),R.layout.spinner_drop_down, listOf("No","Yes"))
        registerSpecialCaseEdit.setAdapter(specialAdapter)
        registerNINLayout.hide()
        txt_nin.hide()
        registerSpecialCaseEdit.setOnItemClickListener { parent, view, position, id ->
            if(position == 0){
                registerNINLayout.hide()
                txt_nin.hide()
            }else{
                registerNINLayout.show()
                txt_nin.show()
            }
        }

        change_account_text.setOnClickListener {
            openLogin(true)
            doLogout()
        }
        if(PrefUtils.getNGOId() == 0){
            openLogin()
            changeLoggedOutText()
        }else{
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
            requireContext().toast("Location permission is required.")
            return
        }
        try {
            initLocation()
        } catch (t: Throwable) {
            requireContext().toast("Please Ensure that the Device GPS is turned on")
            findNavController().navigateUp()
        }
        registerNextButton.setOnClickListener {
            checkInputs()
        }
    }

    private fun observeLoginDone() {
        viewModel.login.observe(viewLifecycleOwner) {
            Timber.v("XXXlogin vn reached ")
            when (it) {
                is ApiResponse.Success -> {
                    Timber.v("XXXlogin vn reached ")
                    findNavController().navigate(R.id.action_registerFragment_to_onboardingFragment)
                }
            }
        }
    }

    private fun changeLoggedInText() {
        logged_in_text.text = "Logged In "
        change_account_text.text = "Change Account? "
    }

    private fun changeLoggedOutText() {
        logged_in_text.text = "Not Logged In? "
        change_account_text.text = "Log In"
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

    private fun checkInputs() {
        var firstName = ""
        var lastName = ""
        var email = ""
        var password = ""
        var phone = ""
        var date = ""
        var gender = ""
        organizationId = PrefUtils.getNGOId()
        if(PrefUtils.getNGOId() == 0){
            showToast("Please Log In")
            openLogin()
            return
        }
        if(registerFirstNameEdit.isValid()){
            firstName = registerFirstNameEdit.text.toString()
            registerFirstNameLayout.error = ""
        }else{
            registerFirstNameLayout.error = "First name is required"
            return
        }
        if(registerLastNameEdit.isValid()){
            registerLastNameLayout.error = ""
            lastName = registerLastNameEdit.text.toString()
        }else{
            registerLastNameLayout.error = "Last name is required"
            return
        }
        if(registerEmailEdit.isValid()){
            registerEmailLayout.error = ""
            email = registerEmailEdit.text.toString()
        }
        if(registerPhoneEdit.isValid()){
            registerPhoneLayout.error = ""
            phone = registerPhoneEdit.text.toString()
            phoneNumberWithCountryCode = formatPhoneNumberWithCountryCode(phone)
            Timber.d(phoneNumberWithCountryCode)
        }else{
            registerPhoneLayout.error = "Phone number is required"
            return
        }
        if(registerDateEdit.isValid()){
            registerDateLayout.error = ""
            date = registerDateEdit.text.toString()
        }else{
            registerDateLayout.error = "Date of birth is required"
            return
        }

        if(campaignId == null){
            registerCampaignLayout.error = "Select a campaign"
            return
        }else{
            registerCampaignLayout.error = ""
        }
        gender = registerGenderEdit.text.toString()
        password = Utils.generatePassword()
        Timber.v(password)
        viewModel.campaign = campaignId!!
        val isSpecialCase = registerSpecialCaseEdit.text.toString() == "Yes"
        viewModel.specialCase = isSpecialCase

        if(isSpecialCase){
            when {
                registerNINEdit.text.isNullOrBlank() -> {
                    showToast("NIN is required.")
                    return
                }
                registerNINEdit.text.toString().length in 10..11 -> {
                    viewModel.nin = registerNINEdit.text.toString()
                }
                else -> {
                    showToast("NIN not valid")
                    return
                }
            }
        }
        findNavController().navigate(RegisterFragmentDirections.actionRegisterFragmentToRegisterVerifyFragment(firstName,lastName,email,phoneNumberWithCountryCode,password,latitude.toString(),longitude.toString(),organizationId!!,gender, date))

    }

    private fun formatPhoneNumberWithCountryCode(phoneNumber: String): String {
        val numberToBeFormatted = phoneNumber.drop(1)
        return "+234$numberToBeFormatted"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(PrefUtils.getNGOId() == 0){
            openLogin()
            changeLoggedOutText()
        } else {
            organizationId = PrefUtils.getNGOId()
            changeLoggedInText()
        }
    }


    private fun openCalendar() {
        val date =
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                myCalendar[Calendar.YEAR] = year
                myCalendar[Calendar.MONTH] = monthOfYear
                myCalendar[Calendar.DAY_OF_MONTH] = dayOfMonth
                updateLabel()
            }
        val datePicker = DatePickerDialog(
            requireContext(), date, myCalendar[Calendar.YEAR], myCalendar[Calendar.MONTH],
            myCalendar[Calendar.DAY_OF_MONTH]
        )
        datePicker.setButton(DialogInterface.BUTTON_POSITIVE,"OK", datePicker)
        datePicker.setButton(DialogInterface.BUTTON_NEGATIVE,"CANCEL", datePicker)
        datePicker.datePicker.maxDate = System.currentTimeMillis()

        datePicker.show()
    }

    private fun updateLabel() {
        val date = myCalendar.time
        registerDateEdit.setText(date.convertDateToString())
    }

    private fun openLogin(isCancelable: Boolean = true) {
        val bottomSheetDialogFragment = LoginDialog.newInstance()
        bottomSheetDialogFragment.isCancelable = isCancelable
        bottomSheetDialogFragment.setTargetFragment(this, 700)
        bottomSheetDialogFragment.show(requireFragmentManager().beginTransaction(),
            "BottomSheetDialog")
    }

    private fun doLogout() {
        PrefUtils.setNGO(0, "")
        changeLoggedOutText()
    }
}