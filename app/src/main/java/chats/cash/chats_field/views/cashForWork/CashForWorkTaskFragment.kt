package chats.cash.chats_field.views.cashForWork

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.core.view.isGone
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.FragmentCashForWorkTaskBinding
import chats.cash.chats_field.utils.safeNavigate
import chats.cash.chats_field.views.auth.adapter.JobAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

class CashForWorkTaskFragment : Fragment(R.layout.fragment_cash_for_work_task) {

    private var _binding: FragmentCashForWorkTaskBinding? = null
    private val binding get() = _binding!!
    private val cashForWorkViewModel by viewModel<CashForWorkViewModel>()
    private val args by navArgs<CashForWorkTaskFragmentArgs>()

    private val adapter: JobAdapter by lazy {
        JobAdapter(onReportClick = {
            findNavController().safeNavigate(
                CashForWorkTaskFragmentDirections.toCashForWorkTaskDetailsFragment(it)
            )
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentCashForWorkTaskBinding.bind(view)

        binding.cfwTaskBackBtn.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.cfwTaskRv.adapter = adapter
        val tasks = args.jobs.toList()
        binding.cfwTaskEmpty.root.isGone = tasks.isNotEmpty()
        adapter.submitList(tasks)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
