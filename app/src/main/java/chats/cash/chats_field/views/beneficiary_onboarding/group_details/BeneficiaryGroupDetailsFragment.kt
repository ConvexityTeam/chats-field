package chats.cash.chats_field.views.beneficiary_onboarding.group_details

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.FragmentSelectGroupDetailsBinding
import chats.cash.chats_field.views.auth.viewmodel.RegisterViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import timber.log.Timber

class BeneficiaryGroupDetailsFragment :
    Fragment(R.layout.fragment_select_group_details),
    AdapterView.OnItemClickListener,
    AdapterView.OnItemSelectedListener {

    private var _binding: FragmentSelectGroupDetailsBinding? = null
    private val binding get() = _binding!!
    private val registerViewModel by activityViewModel<RegisterViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSelectGroupDetailsBinding.bind(view)
        setUpClickListeners()
        registerViewModel.resetGroupDetails()
    }

    private fun setUpClickListeners() {
        binding.groupDetailsAppbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        setUpDropDown()
        setUpContinueButton()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    val list by lazy { resources.getStringArray(R.array.group_drop_down) }
    var selectIndex = -1

    private fun setUpDropDown() {
        val dropdown: AutoCompleteTextView = binding.dropdown
        val adapter: ArrayAdapter<String> = ArrayAdapter(
            requireContext(),
            R.layout.item_beneficiary_group,
            list,
        )
        registerViewModel.registerBeneficiaryState.observe(viewLifecycleOwner) { state ->
            val selectedIndex = enumList.indexOfFirst { it.equals(state.category, true) }
            if (selectedIndex != -1) {
                dropdown.listSelection = selectedIndex
            }
        }
        dropdown.setAdapter(adapter)
        dropdown.onItemClickListener = this
        dropdown.onItemSelectedListener = this
    }

    private fun setUpContinueButton() {
        binding.groupNameText.addTextChangedListener {
            if (selectIndex != -1) {
                binding.groupNext.isEnabled = it.toString().length > 2
            }
        }

        binding.groupNext.setOnClickListener {
            val groupName = binding.groupNameText.text?.toString()
            Timber.v(groupName)
            val categoryValue = list.getOrNull(selectIndex)
            if (groupName.isNullOrEmpty() || groupName.length < 4) {
                binding.groupNameDropdownParent.error = "Enter a valid name"
                binding.groupNameDropdownParent.isErrorEnabled = true
            } else if (selectIndex == -1 || categoryValue.isNullOrEmpty()) {
                binding.dropdownParent.error = "Please select a category"
                binding.dropdownParent.isErrorEnabled = true
            } else {
                binding.groupNameDropdownParent.isErrorEnabled = false
                binding.dropdownParent.isErrorEnabled = false
                registerViewModel.updateRegisterBeneficiaryState_Stage1(categoryValue, groupName)
                findNavController().navigate(R.id.action_beneficiaryGroupDetailsFragment_to_addGroupBeneficiaryFragment)
            }
        }
    }

    val enumList = listOf("family", "community", "interest-group", "associations")

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val item = enumList.getOrNull(position)
        selectIndex = position
        item?.let {
            Timber.v(it)
            registerViewModel.updateBeneficiaryGroup(it)
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val item = enumList.getOrNull(position)
        selectIndex = position
        item?.let {
            Timber.v("selected $it")
            registerViewModel.updateBeneficiaryGroup(it)
            if (binding.groupNameText.text.isNullOrEmpty().not()) {
                binding.groupNext.isEnabled = true
            }
        } ?: kotlin.run {
            binding.groupNext.isEnabled = false
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        selectIndex = -1
    }
}
