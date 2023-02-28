package chats.cash.chats_field.views.auth.ui

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.FragmentRegisterOptInBinding
import chats.cash.chats_field.model.ModelCampaign
import chats.cash.chats_field.offline.OfflineViewModel
import chats.cash.chats_field.utils.safeNavigate
import chats.cash.chats_field.views.base.BaseFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import timber.log.Timber

class RegisterOptinCampaignFragment : BaseFragment() {


    lateinit var _binding:FragmentRegisterOptInBinding
    val binding:FragmentRegisterOptInBinding
        get() = _binding

    private val offlineViewModel by activityViewModels<OfflineViewModel>()

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

        lifecycleScope.launch {

            offlineViewModel.userCampaignForm.collect {
                if(it!=null) {
                    Timber.d(it.toString())
                    if (it.questions.size >= 3) {
                        val first = it.questions[0]
                        val two = it.questions[1]
                        val three = it.questions[2]


                        binding.cardviewOne.visibility = View.VISIBLE
                        binding.cardviewTwo.visibility = View.VISIBLE
                        binding.cardviewThree.visibility = View.VISIBLE


                        binding.questionOneTitle.text = first.question.title
                        binding.questionTwoTitle.text = two.question.title
                        binding.questionThreeTitle.text = three.question.title


                        binding.questionOneOptionOne.text = first.question.options[0].option
                        binding.questionOneOptionTwo.text = first.question.options[1].option

                        binding.questionTwoOptionOne.text = two.question.options[0].option
                        binding.questionTwoOptionTwo.text = two.question.options[1].option

                    }

                    else{
                        navigateWithError()
                    }
                }
                else{
                    navigateWithError("no questions")
                }


            }
        }

        addClickListeners()

        }

    private val questiononeanswer = mutableListOf<String>()
    private var questiontwo_answer = ""


    private fun navigateWithError(title:String = "Invalid campaign questions"){
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
                pin = pin,
                campaign = campaign,
            )
        )
        Toast.makeText(requireContext(),title,Toast.LENGTH_SHORT).show()
    }
    private fun addClickListeners() {

        binding.optinRadiogroup.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId){
                R.id.question_two_option_one -> {
                    questiontwo_answer = binding.questionTwoOptionOne.text.toString()
                }
                R.id.question_two_option_two -> {
                    questiontwo_answer = binding.questionTwoOptionTwo.text.toString()
                }

            }
        }
        campaign.title?.let {
            binding.title.text = requireContext().getString(R.string.opt_in,it)
        }

        binding.questionOneOptionOne.addOnCheckedStateChangedListener { checkBox, state ->
            onViewMediumClicked(Mediums.Newspaper.value)
        }
        binding.questionOneOptionTwo.addOnCheckedStateChangedListener { checkBox, state ->
            onViewMediumClicked(Mediums.Tv.value)
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
                        pin = pin,
                        campaign = campaign,
                    )
                )
            }

        }

    }



    private fun checkIfAllFieldsAreFilledCorrectly(isOkay:()->Unit){

        if(questiononeanswer.isEmpty()){
            showErrorSnackbar(requireContext().getString(R.string.please_select_an_option),binding.cardviewOne)
            return
        }
        if(questiontwo_answer.isEmpty()){
            showErrorSnackbar(getStringResource(R.string.please_select_an_option),
                binding.cardviewTwo)
            return
        }

        if(binding.optinQuestionthree.text.isNullOrBlank()){
            showErrorSnackbar(getStringResource(R.string.enter_a_value),binding.optinQuestionthreeParent)
            binding.optinQuestionthreeParent.apply {
                this.error = getStringResource(R.string.enter_a_value)
                isErrorEnabled = true
            }
            return
        }
        binding.optinQuestionthreeParent.apply {
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
        Timber.d(questiononeanswer.toString())
        if(questiononeanswer.contains(value)){
            Timber.d("exists removing")
            questiononeanswer.remove(value)
        }
        else{
            Timber.d("no exists adding")
            questiononeanswer.add(value)
        }
    }

}


private enum class Mediums(val value:Int){
    Tv(R.string.tv),
    Twitter(R.string.twitter),
    Newspaper(R.string.newspaper)
}

