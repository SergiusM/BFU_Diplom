
package com.example.test_for_diplom

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Material(
    val id: Int,
    val file_name: String,
    val link: String,
    val course: Int,
    val program_id: Int,
    val user_username: String,
    val created_at: String,
    val subject_id: Int
)