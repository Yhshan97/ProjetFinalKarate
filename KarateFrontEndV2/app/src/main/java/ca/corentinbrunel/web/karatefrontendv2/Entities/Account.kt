package ca.corentinbrunel.web.karatefrontendv2.Entities

data class Account(
    val email: String,
    val fullName: String,
    val avatar: String,
    val role: String,
    val groupe: String,
    val points: Int,
    val credits: Int
) {
    var sessionId: String? = null
}