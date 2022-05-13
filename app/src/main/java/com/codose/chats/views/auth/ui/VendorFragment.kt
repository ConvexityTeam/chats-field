package com.codose.chats.views.auth.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.codose.chats.R
import com.codose.chats.offline.Beneficiary
import com.codose.chats.offline.OfflineViewModel
import com.codose.chats.utils.*
import com.codose.chats.utils.BluetoothConstants.VENDOR_TYPE
import com.codose.chats.views.auth.dialog.LoginDialog
import com.codose.chats.views.auth.viewmodel.RegisterViewModel
import com.codose.chats.views.base.BaseFragment
import com.treebo.internetavailabilitychecker.InternetAvailabilityChecker
import kotlinx.android.synthetic.main.fragment_vendor.*
import kotlinx.coroutines.InternalCoroutinesApi
import org.koin.androidx.viewmodel.ext.android.viewModel


@InternalCoroutinesApi
class VendorFragment : BaseFragment() {

    private val beneficiary : Beneficiary = Beneficiary()
    private val registerViewModel by viewModel<RegisterViewModel>()
    private val offlineViewModel by viewModel<OfflineViewModel>()
    private lateinit var internetAvailabilityChecker: InternetAvailabilityChecker
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_vendor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(PrefUtils.getNGOId() == 0){
            logged_in_text2.hide()
            change_account_text2.hide()
            openLogin(true)
        }else{
            logged_in_text2.show()
            change_account_text2.show()
        }

        back_btn.setOnClickListener {
            findNavController().navigateUp()
        }

        internetAvailabilityChecker = InternetAvailabilityChecker.getInstance()

        change_account_text2.setOnClickListener {
            openLogin(true)
        }

        vendorNextButton.setOnClickListener {
            checkInputs()
        }

        setObservers()
    }

    private fun setObservers() {
        registerViewModel.onboardUser.observe(viewLifecycleOwner) {
            when (it) {
                is ApiResponse.Loading -> {
                    vendorProgress.show()
                    vendorNextButton.isEnabled = false
                }
                is ApiResponse.Success -> {
                    vendorProgress.hide()
                    val data = it.data
                    showToast(data.message)
                    findNavController().navigateUp()
                    vendorNextButton.isEnabled = true
                }
                is ApiResponse.Failure -> {
                    vendorProgress.hide()
                    showToast(it.message)
                    vendorNextButton.isEnabled = true
                }
            }
        }
    }

    private fun checkInputs() {
        val firstName = vendorFirstNameEdit.text.toString()
        val lastName = vendorLastNameEdit.text.toString()
        val businessName = vendorBusinessNameEdit.text.toString()
        val email = vendorEmailEdit.text.toString()
        val phone = vendorPhoneEdit.text.toString()
        val password = Utils.generatePassword()
        val pin =  Utils.generatePIN()
        val bvn = vendorBvnEdit.text.toString()
        val address = vendorAddressEdit.text.toString()
        val country = vendorCountryEdit.text.toString()
        val state = vendorStateEdit.text.toString()

        if(vendorFirstNameEdit.text.isNullOrBlank()){
            vendorFirstNameLayout.error = "First name is required"
            return
        }else{
            vendorFirstNameLayout.error = ""
        }

        if(vendorLastNameEdit.text.isNullOrBlank()){
            vendorLastNameLayout.error = "Last name is required"
            return
        }else{
            vendorLastNameLayout.error = ""
        }

        if(vendorBusinessNameEdit.text.isNullOrBlank()){
            vendorBusinessNameLayout.error = "Business name is required"
            return
        }else{
            vendorBusinessNameLayout.error = ""
        }

        if(vendorEmailEdit.text.isNullOrBlank()){
            vendorEmailLayout.error = "Email Address is required"
            return
        }else{
            vendorEmailLayout.error = ""
        }

//        if(vendorPasswordEdit.text.isNullOrBlank()){
//            vendorPasswordLayout.error = "Password is required"
//            return
//        }else{
//            if(vendorPasswordEdit.text.toString().length < 8){
//                vendorPasswordLayout.error = "Password must be at least 8 characters"
//                return
//            }
//            vendorPasswordLayout.error = ""
//        }

        if(vendorPhoneEdit.text.isNullOrBlank()){
            vendorPhoneLayout.error = "Phone number is required"
            return
        }else{
            if(vendorPhoneEdit.text.toString().length != 11){
                vendorPhoneLayout.error = "Invalid Phone Number"
                return
            }
            vendorPhoneLayout.error = ""
        }

        if(vendorBvnEdit.text.isNullOrBlank()){
            vendorBvnLayout.error = "BVN is required"
            return
        } else {
            if (vendorBvnEdit.text.toString().length in 10..11) {
                vendorBvnLayout.error = ""
            } else {
                vendorBvnLayout.error = "Invalid BVN"
                return
            }
        }
        if(PrefUtils.getNGOId() == 0){
            openLogin()
            return
        }
        beneficiary.storeName = businessName
        beneficiary.email = email
        beneficiary.phone = phone
        beneficiary.password = password
        beneficiary.pin = pin.toInt()
        beneficiary.bvn = bvn
        beneficiary.firstName = firstName
        beneficiary.lastName = lastName
//        beneficiary.organizationId = PrefUtils.getNGOId()
        beneficiary.type = VENDOR_TYPE
        beneficiary.address = address
        beneficiary.country = country
        beneficiary.state = state

        if(internetAvailabilityChecker.currentInternetAvailabilityStatus){
            registerViewModel.vendorOnboarding(businessName, email, phone, password, pin, bvn, firstName, lastName, address = address,
                country=country, state=state)
        }else{
            offlineViewModel.insert(beneficiary)
            showToast(getString(R.string.no_internet))
            findNavController().navigateUp()
        }

    }

    private fun openLogin(isCancelable : Boolean = true) {
        val bottomSheetDialogFragment = LoginDialog.newInstance()
        bottomSheetDialogFragment.isCancelable = isCancelable
        bottomSheetDialogFragment.setTargetFragment(this, 700)
        bottomSheetDialogFragment.show(requireFragmentManager().beginTransaction(),"BottomSheetDialog")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(PrefUtils.getNGOId() == 0){
            openLogin()
            logged_in_text2.hide()
            change_account_text2.hide()
        }else{
            logged_in_text2.show()
            change_account_text2.show()
        }
    }

}