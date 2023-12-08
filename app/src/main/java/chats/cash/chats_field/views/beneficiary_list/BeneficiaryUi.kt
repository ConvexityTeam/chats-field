package chats.cash.chats_field.views.beneficiary_list

data class BeneficiaryUi(
    val email: String?,
    val firstName: String?,
    val id: Int,
    val lastName: String?,
    val phone: String?,
    val profilePic: String?,
    val isAdded: Boolean = false,
)
