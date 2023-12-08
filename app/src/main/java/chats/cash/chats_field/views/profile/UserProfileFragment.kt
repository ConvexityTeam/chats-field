package chats.cash.chats_field.views.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.FragmentAgentProfileBinding
import chats.cash.chats_field.views.auth.login.LoginViewModel
import chats.cash.chats_field.views.auth.ui.loadGlide
import chats.cash.chats_field.views.core.dialogs.showAlertDialog
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class UserProfileFragment :
    Fragment(R.layout.fragment_agent_profile) {

    private var _binding: FragmentAgentProfileBinding? = null
    private val loginViewModel by activityViewModel<LoginViewModel>()
    private val binding get() = _binding!!

    private val user by lazy { loginViewModel.getUserProfile() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAgentProfileBinding.bind(view)
        init()
    }

    private fun init() {
        binding.impactReportAppbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.apply {
            user?.profilePic?.let {
                profilePhoto.loadGlide(user!!.profilePic)
            }
            name.text = "${user?.firstName} ${user?.lastName}"
            this.role.text = getString(R.string.role, "field agent")
            this.ngo.setText(user?.associatedOrganisations?.firstOrNull()?.Organisation?.name ?: "")
            this.phonenumberEdit.setText(user?.phone)

            logout.setOnClickListener {
                requireContext().showAlertDialog(
                    R.string.logging_you_out,
                    R.string.are_you_sure_you_want_to_log_out,
                ) {
                    loginViewModel.logout()
                }.show()
            }
        }
    }
}
