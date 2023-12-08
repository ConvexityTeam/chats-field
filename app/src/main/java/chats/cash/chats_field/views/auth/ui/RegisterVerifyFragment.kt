package chats.cash.chats_field.views.auth.ui

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.FragmentRegisterVerifyBinding
import chats.cash.chats_field.model.ModelCampaign
import chats.cash.chats_field.utils.hide
import chats.cash.chats_field.utils.show
import chats.cash.chats_field.views.auth.viewmodel.RegisterViewModel
import chats.cash.chats_field.views.base.BaseFragment
import kotlinx.coroutines.InternalCoroutinesApi
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.util.*

@InternalCoroutinesApi
class RegisterVerifyFragment : BaseFragment() {

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
    private var organizationId: Int = 0
    private val mViewModel by activityViewModel<RegisterViewModel>()
    private val registerViewModel by activityViewModels<RegisterViewModel>()

    private lateinit var binding: FragmentRegisterVerifyBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentRegisterVerifyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = RegisterVerifyFragmentArgs.fromBundle(requireArguments())
        nfc = ""
        firstName = args.firstName
        lastname = args.lastName
        email = args.email
        campaign = registerViewModel.campaign!!
        password = args.password
        phone = args.phone
        lat = args.latitude
        long = args.longitude
        organizationId = args.organizationId
        gender = args.gender
        date = args.date
        // pin = args.pin
        binding.appbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        registerViewModel.resetOnboardState()
        if (mViewModel.specialCase) {
            binding.verifyPrintCard.hide()
        }

        if (mViewModel.isGroupBeneficiary) {
            binding.groupProgressLayout.show()
            binding.group.hide()
        } else {
            binding.groupProgressLayout.hide()
            binding.group.show()
        }

        if (mViewModel.profileImage != null) {
            profileImage = mViewModel.profileImage
        }

        binding.verifyIrisCard.setOnClickListener {
            //findNavController().safeNavigate(R.id.action_registerVerifyFragment_to_irisConfirmation)
        }

        binding.verifyPrintCard.setOnClickListener {
            findNavController().navigate(R.id.action_registerVerifyFragment_to_fingerPrintHomeFragment)
        }

        binding.verifyNinCard.setOnClickListener {
            findNavController().navigate(R.id.action_registerVerifyFragment_to_ninHomeFragment)
        }
    }
}

const val NFC_REQUEST_KEY = "7080"
