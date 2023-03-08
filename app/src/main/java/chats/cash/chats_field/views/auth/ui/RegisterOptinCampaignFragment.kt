package chats.cash.chats_field.views.auth.ui

import android.graphics.Color
import android.os.Bundle
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
import chats.cash.chats_field.model.campaignform.CampaignForm
import chats.cash.chats_field.network.body.survey.AnswerSurveyQuestionBody
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

    val answer = AnswerSurveyQuestionBody(null,null, emptyList())
    var firstQuestionAnwser = mutableListOf<String>()
    var firstQuestionReward = 0
    var secondQuestionReward = 0
    var secondQuestionAnwser = mutableListOf<String>()
    var thirdQuestionAnwser = mutableListOf<String>()
    lateinit var form:CampaignForm

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

                        form = it
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
                    secondQuestionAnwser.clear()
                    secondQuestionAnwser.add(binding.questionTwoOptionOne.text.toString())
                    secondQuestionReward = form.questions[1].question.options[0].reward.toInt()
                }
                R.id.question_two_option_two -> {
                    secondQuestionAnwser.clear()
                    secondQuestionAnwser.add(binding.questionTwoOptionTwo.text.toString())
                    secondQuestionReward = form.questions[1].question.options[1].reward.toInt()
                }

            }
        }
        campaign.title?.let {
            binding.title.text = requireContext().getString(R.string.opt_in,it)
        }

        binding.questionOneOptionOne.addOnCheckedStateChangedListener { checkBox, state ->
            val reward = form.questions[0].question.options[0].reward.toInt()
            if(checkBox.isChecked){
                if(!firstQuestionAnwser.contains(checkBox.text)) {
                    firstQuestionAnwser.add(checkBox.text.toString())
                    firstQuestionReward += reward
                }
            }
            else {
                if(firstQuestionAnwser.contains(checkBox.text)) {
                    firstQuestionAnwser.remove(checkBox.text.toString())
                    firstQuestionReward -= reward
                }
            }

        }
        binding.questionOneOptionTwo.addOnCheckedStateChangedListener { checkBox, state ->
            val reward = form.questions[0].question.options[1].reward.toInt()
            if(checkBox.isChecked){
                if(!firstQuestionAnwser.contains(checkBox.text)) {
                    firstQuestionAnwser.add(checkBox.text.toString())
                    firstQuestionReward += reward
                }
            }
            else {
                if(firstQuestionAnwser.contains(checkBox.text)) {
                    firstQuestionAnwser.remove(checkBox.text.toString())
                    firstQuestionReward -= reward
                }
            }
            Timber.v(firstQuestionAnwser.toString())
        }


        binding.backBtn.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.optinSubmitbutton.setOnClickListener {

            checkIfAllFieldsAreFilledCorrectly() {
                val answerBody = AnswerSurveyQuestionBody(null,form.id,
                    listOf(AnswerSurveyQuestionBody.Question(firstQuestionAnwser,form.questions[0].question.title,firstQuestionReward,form.questions[0].type),
                    AnswerSurveyQuestionBody.Question(secondQuestionAnwser,form.questions[1].question.title,secondQuestionReward,form.questions[1].type),
                AnswerSurveyQuestionBody.Question(listOf(binding.optinQuestionthree.text.toString()),form.questions[2].question.title,form.questions[2].value.toInt(),form.questions[2].type)),
                )
                Timber.v(answerBody.toString())
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

        if(firstQuestionAnwser.isEmpty()){
            showErrorSnackbar(requireContext().getString(R.string.please_select_an_option),binding.cardviewOne)
            return
        }
        if(secondQuestionAnwser.isEmpty()){
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
        thirdQuestionAnwser.add(binding.optinQuestionthree.text.toString())
        isOkay()

    }



    private fun showErrorSnackbar(error:String,view: View){
        binding.root.smoothScrollTo(0, binding.constraints.top + view.top);

        val snackbar = Snackbar.make(binding.root,error,Snackbar.LENGTH_SHORT)
        snackbar.setBackgroundTint(requireContext().getColor(R.color.design_default_color_error))
        snackbar.setTextColor(Color.WHITE)
        snackbar.show()
    }



}


