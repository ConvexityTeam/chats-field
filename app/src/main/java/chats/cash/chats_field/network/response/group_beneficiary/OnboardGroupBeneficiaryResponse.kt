package chats.cash.chats_field.network.response.group_beneficiary

import com.google.gson.annotations.SerializedName

data class OnboardGroupBeneficiaryResponse(
    @SerializedName("code")
    val code: Int,
    @SerializedName("data")
    val response: OnboardGroupBeneficiaryData,
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: String,
) {
    data class OnboardGroupBeneficiaryData(
        @SerializedName("address")
        val address: Any,
        @SerializedName("bvn")
        val bvn: Any,
        @SerializedName("country")
        val country: Any,
        @SerializedName("createdAt")
        val createdAt: String,
        @SerializedName("currency")
        val currency: Any,
        @SerializedName("device_imei")
        val deviceImei: Any,
        @SerializedName("dob")
        val dob: String,
        @SerializedName("email")
        val email: String,
        @SerializedName("first_name")
        val firstName: String,
        @SerializedName("gender")
        val gender: String,
        @SerializedName("group")
        val group: Group,
        @SerializedName("id")
        val id: Int,
        @SerializedName("iris")
        val iris: Any,
        @SerializedName("is_bvn_verified")
        val isBvnVerified: Boolean,
        @SerializedName("is_email_verified")
        val isEmailVerified: Boolean,
        @SerializedName("is_nin_verified")
        val isNinVerified: Boolean,
        @SerializedName("is_phone_verified")
        val isPhoneVerified: Boolean,
        @SerializedName("is_public")
        val isPublic: Boolean,
        @SerializedName("is_self_signup")
        val isSelfSignup: Boolean,
        @SerializedName("is_tfa_enabled")
        val isTfaEnabled: Boolean,
        @SerializedName("last_login")
        val lastLogin: Any,
        @SerializedName("last_name")
        val lastName: String,
        @SerializedName("location")
        val location: Any,
        @SerializedName("marital_status")
        val maritalStatus: Any,
        @SerializedName("members")
        val members: List<Member>,
        @SerializedName("nfc")
        val nfc: Any,
        @SerializedName("nin")
        val nin: Any,
        @SerializedName("password")
        val password: String,
        @SerializedName("phone")
        val phone: String,
        @SerializedName("pin")
        val pin: Any,
        @SerializedName("profile_pic")
        val profilePic: String,
        @SerializedName("referal_id")
        val referalId: Any,
        @SerializedName("RoleId")
        val roleId: Int,
        @SerializedName("status")
        val status: String,
        @SerializedName("tfa_secret")
        val tfaSecret: Any,
        @SerializedName("updatedAt")
        val updatedAt: String,
        @SerializedName("username")
        val username: String,
        @SerializedName("vendor_id")
        val vendorId: Any,
    ) {
        data class Group(
            @SerializedName("createdAt")
            val createdAt: String,
            @SerializedName("group_category")
            val groupCategory: String,
            @SerializedName("group_name")
            val groupName: String,
            @SerializedName("id")
            val id: Int,
            @SerializedName("representative_id")
            val representativeId: Int,
            @SerializedName("updatedAt")
            val updatedAt: String,
        )

        data class Member(
            @SerializedName("createdAt")
            val createdAt: String,
            @SerializedName("dob")
            val dob: String,
            @SerializedName("full_name")
            val fullName: String,
            @SerializedName("group_id")
            val groupId: Int,
            @SerializedName("id")
            val id: Int,
            @SerializedName("profile_pic")
            val profilePic: String,
            @SerializedName("updatedAt")
            val updatedAt: String,
        )
    }
}
