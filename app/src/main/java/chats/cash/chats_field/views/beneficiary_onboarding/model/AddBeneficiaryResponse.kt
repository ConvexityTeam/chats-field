package chats.cash.chats_field.views.beneficiary_onboarding.model

data class AddBeneficiaryResponse(
    val CampaignId: Int,
    val UserId: Int,
    val approved: Boolean,
    val createdAt: String,
    val rejected: Boolean,
    val source: String,
    val updatedAt: String
)