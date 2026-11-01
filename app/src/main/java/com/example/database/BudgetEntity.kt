package com.example.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.Budget

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val categoryId: String,
    val monthlyLimit: Double,
    val month: Int,
    val year: Int
) {
    fun toDomainModel(spentAmount: Double): Budget {
        return Budget(
            id = id,
            categoryId = categoryId,
            monthlyLimit = monthlyLimit,
            month = month,
            year = year,
            spentAmount = spentAmount
        )
    }

    companion object {
        fun fromDomain(domain: Budget): BudgetEntity {
            return BudgetEntity(
                id = domain.id,
                categoryId = domain.categoryId,
                monthlyLimit = domain.monthlyLimit,
                month = domain.month,
                year = domain.year
            )
        }
    }
}
