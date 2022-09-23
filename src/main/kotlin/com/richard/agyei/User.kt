package com.richard.agyei

import kotlinx.coroutines.Deferred
import retrofit2.http.GET
import retrofit2.http.Path

data class Address(
    val street: String,
    val suite: String?,
    val city: String,
    val zipcode: String,
    val geo: Map<String, String>?
)

data class User(
    val id: Int,
    val name: String,
    val email: String?,
    val phone: String?,
    val website: String?,
    val address: Address?,
    val company: Map<String, String>?
)

interface UserApi {

    @GET("/users")
    fun getAllAsync(): Deferred<List<User>>

    @GET("/users/{id}")
    fun getByIdAsync(@Path("id") id: Int): Deferred<User>

    @GET("/users/{id}/posts")
    fun getUserPostsAsync(@Path("id") id: Int): Deferred<List<Post>>

    @GET("/users/{id}/todos")
    fun getUserTodosAsync(@Path("id") id: Int): Deferred<List<Todo>>

    @GET("/users/{id}/albums")
    fun getUserAlbumsAsync(@Path("id") id: Int): Deferred<List<Album>>
}