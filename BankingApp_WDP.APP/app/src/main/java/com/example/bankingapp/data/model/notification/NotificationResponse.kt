package com.example.bankingapp.data.model.notification

import com.google.gson.annotations.SerializedName

data class NotificationResponse(
    @SerializedName("id")        val id: String,
    @SerializedName("type")      val type: String,
    @SerializedName("title")     val title: String,
    @SerializedName("message")   val message: String,
    @SerializedName("isRead")    val isRead: Boolean,
    @SerializedName("createdAt") val createdAt: String
)
