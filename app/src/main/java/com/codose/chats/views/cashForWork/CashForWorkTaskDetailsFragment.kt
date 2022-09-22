package com.codose.chats.views.cashForWork

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.codose.chats.R
import com.codose.chats.databinding.FragmentCashForWorkTaskDetailsBinding
import com.codose.chats.utils.hide
import com.codose.chats.utils.show
import com.codose.chats.utils.showToast
import com.codose.chats.utils.toStatusString
import com.codose.chats.views.auth.adapter.WorkerAdapter
import com.codose.chats.views.cashForWork.model.AssignedWorker
import org.koin.androidx.viewmodel.ext.android.viewModel

class CashForWorkTaskDetailsFragment : Fragment(R.layout.fragment_cash_for_work_task_details) {

    private var _binding: FragmentCashForWorkTaskDetailsBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModel<TaskDetailsViewModel>()
    private val args by navArgs<CashForWorkTaskDetailsFragmentArgs>()
    private val workAdapter: WorkerAdapter by lazy {
        WorkerAdapter(
            onItemClick = ::toImageDetails,
            onAddWorkerClick = ::addWorker
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentCashForWorkTaskDetailsBinding.bind(view)

        binding.taskDetailsTitle.text = args.job.name

        binding.backBtn.setOnClickListener {
            findNavController().navigateUp()
        }

        viewModel.getTaskDetails(taskId = args.job.id.toString())
        setObservers()
        setupUi()
    }

    private fun setupUi() = with(binding) {
        taskDetailsList.adapter = workAdapter
        val job = args.job
        taskStatus.text = job.isCompleted.toStatusString()
        if (job.isCompleted) {
            taskStatus.apply {
                setBackgroundResource(R.drawable.transparent_rectangle_green)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            }
        } else {
            taskStatus.apply {
                setBackgroundResource(R.drawable.transparent_rectangle_yellow)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.colorYellow))
            }
        }
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
                    workAdapter.submitList(task.assignedWorkers)
                    if (task.assignedWorkers.isEmpty()) {
                        taskDetailsEmpty.root.show()
                        taskDetailsEmpty.txtNotFound.text = getString(R.string.text_no_worker_found)
                    } else {
                        taskDetailsEmpty.root.hide()
                        taskDetailsEmpty.txtNotFound.hide()
                    }
                }
            }
        }
    }

    private fun toImageDetails(worker: AssignedWorker) {
        findNavController().navigate(CashForWorkTaskDetailsFragmentDirections.toCashForWorkImageFragment(
            taskId = worker.taskAssignment.taskId.toString(),
            taskName = args.job.name,
            userId = worker.taskAssignment.userId.toString()
        ))
    }

    private fun addWorker(worker: AssignedWorker) {
        // TODO: Add worker to task
        showToast("TODO: Add worker - ${worker.firstName} to task")
    }
}
