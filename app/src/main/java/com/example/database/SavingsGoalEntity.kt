package com.example.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.SavingsGoal

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val targetDate: Long,
    val colorHex: Long,
    val iconName: String,
    val notes: String = ""
) {
    fun toDomainModel(): SavingsGoal {
        return SavingsGoal(
            id = id,
            title = title,
            targetAmount = targetAmount,
            currentAmount = currentAmount,
            targetDate = targetDate,
            colorHex = colorHex,
            iconName = iconName,
            notes = notes
        )
    }

    companion object {
        fun fromDomain(domain: SavingsGoal): SavingsGoalEntity {
            return SavingsGoalEntity(
                id = domain.id,
                title = domain.title,
                targetAmount = domain.targetAmount,
                currentAmount = domain.currentAmount,
                targetDate = domain.targetDate,
                colorHex = domain.colorHex,
                iconName = domain.iconName,
                notes = domain.notes
            )
        }
    }
}
