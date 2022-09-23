package com.richard.agyei

import com.fasterxml.jackson.annotation.JsonProperty

data class Photo(
    val id: Int,
    val title: String?,
    val url: String,
    @JsonProperty("thumbnailUrl")
    val thumbnailUrl: String
) {
}