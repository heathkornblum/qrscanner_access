package com.conversantmedia.access.retrofit

import android.util.Base64
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

object RetrofitClient {

    private const val BASE_URL = "https://access-be-stage.conversantmedia.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor{
            val original = it.request()

            if (TokenHolder.token.isNullOrEmpty()) {
                val requestBuilder = original.newBuilder()
                    .addHeader("Origin", "https://access-stage.conversantmedia.com")
                    .method(original.method(), original.body())
                val request = requestBuilder.build()
                it.proceed(request)
            } else {
                val requestBuilder = original.newBuilder()
                    .addHeader("Origin", "https://access-stage.conversantmedia.com")
                    .addHeader("Authorization", TokenHolder.token!!)
                val request = requestBuilder.build()
                it.proceed(request)
            }

        }.build()


    val instance: RetrofitApi by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()

        retrofit.create(RetrofitApi::class.java)
    }
}