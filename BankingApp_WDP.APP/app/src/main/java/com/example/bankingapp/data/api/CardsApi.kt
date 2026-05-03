package com.example.bankingapp.data.api

import com.example.bankingapp.data.model.card.CardResponse
import com.example.bankingapp.data.model.card.CreateCardRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface CardsApi {
    @GET("api/cards")
    suspend fun getCards(): List<CardResponse>

    @POST("api/cards")
    suspend fun createCard(@Body request: CreateCardRequest): CardResponse

    @PUT("api/cards/{id}/block")
    suspend fun blockCard(@Path("id") id: String): Response<ResponseBody>

    @PUT("api/cards/{id}/unblock")
    suspend fun unblockCard(@Path("id") id: String): Response<ResponseBody>
}
