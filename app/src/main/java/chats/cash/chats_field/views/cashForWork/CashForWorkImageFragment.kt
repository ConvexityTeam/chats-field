package chats.cash.chats_field.views.cashForWork

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import chats.cash.chats_field.BuildConfig
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.FragmentCashForWorkImageBinding
import chats.cash.chats_field.utils.*
import chats.cash.chats_field.views.auth.adapter.PrintPagerAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class CashForWorkImageFragment : Fragment(R.layout.fragment_cash_for_work_image) {

    private var _binding: FragmentCashForWorkImageBinding? = null
    private val binding get() = _binding!!
    private val cashForWorkViewModel by viewModel<CashForWorkViewModel>()
    private val args by navArgs<CashForWorkImageFragmentArgs>()
    private var launcherOne: ActivityResultLauncher<Intent>? = null
    private var launcherTwo: ActivityResultLauncher<Intent>? = null
    private var launcherThree: ActivityResultLauncher<Intent>? = null
    private var launcherFour: ActivityResultLauncher<Intent>? = null
    private var launcherFive: ActivityResultLauncher<Intent>? = null
    private lateinit var adapter: PrintPagerAdapter
    private var images = arrayListOf<Bitmap>()
    private var tempImages = arrayListOf<Uri>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCashForWorkImageBinding.bind(view)
        adapter = PrintPagerAdapter(requireContext())
        binding.taskDetails.text = String.format("Task: %s", args.taskName)

        setupClickListeners()
        setObservers()
    }

    private fun setupClickListeners() = with(binding) {
        cfwImageOne.setOnClickListener {
            dispatchTakePictureIntent(launcherOne)
        }
        cfwImageTwo.setOnClickListener {
            dispatchTakePictureIntent(launcherTwo)
        }
        cfwImageThree.setOnClickListener {
            dispatchTakePictureIntent(launcherThree)
        }
        cfwImageFour.setOnClickListener {
            dispatchTakePictureIntent(launcherFour)
        }
        cfwImageFive.setOnClickListener {
            dispatchTakePictureIntent(launcherFive)
        }
        cfwImageBackBtn.setOnClickListener {
            findNavController().navigateUp()
        }
        cfwImageSubmit.setOnClickListener {
            if (cashForWorkViewModel.imageList.value!!.size >= 5) {
                if (cfwImageDescEdit.text.isNullOrBlank().not()) {
                    cfwImageDescLayout.error = ""
                    submitImages(cfwImageDescEdit.text.toString())
                } else {
                    cfwImageDescLayout.error = "This field is required."
                }
            } else {
                showToast(getString(R.string.add_more_image))
            }
        }
    }

    private fun submitImages(desc: String) {
        val imagesPart = ArrayList<File>()

        cashForWorkViewModel.imageList.value!!.forEachIndexed { index, bitmap ->
            val f = bitmap.toFile(requireContext(),
                "cash_for_work" + System.currentTimeMillis() + "$index" + ".png")
            imagesPart.add(f)
        }

        cashForWorkViewModel.postTaskImages(
            beneficiaryId = args.beneficiaryId,
            taskAssignmentId = args.taskId,
            description = desc,
            images = imagesPart
        )
    }

    private fun dispatchTakePictureIntent(launcher: ActivityResultLauncher<Intent>?) {
        if (cashForWorkViewModel.imageList.value!!.size < 5) {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            try {
                launcher?.launch(takePictureIntent)
            } catch (e: ActivityNotFoundException) {
                showToast(getString(R.string.camera_error))
            }
        } else {
            showToast(getString(R.string.limit_reached))
        }
    }

    private fun setObservers() {
        cashForWorkViewModel.taskOperation.observe(viewLifecycleOwner) {
            when (it) {
                is ApiResponse.Failure -> {
                    binding.cfwImageSubmitProgress.root.hide()
                    showToast(it.message)
                }
                is ApiResponse.Loading -> {
                    binding.cfwImageSubmitProgress.root.show()
                }
                is ApiResponse.Success -> {
                    binding.cfwImageSubmitProgress.root.hide()
                    val data = it.data
                    showToast(data.message)
                    findNavController().safeNavigate(CashForWorkImageFragmentDirections.toOnboardingFragment())
                }
            }
        }

        cashForWorkViewModel.imageUpload.observe(viewLifecycleOwner) {
            when (it) {
                is CashForWorkViewModel.ImageUploadState.Error -> {
                    showToast(it.errorMessage)
                }
                CashForWorkViewModel.ImageUploadState.Loading -> {
                    binding.cfwImageSubmitProgress.root.show()
                }
                is CashForWorkViewModel.ImageUploadState.Success -> {
                    binding.cfwImageSubmitProgress.root.hide()
                    val data = it.data
                    showToast(data.message)
                    findNavController().safeNavigate(CashForWorkImageFragmentDirections.toOnboardingFragment())
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        launcherOne = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val imageBitmap = it.data?.extras?.get("data") as Bitmap?
            imageBitmap?.let { bitmap ->
                binding.cfwImageOne.setImageBitmap(imageBitmap)
                images.add(bitmap)
                cashForWorkViewModel.imageList.value = images
            }
        }
        launcherTwo = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val imageBitmap = it.data?.extras?.get("data") as Bitmap?
            imageBitmap?.let { bitmap ->
                binding.cfwImageTwo.setImageBitmap(bitmap)
                images.add(bitmap)
                cashForWorkViewModel.imageList.value = images
            }
        }
        launcherThree =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                val imageBitmap = it.data?.extras?.get("data") as Bitmap?
                imageBitmap?.let { bitmap ->
                    binding.cfwImageThree.setImageBitmap(bitmap)
                    images.add(bitmap)
                    cashForWorkViewModel.imageList.value = images
                }
            }
        launcherFour = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val imageBitmap = it.data?.extras?.get("data") as Bitmap?
            imageBitmap?.let { bitmap ->
                binding.cfwImageFour.setImageBitmap(bitmap)
                images.add(bitmap)
                cashForWorkViewModel.imageList.value = images
            }
        }
        launcherFive = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val imageBitmap = it.data?.extras?.get("data") as Bitmap?
            imageBitmap?.let { bitmap ->
                binding.cfwImageFive.setImageBitmap(bitmap)
                images.add(bitmap)
                cashForWorkViewModel.imageList.value = images
            }
        }
    }

    private fun getTempFileUri(): Uri {
        val tempFile =
            File.createTempFile("cam_image", ".png", requireActivity().cacheDir).apply {
                createNewFile()
                deleteOnExit()
            }
        return FileProvider.getUriForFile(
            requireContext(),
            "${BuildConfig.APPLICATION_ID}.provider",
            tempFile
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
