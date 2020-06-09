package com.conversantmedia.access.retrofit

data class DefaultResponse (
    val userDisplayName: String,
    val token: String,
    val fileHash: String,
    val firstName: String,
    val defaultCompanyDisplayName: String,
    val defaultCompanyId: Int,
    val defaultCompanyMatchbackName: String,
    val canSeeManageClientsButton: Boolean,
    val canSeeMatchbackFiles: Boolean,
    val canOverrideEmailWhiteList: Boolean,
    val canManageClient: Boolean,
    val canSeeAllClients: Boolean,
    val canManageClientUser: Boolean,
    val canSeeAllClientUsers: Boolean,
    val canImpersonateClientUsers: Boolean
)

data class ValidationResponse (
    val userName: String
)

object TokenHolder {
    var token: String? = null
}