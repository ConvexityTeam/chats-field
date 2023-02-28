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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.FragmentRegisterImageBinding
import chats.cash.chats_field.utils.ChatsFieldConstants.REQUEST_CODE_PERMISSIONS
import chats.cash.chats_field.utils.ChatsFieldConstants.REQUIRED_PERMISSIONS
import chats.cash.chats_field.utils.safeNavigate
import chats.cash.chats_field.utils.toast
import chats.cash.chats_field.views.auth.viewmodel.RegisterViewModel
import kotlinx.coroutines.InternalCoroutinesApi
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber
import java.io.File

@InternalCoroutinesApi
class RegisterImageFragment : Fragment() {

    private val viewModel by sharedViewModel<RegisterViewModel>()
    private lateinit var _binding:FragmentRegisterImageBinding
    private val binding:FragmentRegisterImageBinding
        get() = _binding
    private var imageBitmap : Bitmap? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentRegisterImageBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (viewModel.profileImage != null) {
            Glide.with(requireContext())
                .load(File(viewModel.profileImage!!))
                .into(binding.registerImageView)
            binding.registerImageNextBtn.setOnClickListener {
                findNavController().navigateUp()
            }
        }

        binding.registerImageView.setOnClickListener {
            //Navigate to image intent
            Timber.v("Clicked")
            requestCameraPermission()
        }
        binding.registerImageBackButton.setOnClickListener {
            findNavController().navigateUp()
        }
        //Declared Nav Btn from CaptureImage to ScanFingerPrintScan
        binding.registerImageNextBtn.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun requestCameraPermission() {
        if (allPermissionsGranted()) {
            findNavController().navigate(RegisterImageFragmentDirections.toImageCaptureFragment())
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                findNavController().navigate(RegisterImageFragmentDirections.toImageCaptureFragment())
            } else {
                requireContext().toast("Permissions not granted by the user.")
                findNavController().navigateUp()
            }
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            // display error state to the user
        }
    }

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireActivity().baseContext, it) == PackageManager.PERMISSION_GRANTED
    }





}