@file:OptIn(InternalSerializationApi::class)
package com.example.test_for_diplom
import kotlinx.serialization.InternalSerializationApi

import kotlinx.serialization.Serializable

@Serializable
data class GroupLink(
    val id: Int,
    val link: String,
    val title: String,
    val course: Int? = null,
    val program_id: Int? = null
)