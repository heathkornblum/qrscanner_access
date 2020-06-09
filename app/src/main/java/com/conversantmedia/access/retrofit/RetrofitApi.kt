package com.conversantmedia.access.retrofit

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

interface RetrofitApi {

    @FormUrlEncoded
    @POST("accounts/authenticateUser")
    fun authenticateUser(
        @Field("userName") userName: String,
        @Field("password") password: String
    ): Call<DefaultResponse>

    @POST("accounts/validate")
    fun validateUser(
    ): Call<ValidationResponse>

}