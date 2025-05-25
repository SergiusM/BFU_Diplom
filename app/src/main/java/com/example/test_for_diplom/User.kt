// User.kt
package com.example.test_for_diplom

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val email: String,
    val fullname: String,
    val studyfield: String,
    val course: String,
    @SerialName("profile_completed")
    val profileCompleted: Boolean
)