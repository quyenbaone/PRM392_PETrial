package com.example.trannguyenquyen_qe170062.data.api

import retrofit2.http.GET
import retrofit2.http.Query

data class ApiResponse(
    val page: Int,
    val per_page: Int,
    val total: Int,
    val total_pages: Int,
    val data: List<ApiStudent>
)

data class ApiStudent(
    val id: Int,
    val email: String,
    val first_name: String,
    val last_name: String,
    val avatar: String
)

interface StudentApi {
    @GET("users")
    suspend fun getStudents(@Query("page") page: Int = 1): ApiResponse
} 