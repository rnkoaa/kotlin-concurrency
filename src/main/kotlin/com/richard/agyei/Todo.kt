package com.richard.agyei

import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.coroutines.Deferred
import retrofit2.http.GET
import retrofit2.http.Path

data class Todo(
    @JsonProperty("userId")
    val userId: Int,
    val id: String,
    val title: String,
    val completed: Boolean
) {
}

interface TodoApi {

    @GET("/todos")
    fun getAllAsync(): Deferred<List<Todo>>

    @GET("todos/{id}")
    fun getByIdAsync(@Path("id") id: Int): Deferred<Todo>
}