package chats.cash.chats_field.views.cashForWork

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.FragmentCashForWorkBinding
import chats.cash.chats_field.utils.hide
import chats.cash.chats_field.utils.safeNavigate
import chats.cash.chats_field.utils.show
import chats.cash.chats_field.views.auth.adapter.CashForWorkAdapter
import chats.cash.chats_field.views.cashForWork.model.Job
import org.koin.androidx.viewmodel.ext.android.viewModel

class CashForWorkFragment : Fragment(R.layout.fragment_cash_for_work) {

    private var _binding: FragmentCashForWorkBinding? = null
    private val binding get() = _binding!!
    private val cashForWorkViewModel by viewModel<CashForWorkViewModel>()

    private val adapter: CashForWorkAdapter by lazy {
        CashForWorkAdapter(
            onLoadTaskClick = ::toCashForWorkTask,
            onBeneficiaryClick = ::toBeneficiary,
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentCashForWorkBinding.bind(view)

        cashForWorkViewModel.getCashForWorks()

        binding.apply {
            cfwRv.adapter = adapter
            cfwBackBtn.setOnClickListener {
                findNavController().navigateUp()
            }
            cfwProgress.root.show()
        }
        setObservers()
    }

    private fun setObservers() {
        cashForWorkViewModel.cashForWorkCampaign.observe(viewLifecycleOwner) {
            if (it != null) {
                if (it.isEmpty()) {
                    binding.cfwEmpty.root.show()
                    binding.cfwEmpty.txtNotFound.text = getString(R.string.empty_cash_for_work)
                } else {
                    binding.cfwProgress.root.hide()
                    adapter.submitList(
                        it,
                    )
                }
            }
        }
    }

    private fun toCashForWorkTask(jobs: List<Job>) {
        findNavController().safeNavigate(
            CashForWorkFragmentDirections.toCashForWorkTaskFragment(jobs.toTypedArray()),
        )
    }

    private fun toBeneficiary(campaignId: Int) {
        findNavController().safeNavigate(
            CashForWorkFragmentDirections.toBeneficiaryListFragment(campaignId),
        )
    }
}
