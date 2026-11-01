package com.example.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.Debt
import com.example.model.DebtType

@Entity(tableName = "debts")
data class DebtEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val personName: String,
    val type: String, // HUTANG, PIUTANG
    val totalAmount: Double,
    val savedAmount: Double,
    val dueDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val notes: String = ""
) {
    fun toDomainModel(): Debt {
        return Debt(
            id = id,
            title = title,
            personName = personName,
            type = try { DebtType.valueOf(type) } catch (e: Exception) { DebtType.HUTANG },
            totalAmount = totalAmount,
            savedAmount = savedAmount,
            dueDate = dueDate,
            createdAt = createdAt,
            notes = notes
        )
    }

    companion object {
        fun fromDomain(domain: Debt): DebtEntity {
            return DebtEntity(
                id = domain.id,
                title = domain.title,
                personName = domain.personName,
                type = domain.type.name,
                totalAmount = domain.totalAmount,
                savedAmount = domain.savedAmount,
                dueDate = domain.dueDate,
                createdAt = domain.createdAt,
                notes = domain.notes
            )
        }
    }
}
