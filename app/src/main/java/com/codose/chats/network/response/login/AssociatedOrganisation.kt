package com.codose.chats.network.response.login

data class AssociatedOrganisation(
    val Organisation: Organisation,
    val OrganisationId: Int,
    val UserId: Int,
    val createdAt: String,
    val id: Int,
    val role: String,
    val updatedAt: String,
)