package com.richard.agyei

import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit


object RetrofitProvider {

    fun objectMapper(): ObjectMapper {
        val objectMapper = ObjectMapper()
            .findAndRegisterModules()
            .registerKotlinModule()

        objectMapper.propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
        objectMapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)

        // Write times as a String instead of a Long so its human-readable.
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        objectMapper.registerModule(JavaTimeModule())
        objectMapper.registerModule(Jdk8Module())

        return objectMapper
    }

    fun provideRetrofit(objectMapper: ObjectMapper, okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://jsonplaceholder.typicode.com")
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .addConverterFactory(JacksonConverterFactory.create(objectMapper))
            .client(okHttpClient)
            .build()
    }

    fun provideOkhttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .retryOnConnectionFailure(false)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
//            .addInterceptor(
//                HttpLoggingInterceptor()
//                    .setLevel(HttpLoggingInterceptor.Level.BASIC)
//            )
            .build()
    }
}

object ApiClientProvider {

    fun provideTodoApi(retrofit: Retrofit): TodoApi {
        return retrofit.create(TodoApi::class.java)
    }

    fun providerUserApi(retrofit: Retrofit): UserApi {
        return retrofit.create(UserApi::class.java)
    }

    fun providerAlbumApi(retrofit: Retrofit): AlbumApi {
        return retrofit.create(AlbumApi::class.java)
    }

    fun providerPostApi(retrofit: Retrofit): PostApi {
        return retrofit.create(PostApi::class.java)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
fun CoroutineScope.getUsers(userClient: UserApi): ReceiveChannel<User> = produce {
    val users = userClient.getAllAsync().await()
    if (users.isNotEmpty()) {
        for (user in users) {
            send(user)
        }
    }
}

suspend fun fetchUserDetails(user: User, userApi: UserApi, albumApi: AlbumApi, postApi: PostApi): UserDetail {
    return withContext(Dispatchers.IO) {
        val userAlbums = withContext(Dispatchers.IO) {
            val albums = userApi.getUserAlbumsAsync(user.id).await()
            albums.map { album ->
                val albumPhotos = withContext(Dispatchers.IO) {
                    albumApi.getAlbumPhotosAsync(album.id).await()
                }

                album.copy(photos = albumPhotos)
            }
        }
        val userPosts = withContext(Dispatchers.IO) {
            val posts = userApi.getUserPostsAsync(user.id).await()
            posts.map { post ->
                val postComments = withContext(Dispatchers.IO) {
                    postApi.getPostCommentsAsync(post.id).await()
                }

                post.copy(comments = postComments)
            }
        }

        val userTodos = withContext(Dispatchers.IO) {
            userApi.getUserTodosAsync(user.id).await()
        }


        UserDetail(user, userTodos, userPosts, userAlbums)
    }
}

fun main(args: Array<String>): Unit = runBlocking {

    val objectMapper = RetrofitProvider.objectMapper()
    val okHttpClient = RetrofitProvider.provideOkhttpClient()
    val retrofit = RetrofitProvider.provideRetrofit(objectMapper, okHttpClient)

    val userApi = ApiClientProvider.providerUserApi(retrofit)
    val postApi = ApiClientProvider.providerPostApi(retrofit)
    val albumApi = ApiClientProvider.providerAlbumApi(retrofit)
    val userChannel = getUsers(userApi)

    CoroutineScope(Dispatchers.Default).launch {
        userChannel.consumeEach { user ->
            val userDetail = fetchUserDetails(user, userApi, albumApi, postApi)

            val userDetailJson = objectMapper.writeValueAsString(userDetail)
            File("${user.id}.json").writeText(userDetailJson)
            println("Done with User ${user.id}")
            println("/********************************************/")
        }
    }
}