package com.example.bankingapp.data.model.notification

import com.google.gson.annotations.SerializedName

data class PagedNotificationResponse(
    @SerializedName("page")     val page: Int,
    @SerializedName("pageSize") val pageSize: Int,
    @SerializedName("total")    val total: Int,
    @SerializedName("items")    val items: List<NotificationResponse>
)
