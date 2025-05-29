@file:OptIn(InternalSerializationApi::class)
package com.example.test_for_diplom
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Subject(
    val id: Int,
    val name: String,
    val course: Int,
    val program_id: Int
)