package chats.cash.chats_field.views.campaignform

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
import androidx.recyclerview.widget.LinearLayoutManager
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.FragmentAnswerCampaignFormBinding
import chats.cash.chats_field.model.ModelCampaign
import chats.cash.chats_field.model.campaignform.CampaignForm
import chats.cash.chats_field.network.body.survey.SubmitSurveyAnswerBody
import chats.cash.chats_field.offline.OfflineViewModel
import chats.cash.chats_field.utils.safeNavigate
import chats.cash.chats_field.views.auth.viewmodel.RegisterViewModel
import chats.cash.chats_field.views.base.BaseFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import timber.log.Timber

class RegisterOptinCampaignFragment : BaseFragment() {

    lateinit var _binding: FragmentAnswerCampaignFormBinding
    val binding: FragmentAnswerCampaignFormBinding
        get() = _binding

    lateinit var answer: SubmitSurveyAnswerBody
    lateinit var form: CampaignForm

    var answersMap = mutableMapOf<Int, SubmitSurveyAnswerBody.QuestionAnswers>()

    private val offlineViewModel by activityViewModels<OfflineViewModel>()
    private val viewModel by activityViewModel<RegisterViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAnswerCampaignFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    val args: RegisterOptinCampaignFragmentArgs by navArgs()

    lateinit var firstName: String
    lateinit var lastname: String
    lateinit var email: String
    lateinit var password: String
    lateinit var phone: String
    lateinit var lat: String
    lateinit var long: String
    var organizationId: Int = 0
    lateinit var gender: String
    lateinit var date: String
    lateinit var pin: String
    lateinit var campaign: ModelCampaign

    var isInit = false

    lateinit var adapter: CampaignFormQuestionssAdapter

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
        //pin = args.pin
        campaign = viewModel.campaign!!
        lifecycleScope.launch {
            offlineViewModel.userCampaignForm.collect {
                if (it != null) {
                    Timber.d(it.toString())
                    if (it.questions.isNotEmpty()) {
                        form = it
                        isInit = true

                        adapter = CampaignFormQuestionssAdapter(
                            it.questions,
                            requireContext(),
                            showEnterValueError = {
                                showErrorSnackbar(getString(R.string.please_enter_a_valid_answer))
                            },
                            showSelectError = {
                                showErrorSnackbar(getString(R.string.please_select_a_valid_answer))
                            },
                        ) { index, answer ->
                            answersMap[index] = answer
                            showToast(getString(R.string.saved))
                        }

                        val layoutManager = LinearLayoutManager(
                            requireContext(),
                            LinearLayoutManager.VERTICAL,
                            false,
                        )
                        binding.questionsRecyclerView.layoutManager = layoutManager
                        binding.questionsRecyclerView.adapter = adapter
                    } else {
                        navigateWithError()
                    }
                } else {
                    navigateWithError(getString(R.string.no_questions))
                }
            }
        }

        addClickListeners()
    }

    private fun navigateWithError(title: String = getString(R.string.invalid_campaign_questions)) {
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
                //   pin = pin,
            ),
        )
        Toast.makeText(requireContext(), title, Toast.LENGTH_SHORT).show()
    }

    private fun addClickListeners() {
        campaign.title?.let {
            binding.appbar.title = campaign.title
        }

        binding.appbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.optinSubmitbutton.setOnClickListener {
            checkIfAllFieldsAreFilledCorrectly() {
                offlineViewModel.insertSurveryAnswer(answer)
                Timber.v(answer.toString())
                loading = false
                findNavController().safeNavigate(R.id.action_registerOptinCampaignFragment_to_registerImageFragment)
//                    RegisterOptinCampaignFragmentDirections .actionRegisterOptinCampaignFragmentToRegisterVerifyFragment(
//                        firstName = firstName,
//                        lastName = lastname,
//                        email = email,
//                        phone = phone,
//                        password = password,
//                        latitude = lat,
//                        longitude = long,
//                        organizationId = organizationId,
//                        gender = gender,
//                        date = date,
//                        //  pin = pin,
//                    ),
            }
        }
    }

    var loading = false
    private fun checkIfAllFieldsAreFilledCorrectly(isOkay: () -> Unit) {
        if (!loading) {
            loading = true

            if (answersMap.isEmpty()) {
                showErrorSnackbar(getString(R.string.no_answers_saved_yet))
                loading = false
                return
            }

            form.questions.forEachIndexed() { index, form ->
                if (form.required) {
                    if (answersMap[index] == null) {
                        showErrorSnackbar(
                            getString(
                                R.string.question_is_a_required_question_and_must_be_answered,
                                (index + 1).toString(),
                            ),
                        )
                        loading = false
                        return
                    }
                }
            }

            answer =
                SubmitSurveyAnswerBody(email, campaign.id, 0, form.id, answersMap.values.toList())
            isOkay()
        }
    }

    private fun showErrorSnackbar(error: String) {
        val snackbar = Snackbar.make(binding.root, error, Snackbar.LENGTH_SHORT)
        snackbar.setBackgroundTint(requireContext().getColor(R.color.design_default_color_error))
        snackbar.setTextColor(Color.WHITE)
        snackbar.show()
    }
}
