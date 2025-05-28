// User.kt
@file:OptIn(InternalSerializationApi::class)
package com.example.test_for_diplom
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val email: String,
    val fullname: String,
    @SerialName("program_id")
    val program_id: Int,
    val course: String,
    @SerialName("profile_completed")
    val profileCompleted: Boolean
)