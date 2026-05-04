package com.example.bankingapp.data.model.transaction

import com.google.gson.annotations.SerializedName

data class PagedTransactionResponse(
    @SerializedName("page")     val page: Int,
    @SerializedName("pageSize") val pageSize: Int,
    @SerializedName("total")    val total: Int,
    @SerializedName("items")    val items: List<TransactionResponse>
)
