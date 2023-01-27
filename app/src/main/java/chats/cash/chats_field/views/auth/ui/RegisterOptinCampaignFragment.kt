package chats.cash.chats_field.views.auth.ui

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.FragmentRegisterOptInBinding
import chats.cash.chats_field.model.ModelCampaign
import chats.cash.chats_field.utils.safeNavigate
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber

class RegisterOptinCampaignFragment : Fragment() {


    lateinit var _binding:FragmentRegisterOptInBinding
    val binding:FragmentRegisterOptInBinding
        get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentRegisterOptInBinding.inflate(inflater,container,false)
        return binding.root
    }

    val args:RegisterOptinCampaignFragmentArgs by navArgs()

    lateinit var firstName:String
    lateinit var lastname:String
    lateinit var email:String
    lateinit var password:String
    lateinit var phone:String
    lateinit var lat:String
    lateinit var long:String
    var organizationId:Int=0
    lateinit var gender:String
    lateinit var date:String
    lateinit var pin:String
    lateinit var campaign:ModelCampaign

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firstName = args.firstName
        lastname = args.lastName
        email = args.email
        password = args.password
        phone = args.phone
        lat = args.latitude
        long = args.longitude
        organizationId = args.organizationId
        gender = args.gender
        date = args.date
        pin = args.pin
        campaign = args.campaign

        addClickListeners()

        }

    private val selectedMediums = mutableListOf<String>()
    private var ageGroup = ""

    private fun addClickListeners() {

        binding.optinRadiogroup.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId){
                R.id.age1217 -> {
                    ageGroup = binding.age1217.text.toString()
                }
                R.id.age1828 -> {
                    ageGroup = binding.age1828.text.toString()
                }
                R.id.age2942 -> {
                    ageGroup = binding.age2942.text.toString()
                }
                else -> {
                    ageGroup = binding.age2942.text.toString()
                }
            }
        }
        campaign.title?.let {
            binding.title.text = requireContext().getString(R.string.opt_in,it)
        }

        binding.newspaper.addOnCheckedStateChangedListener { checkBox, state ->
            onViewMediumClicked(Mediums.Newspaper.value)
        }
        binding.tv.addOnCheckedStateChangedListener { checkBox, state ->
            onViewMediumClicked(Mediums.Tv.value)
        }
        binding.twitter.addOnCheckedStateChangedListener { checkBox, state ->
            onViewMediumClicked(Mediums.Twitter .value)
        }
        binding.newspaper2.addOnCheckedStateChangedListener { checkBox, state ->
            onViewMediumClicked(Mediums.Newspaper.value)
        }

        binding.backBtn.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.optinSubmitbutton.setOnClickListener {

            checkIfAllFieldsAreFilledCorrectly() {

                findNavController().safeNavigate(
                    RegisterOptinCampaignFragmentDirections.actionRegisterOptinCampaignFragmentToRegisterVerifyFragment(
                        firstName = firstName,
                        lastName = lastname,
                        email = email,
                        phone = phone,
                        password = password,
                        latitude = lat,
                        longitude = long,
                        organizationId = organizationId,
                        gender = gender,
                        date = date,
                        pin = pin
                    )
                )
            }

        }

    }



    private fun checkIfAllFieldsAreFilledCorrectly(isOkay:()->Unit){

        if(selectedMediums.isEmpty()){
            showErrorSnackbar(requireContext().getString(R.string.please_tell_us_how_you_heared_about),binding.cardviewHowdidyouhear)
            return
        }
        if(ageGroup.isEmpty()){
            showErrorSnackbar(requireContext().getString(R.string.please_tell_your_age),
                binding.cardviewAgebracket)
            return
        }

        if(binding.optinMothermaidenname.text.isNullOrBlank()){
            showErrorSnackbar(requireContext().getString(R.string.please_tell_your_mother_maiden_name),binding.optinMothermaidennameParent)
            binding.optinMothermaidennameParent.apply {
                this.error = requireContext().getString(R.string.please_tell_your_mother_maiden_name)
                isErrorEnabled = true
            }
            return
        }
        binding.optinMothermaidennameParent.apply {
            error = ""
            isErrorEnabled=false
        }

        isOkay()

    }



    private fun showErrorSnackbar(error:String,view: View){
        binding.root.smoothScrollTo(0, binding.constraints.top + view.top);

        val snackbar = Snackbar.make(binding.root,error,Snackbar.LENGTH_SHORT)
        snackbar.setBackgroundTint(requireContext().getColor(R.color.design_default_color_error))
        snackbar.setTextColor(Color.WHITE)
        snackbar.show()
    }


    private fun onViewMediumClicked(id:Int){
        val value = requireContext().getString(id)
        Timber.d(selectedMediums.toString())
        if(selectedMediums.contains(value)){
            Timber.d("exists removing")
            selectedMediums.remove(value)
        }
        else{
            Timber.d("no exists adding")
            selectedMediums.add(value)
        }
    }

}


private enum class Mediums(val value:Int){
    Tv(R.string.tv),
    Twitter(R.string.twitter),
    Newspaper(R.string.newspaper)
}