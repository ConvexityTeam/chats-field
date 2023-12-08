package chats.cash.chats_field.views.campaignform

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import chats.cash.chats_field.R
import chats.cash.chats_field.databinding.ItemCampaignFormQuestionBinding
import chats.cash.chats_field.model.campaignform.CampaignQuestion
import chats.cash.chats_field.network.body.survey.SubmitSurveyAnswerBody
import chats.cash.chats_field.utils.hide
import chats.cash.chats_field.utils.show
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.radiobutton.MaterialRadioButton

class CampaignFormQuestionssAdapter(
    private val questions: List<CampaignQuestion>,
    private val context: Context,
    private val showSelectError: () -> Unit,
    private val showEnterValueError: () -> Unit,
    val onDone: (Int, SubmitSurveyAnswerBody.QuestionAnswers) -> Unit,
) : RecyclerView.Adapter<CampaignFormQuestionssAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            ItemCampaignFormQuestionBinding.bind(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_campaign_form_question, parent, false),
            ),
        )
    }

    override fun getItemCount(): Int {
        return questions.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(questions[position])
    }

    inner class MyViewHolder(private val binding: ItemCampaignFormQuestionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CampaignQuestion) {
            when (item.type) {
                MULTIPLE_TYPE -> {
                    setUpMultipleType(item, bindingAdapterPosition, binding)
                }

                OPTIONAL_TYPE -> {
                    setUpOptionalType(item, bindingAdapterPosition, binding)
                }

                SHORT_TYPE -> {
                    setUpShortType(item, bindingAdapterPosition, binding)
                }
            }
        }
    }

    private fun setUpMultipleType(
        item: CampaignQuestion,
        position: Int,
        binding: ItemCampaignFormQuestionBinding,
    ) {
        binding.apply {
            multipleCardview.show()
            optionCardview.hide()
            shortCardview.hide()
            multipleQuestion.text = item.question.title
            if (item.required) {
                multipleRequired.show()
            }
            item.question.options.forEachIndexed() { index, option ->
                val checkbox = MaterialCheckBox(context)
                checkbox.text = option.option
                multipleOptions.addView(checkbox, index)
            }

            binding.multipleDone.setOnClickListener {
                binding.multipleDone.isEnabled = false

                val checkedAnswers = mutableListOf<String>()
                val rewards = mutableListOf<Int>()

                multipleOptions.children.forEachIndexed { index, child ->
                    val checkbox = child as MaterialCheckBox
                    if (checkbox.isChecked) {
                        checkedAnswers.add(checkbox.text.toString())
                        rewards.add(item.question.options[index].reward.toInt())
                    }
                }

                if (checkedAnswers.isEmpty()) {
                    showSelectError()
                    binding.multipleDone.isEnabled = true
                    return@setOnClickListener
                }

                val answer = SubmitSurveyAnswerBody.QuestionAnswers(
                    checkedAnswers,
                    item.question.title,
                    rewards,
                    item.type,
                )

                onDone(position, answer)
                binding.multipleDone.isEnabled = true
                return@setOnClickListener
            }
        }
    }

    private fun setUpOptionalType(
        item: CampaignQuestion,
        position: Int,
        binding: ItemCampaignFormQuestionBinding,
    ) {
        binding.apply {
            optionCardview.show()
            multipleCardview.hide()
            shortCardview.hide()
            if (item.required) {
                optionalRequired.show()
            }
            optionalQuestion.text = item.question.title
            item.question.options.forEachIndexed() { index, option ->
                val radioButton = MaterialRadioButton(context)
                radioButton.text = option.option
                radioButton.id = index
                optionalRadiogroup.addView(radioButton, index)
            }

            optionDone.setOnClickListener {
                optionDone.isEnabled = false

                val checkedAnswers = mutableListOf<String>()
                val rewards = mutableListOf<Int>()

                val checkedIndex = optionalRadiogroup.checkedRadioButtonId
                if (checkedIndex < 0) {
                    showSelectError()
                    binding.optionDone.isEnabled = true
                    return@setOnClickListener
                }

                val selectedOption = item.question.options[checkedIndex]
                checkedAnswers.add(selectedOption.option)
                rewards.add(selectedOption.reward.toInt())

                if (checkedAnswers.isEmpty()) {
                    showSelectError()
                    binding.optionDone.isEnabled = true
                    return@setOnClickListener
                }

                val answer = SubmitSurveyAnswerBody.QuestionAnswers(
                    checkedAnswers,
                    item.question.title,
                    rewards,
                    item.type,
                )

                onDone(position, answer)
                binding.optionDone.isEnabled = true
                return@setOnClickListener
            }
        }
    }

    private fun setUpShortType(
        item: CampaignQuestion,
        position: Int,
        binding: ItemCampaignFormQuestionBinding,
    ) {
        binding.apply {
            shortCardview.show()
            optionCardview.hide()
            multipleCardview.hide()
            shortQuestion.text = item.question.title

            if (item.required) {
                shortRequired.show()
            }

            shortDone.setOnClickListener {
                shortDone.isEnabled = false

                val shortAnswer = shortAnswer.text.toString()

                if (shortAnswer.isEmpty()) {
                    showEnterValueError()
                    binding.shortDone.isEnabled = true
                    return@setOnClickListener
                }

                val answer = SubmitSurveyAnswerBody.QuestionAnswers(
                    listOf(shortAnswer),
                    item.question.title,
                    listOf(item.value.toInt()),
                    item.type,
                )

                onDone(position, answer)
                binding.shortDone.isEnabled = true
                return@setOnClickListener
            }
        }
    }

    fun getItems(): List<CampaignQuestion> {
        return questions
    }

    private val MULTIPLE_TYPE = "multiple"
    private val OPTIONAL_TYPE = "optional"
    val SHORT_TYPE = "short"
}
