package com.codose.chats.network.response.login


import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("address")
    val address: String,
    @SerializedName("bvn")
    val bvn: Any?,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("dob")
    val dob: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("gender")
    val gender: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("is_bvn_verified")
    val isBvnVerified: Boolean,
    @SerializedName("is_email_verified")
    val isEmailVerified: Boolean,
    @SerializedName("is_phone_verified")
    val isPhoneVerified: Boolean,
    @SerializedName("is_public")
    val isPublic: Boolean,
    @SerializedName("is_self_signup")
    val isSelfSignup: Boolean,
    @SerializedName("is_tfa_enabled")
    val isTfaEnabled: Boolean,
    @SerializedName("last_login")
    val lastLogin: Any?,
    @SerializedName("last_name")
    val lastName: String,
    @SerializedName("lat")
    val lat: Any?,
    @SerializedName("location")
    val location: String,
    @SerializedName("long")
    val long: Any?,
    @SerializedName("marital_status")
    val maritalStatus: Any?,
    @SerializedName("nfc")
    val nfc: Any?,
    @SerializedName("nin")
    val nin: Any?,
    @SerializedName("password")
    val password: String,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("pin")
    val pin: Any?,
    @SerializedName("profile_pic")
    val profilePic: String,
    @SerializedName("referal_id")
    val referalId: Any?,
    @SerializedName("RoleId")
    val roleId: Int,
    @SerializedName("status")
    val status: String,
    @SerializedName("tfa_secret")
    val tfaSecret: Any?,
    @SerializedName("updatedAt")
    val updatedAt: String,
    @SerializedName("AssociatedOrganisations")
    val associatedOrganisations: List<AssociatedOrganisation>,
)