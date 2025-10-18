package com.example.app_for_certification.data.remote

import android.content.Context
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import kotlin.getValue

object RetrofitClient {
    private const val BASE_URL = "https://restcountries.com/"
    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val cache by lazy {
        Cache(File(appContext.cacheDir, "http_cache"), 10L * 1024 * 1024) // 10MB
    }

    private val client by lazy {
        OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(logging)
            .build()
    }

    val retrofitInstance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}