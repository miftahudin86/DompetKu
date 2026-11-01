package com.example.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.Transaction
import com.example.model.TransactionType

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: String, // EXPENSE, INCOME, TRANSFER
    val categoryId: String,
    val walletId: String,
    val toWalletId: String? = null,
    val timestamp: Long,
    val notes: String = "",
    val receiptUri: String? = null
) {
    fun toDomainModel(): Transaction {
        return Transaction(
            id = id,
            title = title,
            amount = amount,
            type = try { TransactionType.valueOf(type) } catch (e: Exception) { TransactionType.EXPENSE },
            categoryId = categoryId,
            walletId = walletId,
            toWalletId = toWalletId,
            timestamp = timestamp,
            notes = notes,
            receiptUri = receiptUri
        )
    }

    companion object {
        fun fromDomain(domain: Transaction): TransactionEntity {
            return TransactionEntity(
                id = domain.id,
                title = domain.title,
                amount = domain.amount,
                type = domain.type.name,
                categoryId = domain.categoryId,
                walletId = domain.walletId,
                toWalletId = domain.toWalletId,
                timestamp = domain.timestamp,
                notes = domain.notes,
                receiptUri = domain.receiptUri
            )
        }
    }
}
