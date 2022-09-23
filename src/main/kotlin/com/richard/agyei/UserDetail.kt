package com.richard.agyei

data class UserDetail(
    val user: User,
    val todos: List<Todo>,
    val posts: List<Post>,
    val albums: List<Album>
) {
}