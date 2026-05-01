package com.example.bankingapp.data.repository

import com.example.bankingapp.data.model.notification.NotificationResponse
import com.example.bankingapp.data.network.RetrofitClient

class NotificationsRepository {

    suspend fun getNotifications(page: Int, pageSize: Int = 20): List<NotificationResponse> =
        RetrofitClient.notificationsApi.getNotifications(page, pageSize)

    suspend fun markAsRead(id: String) {
        val response = RetrofitClient.notificationsApi.markAsRead(id)
        if (!response.isSuccessful) throw retrofit2.HttpException(response)
    }

    suspend fun markAllAsRead() {
        val response = RetrofitClient.notificationsApi.markAllAsRead()
        if (!response.isSuccessful) throw retrofit2.HttpException(response)
    }

    suspend fun deleteNotification(id: String) {
        val response = RetrofitClient.notificationsApi.deleteNotification(id)
        if (!response.isSuccessful) throw retrofit2.HttpException(response)
    }
}
