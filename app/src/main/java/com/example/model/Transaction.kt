package com.example.model

data class Transaction(
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val categoryId: String,
    val walletId: String,
    val toWalletId: String? = null, // Used for TRANSFER type
    val timestamp: Long,
    val notes: String = "",
    val receiptUri: String? = null
)
