package chats.cash.chats_field.views.auth.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.FragmentRegisterImageBinding
import chats.cash.chats_field.views.auth.viewmodel.RegisterViewModel
import chats.cash.chats_field.views.base.BaseFragment
import chats.cash.chats_field.views.core.permissions.CAMERA_PERMISSION
import chats.cash.chats_field.views.core.permissions.checkPermission
import chats.cash.chats_field.views.core.showErrorSnackbar
import kotlinx.coroutines.InternalCoroutinesApi
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.io.File

@InternalCoroutinesApi
class RegisterImageFragment : BaseFragment() {

    private val viewModel by activityViewModel<RegisterViewModel>()
    private lateinit var _binding: FragmentRegisterImageBinding
    private val binding: FragmentRegisterImageBinding
        get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentRegisterImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.profileImage?.let {
            binding.registerImageView.loadGlide(File(it))
        }

        binding.registerImageView.setOnClickListener {
            // Navigate to image intent
            if (requireContext().checkPermission(CAMERA_PERMISSION)) {
                findNavController().navigate(
                    RegisterImageFragmentDirections.toImageCaptureFragment(),
                )
            } else {
                showErrorSnackbar(R.string.camera_permission_desc, binding.root)
            }
        }
        binding.registerImageNextBtn.setOnClickListener {
            if (requireContext().checkPermission(CAMERA_PERMISSION)) {
                findNavController().navigate(
                    RegisterImageFragmentDirections.toImageCaptureFragment(),
                )
            } else {
                showErrorSnackbar(R.string.camera_permission_desc, binding.root)
            }
        }
    }
}
