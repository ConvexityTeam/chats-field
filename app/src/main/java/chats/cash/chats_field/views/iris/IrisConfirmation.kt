package chats.cash.chats_field.views.iris

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.FragmentIrisConfirmationBinding
import chats.cash.chats_field.utils.toast
import chats.cash.chats_field.views.auth.viewmodel.RegisterViewModel
import chats.cash.chats_field.views.base.BaseFragment
import chats.cash.chats_field.views.core.showErrorSnackbar

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [IrisConfirmation.newInstance] factory method to
 * create an instance of this fragment.
 */
class IrisConfirmation : BaseFragment() {

    private lateinit var binding: FragmentIrisConfirmationBinding
    private val registerViewModel by activityViewModels<RegisterViewModel>()

    val getContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (activityResult.resultCode == RESULT_OK) {
                val byte = activityResult.data?.getStringExtra(IRIS_ENROLL_PARAM)
                if (byte != null) {
                    registerViewModel.insertIrisSignature(byte)
                    binding.desc.text = "Iris verification was successful, continue"
                    binding.title.text = "Success"
                    binding.startBtn.text = "Continue"
                    binding.startBtn.setOnClickListener {
                        findNavController().navigateUp()
                    }
                } else {
                    requireContext().toast("byte is null")
                    binding.desc.text = "Iris verification was not successful, try again"
                    binding.title.text = "Verification failed"
                    binding.startBtn.text = "Try again"
                }
            } else {
                showErrorSnackbar(R.string.iris_scan_error, binding.root)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentIrisConfirmationBinding.inflate(inflater)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    fun init() {
        binding.startBtn.setOnClickListener {
            getContent.launch(Intent(requireContext(), IrisCapture::class.java))
        }
        binding.backBtn2.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.backBtn.setOnClickListener {
            findNavController().navigateUp()
        }
    }
}
