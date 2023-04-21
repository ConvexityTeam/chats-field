package chats.cash.chats_field.views.auth.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.FragmentRegisterImageBinding
import chats.cash.chats_field.utils.ChatsFieldConstants.REQUEST_CODE_PERMISSIONS
import chats.cash.chats_field.utils.ChatsFieldConstants.REQUIRED_PERMISSIONS
import chats.cash.chats_field.utils.dialogs.AlertDialog
import chats.cash.chats_field.utils.safeNavigate
import chats.cash.chats_field.utils.toast
import chats.cash.chats_field.views.auth.viewmodel.RegisterViewModel
import chats.cash.chats_field.views.base.BaseFragment
import chats.cash.chats_field.views.core.dialogs.getPermissionDialogs
import chats.cash.chats_field.views.core.permissions.*
import chats.cash.chats_field.views.core.showErrorSnackbar
import kotlinx.coroutines.InternalCoroutinesApi
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber
import java.io.File

@InternalCoroutinesApi
class RegisterImageFragment : BaseFragment() {

    private val viewModel by sharedViewModel<RegisterViewModel>()
    private lateinit var _binding:FragmentRegisterImageBinding
    private val binding:FragmentRegisterImageBinding
        get() = _binding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentRegisterImageBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.profileImage?.let {
            binding.registerImageView.loadGlide(File(it))
        }

        binding.registerImageView.setOnClickListener {
            //Navigate to image intent
            if(requireContext().checkPermission(CAMERA_PERMISSION)) {
                findNavController().navigate(RegisterImageFragmentDirections.toImageCaptureFragment())
            }
            else{
                showErrorSnackbar(R.string.camera_permission_desc,binding.root)
            }
        }
        binding.registerImageBackButton.setOnClickListener {
            findNavController().navigateUp()
        }
        //Declared Nav Btn from CaptureImage to ScanFingerPrintScan
        binding.registerImageNextBtn.setOnClickListener {
            findNavController().navigateUp()
        }
    }







}