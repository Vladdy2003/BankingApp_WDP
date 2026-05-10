package com.example.bankingapp.data.repository

import com.example.bankingapp.data.model.notification.NotificationResponse
import com.example.bankingapp.data.network.RetrofitClient

class NotificationsRepository {

    suspend fun getNotifications(page: Int, pageSize: Int = 20): List<NotificationResponse> =
        RetrofitClient.notificationsApi.getNotifications(page, pageSize).items

    suspend fun markAsRead(id: Int) {
        val response = RetrofitClient.notificationsApi.markAsRead(id)
        if (!response.isSuccessful) throw retrofit2.HttpException(response)
    }

    suspend fun markAllAsRead() {
        val response = RetrofitClient.notificationsApi.markAllAsRead()
        if (!response.isSuccessful) throw retrofit2.HttpException(response)
    }

    suspend fun deleteNotification(id: Int) {
        val response = RetrofitClient.notificationsApi.deleteNotification(id)
        if (!response.isSuccessful) throw retrofit2.HttpException(response)
    }
}
