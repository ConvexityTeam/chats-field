package chats.cash.chats_field.views.auth.ui

import android.app.DatePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.FragmentRegisterGroupBinding
import chats.cash.chats_field.network.NetworkResponse
import chats.cash.chats_field.offline.Beneficiary
import chats.cash.chats_field.offline.OfflineViewModel
import chats.cash.chats_field.utils.PreferenceUtilInterface
import chats.cash.chats_field.utils.Utils
import chats.cash.chats_field.utils.convertDateToString
import chats.cash.chats_field.utils.hide
import chats.cash.chats_field.utils.isEmailValid
import chats.cash.chats_field.utils.isValid
import chats.cash.chats_field.utils.safeNavigate
import chats.cash.chats_field.utils.show
import chats.cash.chats_field.views.auth.viewmodel.RegisterViewModel
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.util.*

@InternalCoroutinesApi
class RegisterGroupFragment : Fragment(R.layout.fragment_register_group) {

    private var _binding: FragmentRegisterGroupBinding? = null
    private val binding get() = _binding!!

    private val preferenceUtil: PreferenceUtilInterface by inject()
    private val offlineViewModel by activityViewModels<OfflineViewModel>()
    private val organizationId: Int by lazy { preferenceUtil.getNGOId() }
    private val viewModel by activityViewModel<RegisterViewModel>()
    private val myCalendar: Calendar = Calendar.getInstance()

    private var isNumberValid = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    val adapter by lazy {
        ArrayAdapter(
            requireContext(),
            android.R.layout.simple_selectable_list_item,
            listOf("Male", "Female"),
        )
    }

    val specialAdapter by lazy {
        ArrayAdapter(
            requireContext(),
            android.R.layout.simple_selectable_list_item,
            listOf("No", "Yes"),
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRegisterGroupBinding.bind(view)

        viewModel.nfc = null
        viewModel.profileImage = null
        viewModel.insertIrisSignature(null)
        viewModel.restartFingerPrintScan()
        viewModel.nin = ""
        viewModel.specialCase = false

        binding.run {
            appbar.setNavigationOnClickListener {
                findNavController().navigateUp()
            }

            registerGenderEdit.setAdapter(adapter)

            observeLoginDone()

            registerDateEdit.setOnClickListener {
                openCalendar()
            }

            registerNextButton.setOnClickListener {
                checkInputs()
            }

            addCCpListeners()
        }
    }

    private fun observeLoginDone() {
        viewModel.login.observe(viewLifecycleOwner) {
            Timber.v("XXXlogin vn reached ")
            when (it) {
                is NetworkResponse.Success -> {
                    Timber.v("XXXlogin vn reached ")
                    findNavController().safeNavigate(
                        R.id.action_registerFragment_to_onboardingFragment,
                    )
                }

                else -> {}
            }
        }
    }

    private fun checkInputs() = with(binding) {
        val firstName: String
        val lastName: String
        val email: String
        val phone: String
        val date: String
        //val pin: String

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
        if (isNumberValid) {
            phoneError.error = ""
            phoneError.hide()
            phone = binding.ccp.fullNumber
            Timber.v(phone)
        } else {
            phoneError.error = "Phone number is required"
            phoneError.show()
            return
        }
//        if (inputPinEdit.isValid() && inputPinEdit.text.toString().isValidPin()) {
//            inputPinLayout.error = ""
//            pin = inputPinEdit.text.toString()
//        } else {
//            inputPinLayout.error = "A 4-digit PIN is required"
//            return
//        }
        if (registerDateEdit.isValid()) {
            registerDateLayout.error = ""
            date = registerDateEdit.text.toString()
        } else {
            registerDateLayout.error = "Date of birth is required"
            return
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

        lifecycleScope.launch {
            offlineViewModel.getAllCampaignForms().asLiveData(coroutineContext).observe(
                viewLifecycleOwner,
            ) {
                viewModel.tempBeneficiary = Beneficiary(
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    phone = phone,
                    password = password,
                    latitude = preferenceUtil.getLatLong().first,
                    longitude = preferenceUtil.getLatLong().second,
                    id = organizationId,
                    gender = gender,
                    date = date,
                )
                if (it.isNotEmpty()) {
                    val campaignForm =
                        it.find { form -> form.campaigns.any { camp -> camp.id == viewModel.campaign?.id } }
                    campaignForm?.let { form ->
                        Timber.d(form.questions.toString())
                        offlineViewModel.setCampaignForm(form)
                        findNavController().safeNavigate(
                            RegisterGroupFragmentDirections.toRegisterOptinCampaignFragment2(
                                firstName = firstName,
                                lastName = lastName,
                                email = email,
                                phone = phone,
                                password = password,
                                latitude = preferenceUtil.getLatLong().first.toString(),
                                longitude = preferenceUtil.getLatLong().second.toString(),
                                organizationId = organizationId,
                                gender = gender,
                                date = date,
                                //  pin = pin,
                            ),
                        )
                    } ?: run {
                        findNavController().safeNavigate(
                            RegisterGroupFragmentDirections.actionRegisterGroupFragmentToRegisterImageFragment(
//                                firstName = firstName,
//                                lastName = lastName,
//                                email = email,
//                                phone = phone,
//                                password = password,
//                                latitude = preferenceUtil.getLatLong().first.toString(),
//                                longitude = preferenceUtil.getLatLong().second.toString(),
//                                organizationId = organizationId,
//                                gender = gender,
//                                date = date,
                                //  pin = pin,
                            ),
                        )
                    }
                } else {
                    viewModel.getAllCampaignForms()
                    findNavController().safeNavigate(
                        RegisterGroupFragmentDirections.actionRegisterGroupFragmentToRegisterImageFragment(),
                    )
                }
            }
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
            requireContext(),
            date,
            myCalendar[Calendar.YEAR],
            myCalendar[Calendar.MONTH],
            myCalendar[Calendar.DAY_OF_MONTH],
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

    override fun onPause() {
        super.onPause()
        lifecycleScope.launch {
            offlineViewModel.getAllCampaignForms().asLiveData(coroutineContext).removeObservers(
                viewLifecycleOwner,
            )
        }
    }

    private fun addCCpListeners() {
        binding.ccp.apply {
            registerCarrierNumberEditText(binding.registerPhoneEdit)

            setPhoneNumberValidityChangeListener { isValid ->
                isNumberValid = isValid
                Timber.v(fullNumber)
            }
            binding.registerPhoneEdit.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int,
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                    if (s?.startsWith("0") == true) {
                        s.replace(0, 1, "")
                    }
                }
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
