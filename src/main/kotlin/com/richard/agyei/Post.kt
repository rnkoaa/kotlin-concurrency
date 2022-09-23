package com.richard.agyei

import kotlinx.coroutines.Deferred
import retrofit2.http.GET
import retrofit2.http.Path

data class Post(
    val id: Int,
    val title: String,
    val body: String,
    val comments: List<Comment>?
) {
}

interface PostApi {

    @GET("/posts/{id}/comments")
    fun getPostCommentsAsync(@Path("id") id: Int): Deferred<List<Comment>>
}
