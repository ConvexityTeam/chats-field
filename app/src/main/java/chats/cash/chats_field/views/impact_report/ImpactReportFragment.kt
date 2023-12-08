package chats.cash.chats_field.views.impact_report

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.FragmentImpactReportDetailsBinding
import chats.cash.chats_field.views.auth.login.LoginViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class ImpactReportFragment :
    Fragment(R.layout.fragment_impact_report_details) {

    private var _binding: FragmentImpactReportDetailsBinding? = null
    private val loginViewModel by activityViewModel<LoginViewModel>()
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentImpactReportDetailsBinding.bind(view)
        init()
    }

    private fun init() {
        binding.impactReportAppbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.getStartedButton.setOnClickListener {
            findNavController().navigate(
                ImpactReportFragmentDirections.actionImpactReportFragmentToSelectCampaignFragment(false),
            )
        }
    }
}
