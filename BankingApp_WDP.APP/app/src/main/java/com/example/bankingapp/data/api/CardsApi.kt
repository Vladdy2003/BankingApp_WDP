package com.example.bankingapp.data.api

import com.example.bankingapp.data.model.card.CardResponse
import retrofit2.http.GET

interface CardsApi {
    @GET("api/cards")
    suspend fun getCards(): List<CardResponse>
}
