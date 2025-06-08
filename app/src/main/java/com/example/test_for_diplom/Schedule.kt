@file:OptIn(InternalSerializationApi::class)
package com.example.test_for_diplom

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable

@Serializable
data class Schedule(
    val course: Int,
    val program_id: Int,
    val day: String,
    val time_slot: String,     // <-- Обязательно такое имя
    val subject: String,
    val information: String
)