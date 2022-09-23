package com.richard.agyei

import kotlinx.coroutines.Deferred
import retrofit2.http.GET
import retrofit2.http.Path

data class Album(
    val id: Int,
    val title: String,
    val photos: List<Photo>?
)

interface AlbumApi {

    @GET("/albums/{id}/photos")
    fun getAlbumPhotosAsync(@Path("id") id: Int): Deferred<List<Photo>>
}