package com.codose.chats.views.cashForWork

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.codose.chats.R
import com.codose.chats.utils.ApiResponse
import com.codose.chats.utils.hide
import com.codose.chats.utils.show
import com.codose.chats.utils.toast
import com.codose.chats.views.auth.adapter.TaskAdapter
import com.codose.chats.views.auth.adapter.TaskClickListener
import kotlinx.android.synthetic.main.fragment_cash_for_work_task.*
import kotlinx.android.synthetic.main.layout_empty.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel


class CashForWorkTaskFragment : Fragment() {
    private val cashForWorkViewModel by viewModel<CashForWorkViewModel>()

    private lateinit var adapter: TaskAdapter

    private lateinit var cfWId: String

    private lateinit var cfwName: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = CashForWorkTaskFragmentArgs.fromBundle(requireArguments())
        cfWId = args.cfwId
        cfwName = args.cfwName
        cashForWorkViewModel.getTask(cfWId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cash_for_work_task, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = TaskAdapter(TaskClickListener {
            findNavController().navigate(CashForWorkTaskFragmentDirections.actionCashForWorkTaskFragmentToCashForWorkTaskDetailsFragment(
                taskId = it.id.toString(),
                taskName = it.name,
            ))
        })

        cfw_task_title.text = cfwName

        cfw_task_back_btn.setOnClickListener{
            findNavController().navigateUp()
        }

        cfw_task_rv.adapter = adapter
        setObservers()
    }

    private fun setObservers() {
        cashForWorkViewModel.tasks.observe(viewLifecycleOwner, {
            when (it) {
                is ApiResponse.Failure -> {
                    cfw_task_progress.hide()
                    requireContext().toast(it.message)
                    findNavController().navigateUp()
                }
                is ApiResponse.Loading -> {
                    cfw_task_progress.show()
                }
                is ApiResponse.Success -> {
                    cfw_task_progress.hide()
                    val data = it.data
                    adapter.submitList(data.data.tasks)
                    if(data.data.tasks.isEmpty()){
                        cfw_task_empty.show()
                        cfw_task_empty.txt_not_found.text = "No tasks found"
                    }else{
                        cfw_task_empty.hide()
                    }
                }
            }
        })
    }
}