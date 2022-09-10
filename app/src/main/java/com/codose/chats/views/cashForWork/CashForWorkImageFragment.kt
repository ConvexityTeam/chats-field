@file:Suppress("DEPRECATION")

package com.codose.chats.views.cashForWork

import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.codose.chats.R
import com.codose.chats.utils.*
import com.codose.chats.views.auth.adapter.PrintPager
import com.codose.chats.views.auth.adapter.PrintPagerAdapter
import com.codose.chats.views.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_cash_for_work_image.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.io.File


class CashForWorkImageFragment : BaseFragment() {

    private lateinit var adapter: PrintPagerAdapter
    private val cashForWorkViewModel by viewModel<CashForWorkViewModel>()
    private lateinit var taskId: String
    private lateinit var userId: String
    private lateinit var taskName : String
    private var images = arrayListOf<Bitmap>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = CashForWorkImageFragmentArgs.fromBundle(requireArguments())
        taskId = args.taskId
        userId = args.userId
        taskName = args.taskName
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cash_for_work_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = PrintPagerAdapter(requireContext())
        task_details.text = "Task: $taskName"

        cfw_image_one.setOnClickListener {
            dispatchTakePictureIntent(REQUEST_IMAGE_CAPTURE)
        }
        cfw_image_two.setOnClickListener {
            dispatchTakePictureIntent(REQUEST_IMAGE_CAPTURE_TWO)
        }
        cfw_image_three.setOnClickListener {
            dispatchTakePictureIntent(REQUEST_IMAGE_CAPTURE_THREE)
        }
        cfw_image_four.setOnClickListener {
            dispatchTakePictureIntent(REQUEST_IMAGE_CAPTURE_FOUR)
        }
        cfw_image_five.setOnClickListener {
            dispatchTakePictureIntent(REQUEST_IMAGE_CAPTURE_FIVE)
        }
        cfw_image_back_btn.setOnClickListener {
            findNavController().navigateUp()
        }
        cfw_image_submit.setOnClickListener {
            if(cashForWorkViewModel.imageList.value!!.size >= 5){
                if(cfw_image_descEdit.text.isNullOrBlank().not()){
                    cfw_image_descLayout.error = ""
                    submitImages(cfw_image_descEdit.text.toString())
                }else{
                    cfw_image_descLayout.error = "This field is required."
                }
            }else{
                requireContext().toast(getString(R.string.add_more_image))
            }
        }
        setObservers()
    }

    private fun submitImages(desc: String) {
        val imagesPart = ArrayList<File>()

        cashForWorkViewModel.imageList.value!!.forEachIndexed { index, bitmap ->
            val f = bitmap.toFile(requireContext(),
                "cash_for_work" + System.currentTimeMillis() + "$index" + ".png")
            imagesPart.add(f)
        }

        cashForWorkViewModel.postTaskImages(taskId, userId, desc, imagesPart)
    }

    private fun dispatchTakePictureIntent(requestCode: Int) {
        if(cashForWorkViewModel.imageList.value!!.size < 5){
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            try {
                startActivityForResult(takePictureIntent, requestCode)
            } catch (e: ActivityNotFoundException) {
                requireContext().toast(getString(R.string.camera_error))
            }
        }else{
            requireContext().toast(getString(R.string.limit_reached))
        }

    }

    private fun setObservers() {
        cashForWorkViewModel.taskOperation.observe(viewLifecycleOwner) {
            when (it) {
                is ApiResponse.Failure -> {
                    cfw_image_submit_progress.hide()
                    requireContext().toast(it.message)
                }
                is ApiResponse.Loading -> {
                    cfw_image_submit_progress.show()
                }
                is ApiResponse.Success -> {
                    cfw_image_submit_progress.hide()
                    val data = it.data
                    requireContext().toast(data.message)
                    findNavController().navigate(CashForWorkImageFragmentDirections.actionCashForWorkImageFragmentToOnboardingFragment())
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    cfw_image_one.setImageBitmap(imageBitmap)
                }
                REQUEST_IMAGE_CAPTURE_TWO -> {
                    cfw_image_two.setImageBitmap(imageBitmap)
                }
                REQUEST_IMAGE_CAPTURE_THREE -> {
                    cfw_image_three.setImageBitmap(imageBitmap)
                }
                REQUEST_IMAGE_CAPTURE_FOUR -> {
                    cfw_image_four.setImageBitmap(imageBitmap)
                }
                REQUEST_IMAGE_CAPTURE_FIVE -> {
                    cfw_image_five.setImageBitmap(imageBitmap)
                }
            }
            images.add(imageBitmap)
            cashForWorkViewModel.imageList.value = images
        }
    }

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 748
        const val REQUEST_IMAGE_CAPTURE_TWO = 749
        const val REQUEST_IMAGE_CAPTURE_THREE = 750
        const val REQUEST_IMAGE_CAPTURE_FOUR = 751
        const val REQUEST_IMAGE_CAPTURE_FIVE = 752

    }

}