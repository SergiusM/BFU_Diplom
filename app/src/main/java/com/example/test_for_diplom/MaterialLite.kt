
package com.example.test_for_diplom

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MaterialLite(
    val file_name: String? = null,
    val link: String? = null,
    val course: Int? = null,
    val program_id: Int? = null,
    val subject_id: Int? = null
)