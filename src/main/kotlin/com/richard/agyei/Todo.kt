package com.richard.agyei

import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.coroutines.Deferred
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path

object TodoFactory {

    fun createTodoClient(retrofit: Retrofit): TodoClient {
        return retrofit.newBuilder()
            .build()
            .create(TodoClient::class.java)
    }
}

data class Todo(
    @JsonProperty("userId")
    val userId: Int,
    val id: String,
    val title: String,
    val completed: Boolean
) {
}

interface TodoClient {

    @GET("/todos")
    fun getAllAsync(): Deferred<List<Todo>>

    @GET("todos/{id}")
    fun getByIdAsync(@Path("id") id: Int): Deferred<Todo>
}