package chats.cash.chats_field.views.beneficiary_onboarding.add_group_beneficiaries

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.BeneficiaryAddGroupMemberBinding
import chats.cash.chats_field.utils.safeNavigate
import chats.cash.chats_field.utils.toFile
import chats.cash.chats_field.views.auth.viewmodel.BeneficiaryMembers
import chats.cash.chats_field.views.auth.viewmodel.RegisterViewModel
import chats.cash.chats_field.views.beneficiary_onboarding.add_group_beneficiaries.adapter.AddBeneficiaryGroupsAdapter
import chats.cash.chats_field.views.core.showErrorSnackbar
import chats.cash.chats_field.views.faceCapture.ConvexityFaceCaptureActivityCaptureActivity
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import timber.log.Timber

class AddGroupBeneficiaryFragment : Fragment(R.layout.beneficiary_add_group_member) {

    private val registerViewModel by activityViewModel<RegisterViewModel>()

    private var _binding: BeneficiaryAddGroupMemberBinding? = null

    private val resultLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { intent ->
        if (intent.resultCode == RESULT_OK) {
            val imageUri = intent.data?.data
            Timber.v(imageUri.toString())
            imageUri?.let {
                val currentEditingData = registerViewModel.getCurrentEditingBeneficiaryDetails()
                if (currentEditingData == null) {
                    showErrorSnackbar(
                        R.string.photo_capture_failed2,
                        binding.root,
                    )
                    return@let
                } else {
                    imageUri.toFile(requireContext())
                        ?.let { it1 ->
                            //registerViewModel.uploadImage(it1.path, "simontest${Calendar.getInstance().time.time}")
                            registerViewModel.mcallBack.invoke(it1)
                            registerViewModel.editItemFromAddedGroupBeneficiaries(
                                currentEditingData.first.copy(picture = it1),
                                currentEditingData.second,
                            ) {
                                // onDone()
                            }
                        }
                }
            }
        } else {
            showErrorSnackbar(R.string.photo_capture_failed, binding.root)
        }
    }

    private val adapter: AddBeneficiaryGroupsAdapter by lazy {
        AddBeneficiaryGroupsAdapter(
            binding.root,
            lifecycleScope,
            requireContext(),
            onChange = {},
            onEditItem = { i: Int, beneficiaryMembers: BeneficiaryMembers ->
                registerViewModel.editItemFromAddedGroupBeneficiaries(beneficiaryMembers, i) {
                }
            },
            onDeleteItem = {
                registerViewModel.removeItemFromAddedGroupBeneficiaries(it) {
                    adapter.notifyItemRemoved(it)
                }
            },
            onDone = { index, bene ->
                registerViewModel.editItemFromAddedGroupBeneficiaries(bene, index) {
                }
            },

        ) { beneficiary, index, onClick ->
            registerViewModel.initPropsForImageSelect(beneficiary, index, onClick)
            val cameraIntent =
                Intent(requireContext(), ConvexityFaceCaptureActivityCaptureActivity::class.java)
            resultLauncher.launch(cameraIntent)
        }
    }

    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = BeneficiaryAddGroupMemberBinding.bind(view)
        setUpClickListeners()
        setUpRecyclerView()
    }

    private fun setUpClickListeners() {
        binding.addGroupAppbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.addNewButton.setOnClickListener {
            try {
                val currentIndex = binding.membersAddedCount.text.toString().toInt()
                registerViewModel.addItemToAddedGroupBeneficiaries(BeneficiaryMembers()) {
                    adapter.notifyItemInserted(currentIndex)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setUpRecyclerView() {
        val layoutManger = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, true)
        binding.beneficiariesRecyclerview.layoutManager = layoutManger
        binding.beneficiariesRecyclerview.adapter = adapter
        registerViewModel.addedGroupBeneficiaries.observe(viewLifecycleOwner) {
            Timber.v(it.toString())
            adapter.updateData(it)
            binding.membersAddedCount.text = it.size.toString()
            binding.continueButton.isEnabled = it.isNotEmpty()
        }

        binding.continueButton.setOnClickListener {
            if (adapter.getItems().any { it.name.isEmpty() || it.dob.isEmpty() || it.picture == null }) {
                showErrorSnackbar(R.string.incomplete_or_invalid_information_entered, binding.root)
                return@setOnClickListener
            }
            findNavController().safeNavigate(R.id.action_addGroupBeneficiaryFragment_to_registerGroupFragment)
        }
    }
}
