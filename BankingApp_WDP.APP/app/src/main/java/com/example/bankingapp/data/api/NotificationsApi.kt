package com.example.bankingapp.data.api

import retrofit2.http.GET

interface NotificationsApi {
    @GET("api/notifications/unread-count")
    suspend fun getUnreadCount(): Int
}
