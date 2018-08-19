package com.oscar.api.gist.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Data class that represents the Gist File information
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class File(
        val filename: String? = null,
        val content: String? = null,
        val truncated: Boolean? = null
)