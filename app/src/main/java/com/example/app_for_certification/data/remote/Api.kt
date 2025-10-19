package com.example.app_for_certification.data.remote

import com.example.app_for_certification.data.model.CountryDto
import retrofit2.http.GET
import retrofit2.http.Query

interface Api {
    @GET("v3.1/all")
    suspend fun getAll(@Query("fields") fields: String): List<CountryDto>

    @GET("v3.1/alpha")
    suspend fun getByAlphaQuery(
        @Query("codes") codes: String,
        @Query("fields") fields: String
    ): List<CountryDto>
}