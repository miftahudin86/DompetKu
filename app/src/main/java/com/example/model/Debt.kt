package com.example.model

enum class DebtType {
    HUTANG,  // Hutang Saya (Saya berhutang / kewajiban membayar)
    PIUTANG  // Piutang Saya (Orang lain berhutang ke saya)
}

data class Debt(
    val id: Long = 0,
    val title: String,
    val personName: String,
    val type: DebtType,
    val totalAmount: Double,
    val savedAmount: Double = 0.0, // Dana yang sudah ditabung / dicicil untuk pelunasan
    val dueDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val notes: String = ""
) {
    val remainingAmount: Double
        get() = (totalAmount - savedAmount).coerceAtLeast(0.0)

    val progressPercentage: Float
        get() = if (totalAmount > 0) ((savedAmount / totalAmount) * 100).toFloat().coerceIn(0f, 100f) else 0f

    val isSettled: Boolean
        get() = savedAmount >= totalAmount
}
