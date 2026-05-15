package com.example.bankingapp.data.api

import com.example.bankingapp.data.model.notification.NotificationResponse
import com.example.bankingapp.data.model.notification.PagedNotificationResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificationsApi {

    @GET("api/notifications/unread-count")
    suspend fun getUnreadCount(): Int

    @GET("api/notifications")
    suspend fun getNotifications(
        @Query("page")     page: Int,
        @Query("pageSize") pageSize: Int
    ): PagedNotificationResponse

    @PUT("api/notifications/{id}/read")
    suspend fun markAsRead(@Path("id") id: Int): Response<ResponseBody>

    @PUT("api/notifications/read-all")
    suspend fun markAllAsRead(): Response<ResponseBody>

    @DELETE("api/notifications/{id}")
    suspend fun deleteNotification(@Path("id") id: Int): Response<ResponseBody>
}
