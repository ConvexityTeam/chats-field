package com.codose.chats.views.cashForWork

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.fragment.findNavController
import com.codose.chats.R
import com.codose.chats.utils.ApiResponse
import com.codose.chats.utils.hide
import com.codose.chats.utils.show
import com.codose.chats.utils.toast
import com.codose.chats.views.auth.adapter.WorkerAdapter
import com.codose.chats.views.auth.adapter.WorkerClickListener
import kotlinx.android.synthetic.main.fragment_cash_for_work_task_details.*
import kotlinx.android.synthetic.main.layout_empty.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel


class CashForWorkTaskDetailsFragment : Fragment() {

    private val cashForWorkViewModel by viewModel<CashForWorkViewModel>()

    private lateinit var adapter : WorkerAdapter

    private lateinit var taskId : String

    private lateinit var taskName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = CashForWorkTaskDetailsFragmentArgs.fromBundle(requireArguments())
        taskId = args.taskId
        taskName = args.taskName
        cashForWorkViewModel.getTaskDetails(taskId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cash_for_work_task_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cfw_task_details_status.hide()
        adapter = WorkerAdapter(WorkerClickListener {
            findNavController().navigate(CashForWorkTaskDetailsFragmentDirections.actionCashForWorkTaskDetailsFragmentToCashForWorkImageFragment(
                taskId = taskId,
                userId = it.userId.toString(),
                taskName = taskName
            ))
        })

        cfw_task_details_title.text = taskName

        cfw_task_details_rv.adapter = adapter

        cfw_task_details_back_btn.setOnClickListener{
            findNavController().navigateUp()
        }

        setObservers()
    }

    private fun setObservers(){
        cashForWorkViewModel.taskDetails.observe(viewLifecycleOwner, {
            when (it) {
                is ApiResponse.Failure -> {
                    cfw_task_details_progress.hide()
                    requireContext().toast(it.message)
                    findNavController().navigateUp()
                }
                is ApiResponse.Loading -> {
                    cfw_task_details_progress.show()
                }
                is ApiResponse.Success -> {
                    cfw_task_details_progress.hide()
                    val data = it.data
                    adapter.submitList(data.data.task.associatedWorkers)
                    if(data.data.task.associatedWorkers.isEmpty()){
                        cfw_task_details_empty.show()
                        cfw_task_details_empty.txt_not_found.text = "No worker found"
                    }else{
                        cfw_task_details_status.show()
                        cfw_task_details_status.text = data.data.task.status.capitalize()
                        if(data.data.task.status == "pending"){
                            cfw_task_details_status.apply {
                                setBackgroundResource(R.drawable.transparent_rectangle_blue)
                                setTextColor(resources.getColor(R.color.colorBlue))
                            }
                        }else if(data.data.task.status == "fulfilled"){
                            cfw_task_details_status.apply {
                                setBackgroundResource(R.drawable.transparent_rectangle_green)
                                setTextColor(resources.getColor(R.color.colorPrimary))
                            }
                        }else{
                            cfw_task_details_status.apply {
                                setBackgroundResource(R.drawable.transparent_rectangle_yellow)
                                setTextColor(resources.getColor(R.color.colorYellow))
                            }
                        }
                        cfw_task_details_empty.hide()
                    }
                }
            }
        })
    }




}