package com.codose.chats.views.cashForWork

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.codose.chats.R
import com.codose.chats.utils.*
import com.codose.chats.views.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_cash_for_work.*
import kotlinx.android.synthetic.main.fragment_cash_for_work_submit.*
import kotlinx.android.synthetic.main.fragment_cash_for_work_task_details.*
import okhttp3.MultipartBody
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File


class CashForWorkSubmitFragment : BaseFragment() {

    private val cashForWorkViewModel by viewModel<CashForWorkViewModel>()
    private lateinit var taskId : String
    private lateinit var taskName : String
    private lateinit var userName : String
    private lateinit var userId : String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = CashForWorkSubmitFragmentArgs.fromBundle(requireArguments())
        taskId = args.taskId
        taskName = args.taskName
        userName = args.userName
        userId = args.userId
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cash_for_work_submit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cfw_submit_name.text = userName
        cfw_submit_title.text = taskName
        cfw_submit_back_btn.setOnClickListener {
            findNavController().navigateUp()
        }

        cfw_submit_btn.setOnClickListener{
            cashForWorkViewModel.postTaskCompleted(taskId, userId)
        }

        cfw_picture_card.setOnClickListener{
            findNavController().navigate(CashForWorkSubmitFragmentDirections.actionCashForWorkSubmitFragmentToCashForWorkImageFragment(
                taskId = taskId,
                userId = userId,
                taskName = taskName
            ))
        }

        setObservers()
    }

    private fun setObservers(){
        cashForWorkViewModel.taskOperation.observe(viewLifecycleOwner, {
            when (it) {
                is ApiResponse.Failure -> {
                    cfw_submit_progress.hide()
                    requireContext().toast(it.message)
                    findNavController().navigateUp()
                }
                is ApiResponse.Loading -> {
                    cfw_submit_progress.show()
                }
                is ApiResponse.Success -> {
                    cfw_submit_progress.hide()
                    val data = it.data
                    requireContext().toast(data.message)
                    findNavController().navigate(CashForWorkSubmitFragmentDirections.actionCashForWorkSubmitFragmentToOnboardingFragment())
                }
            }
        })
    }
}