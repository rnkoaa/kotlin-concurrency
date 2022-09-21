package com.richard.agyei

import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
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
            .addInterceptor(
                HttpLoggingInterceptor()
                    .setLevel(HttpLoggingInterceptor.Level.BASIC)
            )
            .build()
    }
}

fun main(args: Array<String>): Unit = runBlocking {
    println("Hello World!")

    val objectMapper = RetrofitProvider.objectMapper()
    val okHttpClient = RetrofitProvider.provideOkhttpClient()
    val retrofit = RetrofitProvider.provideRetrofit(objectMapper, okHttpClient)

    val todoClient = TodoFactory.createTodoClient(retrofit)
    val userApi = UserApiFactory.createUserApi(retrofit)
    val channel = Channel<Todo>()

    val todos = todoClient.getAllAsync().await()
    todos.forEach { todo ->
        launch {
            channel.send(todo)
        }
    }

    CoroutineScope(Dispatchers.IO).launch {
        channel.consumeEach {
            launch {
                val user = userApi.getByIdAsync(it.userId)
                    .await()
                println("${Thread.currentThread().name} -> $it, $user")
            }
        }
    }
}