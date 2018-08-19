package com.oscar.api.gist.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * The data Class that represent the Gist information
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Gist(
        val id: String = "",
        val description: String = "",
        val public: Boolean = true,
        val files: HashMap<String, File>? = null
)