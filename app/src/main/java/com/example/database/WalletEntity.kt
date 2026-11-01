package com.example.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.Wallet
import com.example.model.WalletType

@Entity(tableName = "wallets")
data class WalletEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val type: String, // CASH, BANK, EWALLET, etc.
    val initialBalance: Double,
    val colorHex: Long,
    val iconName: String,
    val accountNumber: String = ""
) {
    fun toDomainModel(currentBalance: Double): Wallet {
        return Wallet(
            id = id,
            name = name,
            type = try { WalletType.valueOf(type) } catch (e: Exception) { WalletType.OTHER },
            initialBalance = initialBalance,
            currentBalance = currentBalance,
            colorHex = colorHex,
            iconName = iconName,
            accountNumber = accountNumber
        )
    }

    companion object {
        fun fromDomain(wallet: Wallet): WalletEntity {
            return WalletEntity(
                id = wallet.id,
                name = wallet.name,
                type = wallet.type.name,
                initialBalance = wallet.initialBalance,
                colorHex = wallet.colorHex,
                iconName = wallet.iconName,
                accountNumber = wallet.accountNumber
            )
        }
    }
}
