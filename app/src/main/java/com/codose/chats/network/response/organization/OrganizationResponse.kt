package com.codose.chats.network.response.organization

data class OrganizationResponse(
    val code: Int,
    val `data`: List<Organization>,
    val message: String,
    val status: String
)