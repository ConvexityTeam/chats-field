package chats.cash.chats_field.views.impact_report

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import chats.cash.chats_field.BuildConfig
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.FragmentImpactReportUploadBinding
import chats.cash.chats_field.utils.hide
import chats.cash.chats_field.utils.show
import chats.cash.chats_field.views.auth.viewmodel.RegisterViewModel
import chats.cash.chats_field.views.core.dialogs.showSuccessDialog
import chats.cash.chats_field.views.core.showErrorSnackbar
import chats.cash.chats_field.views.core.showSuccessSnackbar
import chats.cash.chats_field.views.impact_report.utils.RecordVideoContract
import chats.cash.chats_field.views.impact_report.viewModel.ImpactReportViewModel
import chats.cash.chats_field.views.impact_report.viewModel.UploadState
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.util.Calendar

class ImpactReportUploadFragment :
    Fragment(R.layout.fragment_impact_report_upload) {

    private var _binding: FragmentImpactReportUploadBinding? = null
    private val registerViewModel by activityViewModel<RegisterViewModel>()
    private val impactViewModel by viewModel<ImpactReportViewModel>()
    private val binding get() = _binding!!

    val campaign by lazy {
        registerViewModel.campaign
    }

    private val recordVideoLauncher = registerForActivityResult(RecordVideoContract()) { result ->
        // Handle the result URI here
        if (result != null) {
            val file = videoFile
            if (campaign != null) {
                file?.let { filee ->
                    binding.uploadVideo.text = filee.name
                    impactViewModel.uploadVideo(filee, campaign!!) { progress, current, total ->
                        binding.progressText.text = requireContext().getString(
                            R.string.impact_report_upload_progress,
                            current,
                            total,
                            progress.toString(),
                        )
                    }
                } ?: run {
                    showErrorSnackbar(R.string.video_not_recorded, binding.root)
                }
            } else {
                showErrorSnackbar(R.string.no_campaign_selected, binding.root)
            }
        } else {
            showErrorSnackbar(R.string.video_not_recorded, binding.root)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentImpactReportUploadBinding.bind(view)
        init()
    }

    private var link: String? = null
    private fun init() {
        binding.progress.max = 100
        binding.impactReportAppbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.impactReportAppbar.title = campaign?.title

        lifecycleScope.launch {
            impactViewModel.response.collect {
                if (it.loading) {
                    binding.submitReport.isEnabled = false
                    binding.progressIndicator.show()
                } else if (it.error) {
                    binding.progressIndicator.hide()
                    binding.submitReport.enable()
                    showErrorSnackbar(R.string.an_unknown_error_occured, binding.root)
                } else if (it.success) {
                    binding.progressIndicator.hide()
                    binding.submitReport.enable()
                    requireContext().showSuccessDialog(
                        R.string.impactreport_submittted,
                        R.string.impact_desc,
                    ) {
                        findNavController().navigate(R.id.action_impactReportUploadFragment_to_onboardingFragment)
                    }.show()
                }
            }
        }

        lifecycleScope.launch {
            impactViewModel.uploadState.collect { state ->
                binding.impactReportGroup.hide()
                when (state) {
                    is UploadState.COMPLETED -> {
                        binding.uploadVideo.enable()
                        binding.cancelUpload.disable()
                        showSuccessSnackbar(R.string.video_uploaded, binding.root)
                        link = state.url
                    }

                    is UploadState.ERROR -> {
                        binding.uploadVideo.enable()
                        binding.cancelUpload.disable()
                        showErrorSnackbar(R.string.upload_failed, binding.root)
                    }

                    UploadState.STARTED -> {
                        binding.uploadVideo.disable()
                        binding.cancelUpload.enable()
                        binding.impactReportGroup.show()
                    }

                    UploadState.UPLOADING -> {
                        binding.uploadVideo.disable()
                        binding.cancelUpload.enable()
                        binding.impactReportGroup.show()
                    }

                    is UploadState.Idle -> {
                        binding.uploadVideo.enable()
                        binding.cancelUpload.disable()
                    }
                }
            }
        }

        lifecycleScope.launch {
            impactViewModel.progress.collect {
                binding.progress.progress = it.toInt()
            }
        }

        binding.uploadVideo.setOnClickListener {
            recordVideoLauncher.launch(createVideoFileUri())
        }

        binding.cancelUpload.setOnClickListener {
            impactViewModel.cancelUpload()
            binding.uploadVideo.text = getString(R.string.upload_media)
        }

        binding.submitReport.setOnClickListener {
            if (link.isNullOrEmpty()) {
                showErrorSnackbar(R.string.please_upload_a_video, binding.root)
                return@setOnClickListener
            } else if (binding.titleText.text.isNullOrEmpty() || binding.titleText.text.toString().length < 3) {
                showErrorSnackbar(R.string.please_enter_report_title, binding.root)
                return@setOnClickListener
            }
            val id = registerViewModel.userProfile?.id
            if (id != null) {
                impactViewModel.sendImpactReport(
                    link!!,
                    binding.titleText.text.toString(),
                    id.toString(),
                    campaign!!.id,
                )
            } else {
                showErrorSnackbar(R.string.invalid_field_id, binding.root)
                return@setOnClickListener
            }
        }
    }

    var videoFile: File? = null

    private fun createVideoFileUri(): Uri {
        // Create a file using FileProvider to store the recorded video
        videoFile = File(requireActivity().filesDir, "${Calendar.getInstance().time.time}.mp4")
        return FileProvider.getUriForFile(
            requireContext(),
            BuildConfig.APPLICATION_ID + ".provider",
            videoFile!!,
        )
    }
}

fun View.disable() {
    this.isEnabled = false
}

fun View.enable() {
    this.isEnabled = true
}
