package com.example.model

data class Budget(
    val id: Long = 0,
    val categoryId: String,
    val monthlyLimit: Double,
    val month: Int, // 1 - 12
    val year: Int,
    val spentAmount: Double = 0.0
) {
    val remainingAmount: Double
        get() = monthlyLimit - spentAmount

    val percentageSpent: Float
        get() = if (monthlyLimit > 0) ((spentAmount / monthlyLimit) * 100).toFloat() else 0f

    val isOverBudget: Boolean
        get() = spentAmount > monthlyLimit

    val isWarningThreshold: Boolean
        get() = percentageSpent >= 80f && !isOverBudget
}

data class SavingsGoal(
    val id: Long = 0,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val targetDate: Long, // timestamp
    val colorHex: Long,
    val iconName: String,
    val notes: String = ""
) {
    val progressPercentage: Float
        get() = if (targetAmount > 0) ((currentAmount / targetAmount) * 100).toFloat().coerceIn(0f, 100f) else 0f

    val isCompleted: Boolean
        get() = currentAmount >= targetAmount

    val remainingAmount: Double
        get() = (targetAmount - currentAmount).coerceAtLeast(0.0)
}
