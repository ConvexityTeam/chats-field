package com.codose.chats.views.cashForWork

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.navigation.fragment.findNavController
import com.codose.chats.R
import com.codose.chats.databinding.FragmentCashForWorkBinding
import com.codose.chats.utils.*
import com.codose.chats.utils.BluetoothConstants.ACTIVE_CASH_FOR_WORK
import com.codose.chats.views.auth.adapter.CashForWorkAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

class CashForWorkFragment : Fragment(R.layout.fragment_cash_for_work) {

    private var _binding: FragmentCashForWorkBinding? = null
    private val binding get() = _binding!!
    private val cashForWorkViewModel by viewModel<CashForWorkViewModel>()

    private val adapter: CashForWorkAdapter by lazy {
        CashForWorkAdapter {
            findNavController().navigate(CashForWorkFragmentDirections.actionCashForWorkFragmentToCashForWorkTaskFragment(
                cfwId = it.id.toString(),
                cfwName = it.title!!,
            ))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentCashForWorkBinding.bind(view)

        cashForWorkViewModel.getCashForWorks()

        binding.run {
            cfwRv.adapter = adapter
            cfwBackBtn.setOnClickListener{
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
                    adapter.submitList(it.filter { campaign ->
                        campaign.status.equals(ACTIVE_CASH_FOR_WORK, ignoreCase = true)
                    })
                }
            }
        }
    }
}
