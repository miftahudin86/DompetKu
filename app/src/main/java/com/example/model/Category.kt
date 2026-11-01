package com.example.model

enum class TransactionType {
    EXPENSE, // Pengeluaran
    INCOME,  // Pemasukan
    TRANSFER // Transfer Antar Dompet
}

enum class CategoryGroup {
    EXPENSE,
    INCOME,
    TRANSFER
}

data class Category(
    val id: String,
    val name: String,
    val iconName: String,
    val colorHex: Long,
    val group: CategoryGroup
) {
    companion object {
        val DEFAULT_CATEGORIES = listOf(
            // Expense Categories
            Category("food", "Makanan & Minuman", "restaurant", 0xFFF97316, CategoryGroup.EXPENSE),
            Category("transport", "Transportasi", "directions_car", 0xFF3B82F6, CategoryGroup.EXPENSE),
            Category("shopping", "Belanja & Kebutuhan", "shopping_bag", 0xFFEC4899, CategoryGroup.EXPENSE),
            Category("bills", "Tagihan & Utilitas", "receipt_long", 0xFF8B5CF6, CategoryGroup.EXPENSE),
            Category("entertainment", "Hiburan & Hobi", "sports_esports", 0xFF06B6D4, CategoryGroup.EXPENSE),
            Category("health", "Kesehatan & Medis", "favorite", 0xFFEF4444, CategoryGroup.EXPENSE),
            Category("education", "Pendidikan", "school", 0xFF14B8A6, CategoryGroup.EXPENSE),
            Category("family", "Keluarga & Rumah", "home", 0xFFEAB308, CategoryGroup.EXPENSE),
            Category("other_expense", "Pengeluaran Lain", "category", 0xFF64748B, CategoryGroup.EXPENSE),

            // Income Categories
            Category("salary", "Gaji & Upah", "payments", 0xFF10B981, CategoryGroup.INCOME),
            Category("freelance", "Freelance & Bisnis", "work", 0xFF059669, CategoryGroup.INCOME),
            Category("investment", "Investasi & Dividen", "trending_up", 0xFF047857, CategoryGroup.INCOME),
            Category("bonus", "Bonus & Hadiah", "card_giftcard", 0xFF34D399, CategoryGroup.INCOME),
            Category("other_income", "Pemasukan Lain", "attach_money", 0xFF6EE7B7, CategoryGroup.INCOME),

            // Transfer Category
            Category("transfer", "Transfer Antar Dompet", "swap_horiz", 0xFF6366F1, CategoryGroup.TRANSFER)
        )

        fun getById(id: String): Category {
            return DEFAULT_CATEGORIES.firstOrNull { it.id == id }
                ?: Category("other", "Lainnya", "category", 0xFF64748B, CategoryGroup.EXPENSE)
        }
    }
}
