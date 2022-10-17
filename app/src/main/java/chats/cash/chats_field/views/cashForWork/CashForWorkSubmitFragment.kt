package chats.cash.chats_field.views.cashForWork

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.FragmentCashForWorkSubmitBinding
import chats.cash.chats_field.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class CashForWorkSubmitFragment : Fragment(R.layout.fragment_cash_for_work_submit) {

    private var _binding: FragmentCashForWorkSubmitBinding? = null
    private val binding get() = _binding!!
    private val cashForWorkViewModel by viewModel<CashForWorkViewModel>()
    private val args by navArgs<CashForWorkSubmitFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCashForWorkSubmitBinding.bind(view)

        binding.apply {
            cfwSubmitName.text = args.userName
            cfwSubmitTitle.text = args.taskName
        }

        setupClickListeners()
        setObservers()
    }

    private fun setObservers() = with(binding) {
        cashForWorkViewModel.taskOperation.observe(viewLifecycleOwner) {
            when (it) {
                is ApiResponse.Failure -> {
                    cfwSubmitProgress.root.hide()
                    requireContext().toast(it.message)
                    findNavController().navigateUp()
                }
                is ApiResponse.Loading -> {
                    cfwSubmitProgress.root.show()
                }
                is ApiResponse.Success -> {
                    cfwSubmitProgress.root.hide()
                    val data = it.data
                    requireContext().toast(data.message)
                    findNavController().navigate(CashForWorkSubmitFragmentDirections.toOnboardingFragment())
                }
            }
        }
    }

    private fun setupClickListeners() = with(binding) {
        cfwSubmitBackBtn.setOnClickListener {
            findNavController().navigateUp()
        }

        cfwSubmitBtn.setOnClickListener {
            cashForWorkViewModel.postTaskCompleted(args.taskId, args.userId)
        }

        cfwPictureCard.setOnClickListener {
            findNavController().navigate(CashForWorkSubmitFragmentDirections.toCashForWorkImageFragment(
                taskId = args.taskId,
                userId = args.userId,
                taskName = args.taskName,
                beneficiaryId = args.beneficiaryId
            ))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
