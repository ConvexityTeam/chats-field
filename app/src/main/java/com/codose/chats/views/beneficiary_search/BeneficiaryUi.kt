package com.codose.chats.views.beneficiary_search

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BeneficiaryUi(
    val dob: String?,
    val email: String?,
    val firstName: String?,
    val gender: String?,
    val id: Int,
    val lastName: String?,
    val maritalStatus: String?,
    val phone: String?,
    val profilePic: String?
): Parcelable
