package com.example.services

import com.example.model.Budget
import com.example.model.Category
import com.example.model.Transaction
import com.example.model.TransactionType
import com.example.utils.CurrencyUtils

data class FinancialHealthReport(
    val score: Int, // 0 - 100
    val status: String, // "Sangat Sehat", "Sehat", "Cukup", "Perlu Waspada"
    val summary: String,
    val savingsRatePercentage: Float,
    val needsRatioPercentage: Float,
    val wantsRatioPercentage: Float,
    val keyInsights: List<SmartInsight>
)

data class SmartInsight(
    val title: String,
    val description: String,
    val type: InsightType, // POSITIVE, WARNING, TIP
    val iconName: String
)

enum class InsightType {
    POSITIVE,
    WARNING,
    TIP
}

object SmartFinanceAdvisor {

    fun suggestCategory(title: String): Category? {
        val lower = title.lowercase().trim()
        if (lower.isEmpty()) return null

        return when {
            // Food & Drink
            containsAny(lower, "makan", "soto", "bakso", "kopi", "coffee", "cafe", "restoran", "warteg",
                "sarapan", "lunch", "dinner", "snack", "gofood", "grabfood", "indomaret", "alfamart",
                "roti", "mie", "kfc", "mcd", "burger", "pizza", "nasi", "es teh", "minum", "jajan") ->
                Category.getById("food")

            // Transport
            containsAny(lower, "bensin", "pertalite", "pertamax", "spbu", "shell", "grab", "gojek",
                "goride", "gocar", "tol", "parkir", "kereta", "krl", "mrt", "busway", "tj",
                "servis", "service", "oli", "tambal ban", "angkot", "tiket pesawat", "ojek") ->
                Category.getById("transport")

            // Bills & Utilities
            containsAny(lower, "listrik", "pln", "token", "air", "pdam", "wifi", "indihome", "biznet",
                "firstmedia", "pulsa", "kuota", "telkomsel", "indosat", "xl", "bpjs", "asuransi",
                "sewa", "kos", "kost", "kontrakan", "iuran", "ipl") ->
                Category.getById("bills")

            // Shopping
            containsAny(lower, "shopee", "tokopedia", "lazada", "tiktok shop", "baju", "sepatu",
                "skincare", "belanja", "supermarket", "mall", "celana", "tas", "buku", "furnitur") ->
                Category.getById("shopping")

            // Entertainment
            containsAny(lower, "netflix", "spotify", "youtube", "bioskop", "cinema", "xxi", "cgv",
                "game", "steam", "playstation", "topup", "top up", "nonton", "liburan", "hotel", "karaoke") ->
                Category.getById("entertainment")

            // Health
            containsAny(lower, "obat", "apotek", "dokter", "klinik", "rumah sakit", "vitamin",
                "medis", "faskes", "halodoc", "alodokter", "kacamata", "gigi") ->
                Category.getById("health")

            // Education
            containsAny(lower, "kursus", "kuliah", "spp", "sekolah", "udemy", "buku pelajaran",
                "les", "seminar", "workshop", "ujian") ->
                Category.getById("education")

            // Income - Salary
            containsAny(lower, "gaji", "payroll", "salary", "upah", "thr") ->
                Category.getById("salary")

            // Income - Freelance / Business
            containsAny(lower, "freelance", "proyek", "klien", "client", "omzet", "penjualan", "jasa") ->
                Category.getById("freelance")

            // Income - Investment
            containsAny(lower, "dividen", "reksadana", "saham", "crypto", "bibit", "bareksa", "bunga bank") ->
                Category.getById("investment")

            // Income - Bonus
            containsAny(lower, "bonus", "hadiah", "giveaway", "angpao", "cashback") ->
                Category.getById("bonus")

            // Transfer
            containsAny(lower, "transfer", "tarik tunai", "setor tunai", "top up gopay", "top up ovo",
                "pindah dana", "kirim saldo") ->
                Category.getById("transfer")

            else -> null
        }
    }

    private fun containsAny(text: String, vararg keywords: String): Boolean {
        return keywords.any { text.contains(it) }
    }

    fun analyzeFinancialHealth(
        transactions: List<Transaction>,
        budgets: List<Budget>
    ): FinancialHealthReport {
        val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val totalExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

        val savings = (totalIncome - totalExpense).coerceAtLeast(0.0)
        val savingsRate = if (totalIncome > 0) ((savings / totalIncome) * 100).toFloat() else 0f

        // Group expenses
        val expenseByCategory = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.categoryId }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        // Needs: Food, Transport, Bills, Health
        val needsKeys = setOf("food", "transport", "bills", "health", "family")
        val needsTotal = expenseByCategory.filter { it.key in needsKeys }.values.sum()
        val needsRatio = if (totalIncome > 0) ((needsTotal / totalIncome) * 100).toFloat() else 0f

        // Wants: Shopping, Entertainment, etc.
        val wantsTotal = totalExpense - needsTotal
        val wantsRatio = if (totalIncome > 0) ((wantsTotal / totalIncome) * 100).toFloat() else 0f

        // Check budget violations
        val overBudgets = budgets.filter { it.isOverBudget }
        val warningBudgets = budgets.filter { it.isWarningThreshold }

        // Score calculation
        var calculatedScore = 70

        if (totalIncome > 0) {
            // Savings rate reward (target 20%+)
            when {
                savingsRate >= 30f -> calculatedScore += 20
                savingsRate >= 20f -> calculatedScore += 15
                savingsRate >= 10f -> calculatedScore += 5
                savingsRate < 0f -> calculatedScore -= 25
                else -> calculatedScore -= 10
            }

            // Budget violations penalty
            calculatedScore -= (overBudgets.size * 12)
            calculatedScore -= (warningBudgets.size * 4)

            // Needs ratio assessment (target < 50%)
            if (needsRatio in 20f..55f) {
                calculatedScore += 10
            } else if (needsRatio > 70f) {
                calculatedScore -= 10
            }
        } else if (totalExpense > 0) {
            calculatedScore = 45
        }

        val finalScore = calculatedScore.coerceIn(15, 100)

        val status = when {
            finalScore >= 85 -> "Sangat Sehat"
            finalScore >= 70 -> "Sehat & Terkendali"
            finalScore >= 55 -> "Cukup Baik"
            else -> "Perlu Waspada"
        }

        val summary = when {
            finalScore >= 85 -> "Keuangan Anda bulan ini berada di tingkat optimal! Arus kas positif dan tabungan sangat aman."
            finalScore >= 70 -> "Kondisi keuangan cukup stabil. Lanjutkan disiplin pencatatan dan perhatikan batas anggaran kategori."
            finalScore >= 55 -> "Keuangan masih berimbang, namun ruang tabungan Anda relatif tipis. Coba tekan pengeluaran gaya hidup."
            else -> "Pengeluaran melebihi laju pemasukan atau beberapa anggaran terlampaui. Segera lakukan penyesuaian belanja!"
        }

        val insights = mutableListOf<SmartInsight>()

        // Savings insight
        if (savingsRate >= 20f) {
            insights.add(
                SmartInsight(
                    title = "Target Tabungan Tercapai",
                    description = "Rasio tabungan Anda sebesar ${String.format("%.1f", savingsRate)}% melebihi standar aman 20%. Pertahankan momentum ini!",
                    type = InsightType.POSITIVE,
                    iconName = "savings"
                )
            )
        } else if (totalIncome > 0 && savingsRate < 10f) {
            insights.add(
                SmartInsight(
                    title = "Tabungan Masih Rendah",
                    description = "Rasio tabungan Anda baru ${String.format("%.1f", savingsRate)}%. Usahakan menyisihkan minimal 10-20% di awal penerimaan gaji.",
                    type = InsightType.WARNING,
                    iconName = "warning"
                )
            )
        }

        // Budget insight
        if (overBudgets.isNotEmpty()) {
            val names = overBudgets.joinToString(", ") { Category.getById(it.categoryId).name }
            insights.add(
                SmartInsight(
                    title = "Anggaran Terlampaui!",
                    description = "Kategori $names telah melebihi batas bulanan yang Anda tetapkan. Hindari transaksi non-krusial di kategori ini.",
                    type = InsightType.WARNING,
                    iconName = "error"
                )
            )
        } else if (warningBudgets.isNotEmpty()) {
            val names = warningBudgets.joinToString(", ") { Category.getById(it.categoryId).name }
            insights.add(
                SmartInsight(
                    title = "Mendekati Batas Anggaran",
                    description = "Kategori $names sudah mencapai di atas 80% dari anggaran bulanan.",
                    type = InsightType.WARNING,
                    iconName = "notifications_active"
                )
            )
        } else {
            insights.add(
                SmartInsight(
                    title = "Anggaran Disiplin",
                    description = "Semua batas anggaran kategori Anda terpantau aman dan terkontrol dengan baik.",
                    type = InsightType.POSITIVE,
                    iconName = "check_circle"
                )
            )
        }

        // Top expense insight
        val topExpense = expenseByCategory.maxByOrNull { it.value }
        if (topExpense != null && topExpense.value > 0) {
            val cat = Category.getById(topExpense.key)
            val percent = if (totalExpense > 0) (topExpense.value / totalExpense) * 100 else 0.0
            insights.add(
                SmartInsight(
                    title = "Pengeluaran Terbesar: ${cat.name}",
                    description = "Kategori ini menyerap ${CurrencyUtils.formatRupiah(topExpense.value)} (${String.format("%.1f", percent)}% dari total pengeluaran Anda).",
                    type = InsightType.TIP,
                    iconName = "pie_chart"
                )
            )
        }

        // General smart tip
        insights.add(
            SmartInsight(
                title = "Aturan Emas 50/30/20",
                description = "Alokasikan 50% untuk kebutuhan pokok, 30% untuk keinginan, dan 20% langsung ke tabungan atau dana darurat.",
                type = InsightType.TIP,
                iconName = "lightbulb"
            )
        )

        return FinancialHealthReport(
            score = finalScore,
            status = status,
            summary = summary,
            savingsRatePercentage = savingsRate,
            needsRatioPercentage = needsRatio,
            wantsRatioPercentage = wantsRatio,
            keyInsights = insights
        )
    }
}
