package com.example.bankingapp.data.repository

import com.example.bankingapp.data.model.card.CardResponse
import com.example.bankingapp.data.model.card.CreateCardRequest
import com.example.bankingapp.data.network.RetrofitClient

class CardsRepository {
    suspend fun getCards(): List<CardResponse> =
        RetrofitClient.cardsApi.getCards()

    suspend fun createCard(request: CreateCardRequest): CardResponse =
        RetrofitClient.cardsApi.createCard(request)

    suspend fun blockCard(id: String) {
        val response = RetrofitClient.cardsApi.blockCard(id)
        if (!response.isSuccessful) throw retrofit2.HttpException(response)
    }

    suspend fun unblockCard(id: String) {
        val response = RetrofitClient.cardsApi.unblockCard(id)
        if (!response.isSuccessful) throw retrofit2.HttpException(response)
    }
}
