package chats.cash.chats_field.network.response.vendor

import com.google.gson.annotations.SerializedName

data class VendorOnboardingResponse(
    @SerializedName("code")
    val code: Int,
    @SerializedName("data")
    val response: VendorResponseData,
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: String?,
) {
    data class VendorResponseData(
        @SerializedName("address")
        val address: String?,
        @SerializedName("bvn")
        val bvn: Any?,
        @SerializedName("country")
        val country: Any?,
        @SerializedName("createdAt")
        val createdAt: String?,
        @SerializedName("currency")
        val currency: Any?,
        @SerializedName("device_imei")
        val deviceImei: Any?,
        @SerializedName("dob")
        val dob: Any?,
        @SerializedName("email")
        val email: String?,
        @SerializedName("first_name")
        val firstName: String?,
        @SerializedName("gender")
        val gender: Any?,
        @SerializedName("id")
        val id: Int?,
        @SerializedName("is_bvn_verified")
        val isBvnVerified: Boolean?,
        @SerializedName("is_email_verified")
        val isEmailVerified: Boolean?,
        @SerializedName("is_nin_verified")
        val isNinVerified: Boolean?,
        @SerializedName("is_phone_verified")
        val isPhoneVerified: Boolean?,
        @SerializedName("is_public")
        val isPublic: Boolean?,
        @SerializedName("is_self_signup")
        val isSelfSignup: Boolean?,
        @SerializedName("is_tfa_enabled")
        val isTfaEnabled: Boolean?,
        @SerializedName("last_login")
        val lastLogin: Any?,
        @SerializedName("last_name")
        val lastName: String?,
        @SerializedName("location")
        val location: String?,
        @SerializedName("marital_status")
        val maritalStatus: Any?,
        @SerializedName("nfc")
        val nfc: Any?,
        @SerializedName("nin")
        val nin: Any?,
        @SerializedName("phone")
        val phone: String?,
        @SerializedName("profile_pic")
        val profilePic: Any?,
        @SerializedName("referal_id")
        val referalId: Any?,
        @SerializedName("RoleId")
        val roleId: Int?,
        @SerializedName("status")
        val status: String?,
        @SerializedName("Store")
        val store: Store?,
        @SerializedName("updatedAt")
        val updatedAt: String?,
        @SerializedName("username")
        val username: Any?,
        @SerializedName("vendor_id")
        val vendorId: String?,
    ) {
        data class Store(
            @SerializedName("address")
            val address: String?,
            @SerializedName("createdAt")
            val createdAt: String?,
            @SerializedName("id")
            val id: Int?,
            @SerializedName("location")
            val location: String?,
            @SerializedName("store_name")
            val storeName: String?,
            @SerializedName("updatedAt")
            val updatedAt: String?,
            @SerializedName("UserId")
            val userId: Int?,
        )
    }
}
