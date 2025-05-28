@file:OptIn(InternalSerializationApi::class)

package com.example.test_for_diplom

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable

@Serializable
data class Program(
    val id: Int,
    val name: String,
    val code: String

)