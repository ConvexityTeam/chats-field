package com.codose.chats.views.cashForWork

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.codose.chats.R
import com.codose.chats.databinding.FragmentCashForWorkTaskDetailsBinding
import com.codose.chats.utils.hide
import com.codose.chats.utils.showToast
import com.codose.chats.views.auth.adapter.WorkerAdapter
import com.codose.chats.views.auth.adapter.WorkerClickListener
import org.koin.androidx.viewmodel.ext.android.viewModel

class CashForWorkTaskDetailsFragment : Fragment(R.layout.fragment_cash_for_work_task_details) {

    private var _binding: FragmentCashForWorkTaskDetailsBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModel<TaskDetailsViewModel>()
    private val args by navArgs<CashForWorkTaskDetailsFragmentArgs>()
    private val adapter: WorkerAdapter by lazy { WorkerAdapter(WorkerClickListener {  }) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentCashForWorkTaskDetailsBinding.bind(view)
        //cfw_task_details_status.hide()
        //adapter = WorkerAdapter(WorkerClickListener {
            /*findNavController().navigate(CashForWorkTaskDetailsFragmentDirections.actionCashForWorkTaskDetailsFragmentToCashForWorkImageFragment(
                taskId = taskId,
                userId = it.userId.toString(),
                taskName = taskName
            ))*/
        //})

        binding.taskDetailsTitle.text = args.taskName

        binding.backBtn.setOnClickListener {
            findNavController().navigateUp()
        }

        viewModel.getTaskDetails(taskId = args.taskId.toString())
        setObservers()
    }

    private fun setObservers() = with(binding) {
        viewModel.uiState.observe(viewLifecycleOwner) {
            when (it) {
                is TaskDetailsViewModel.TaskDetailsUiState.Error -> {
                    progressIndicator.root.hide()
                    showToast(it.errorMessage)
                    findNavController().navigateUp()
                }
                TaskDetailsViewModel.TaskDetailsUiState.Loading -> {
                    progressIndicator.root.show()
                }
                is TaskDetailsViewModel.TaskDetailsUiState.Success -> {
                    progressIndicator.root.hide()
                    val task = it.task
                    adapter.submitList(task.associatedWorkers)
                    if (task.associatedWorkers.isEmpty()) {
                        progressIndicator.root.show()
                        taskDetailsEmpty.txtNotFound.text = getString(R.string.text_no_worker_found)
                    } else {
                        /*cfw_task_details_status.show()
                        cfw_task_details_status.text = data.data.task.status.capitalize()
                        if (data.data.task.status == "pending") {
                            cfw_task_details_status.apply {
                                setBackgroundResource(R.drawable.transparent_rectangle_blue)
                                setTextColor(resources.getColor(R.color.colorBlue))
                            }
                        } else if (data.data.task.status == "fulfilled") {
                            cfw_task_details_status.apply {
                                setBackgroundResource(R.drawable.transparent_rectangle_green)
                                setTextColor(resources.getColor(R.color.colorPrimary))
                            }
                        } else {
                            cfw_task_details_status.apply {
                                setBackgroundResource(R.drawable.transparent_rectangle_yellow)
                                setTextColor(resources.getColor(R.color.colorYellow))
                            }
                        }*/
                        taskDetailsEmpty.root.hide()
                    }
                }
            }
        }
    }
}
