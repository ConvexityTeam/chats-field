package chats.cash.chats_field.network.response.organization

data class Organization(
    val contact_address: String,
    val contact_name: String,
    val contact_phone: String,
    val createdAt: String,
    val email: String,
    val id: Int,
    val is_individual: Boolean,
    val location: String,
    val logo_link: String,
    val name: String,
    val phone: String,
    val updatedAt: String,
) {
    override fun toString(): String {
        return name
    }
}
