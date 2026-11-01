package com.example.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object CurrencyUtils {

    private val indonesianSymbols = DecimalFormatSymbols(Locale("id", "ID")).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }

    private val rupiahFormatter = DecimalFormat("#,###", indonesianSymbols)

    fun formatRupiah(amount: Double): String {
        val formatted = rupiahFormatter.format(amount)
        return "Rp $formatted"
    }

    fun formatSignedRupiah(amount: Double, isExpense: Boolean): String {
        val prefix = if (isExpense) "- " else "+ "
        val formatted = rupiahFormatter.format(amount)
        return "$prefix Rp $formatted"
    }

    fun formatCompactRupiah(amount: Double): String {
        return when {
            amount >= 1_000_000_000 -> {
                val value = amount / 1_000_000_000
                String.format(Locale("id", "ID"), "Rp %.1f M", value)
            }
            amount >= 1_000_000 -> {
                val value = amount / 1_000_000
                String.format(Locale("id", "ID"), "Rp %.1f jt", value)
            }
            amount >= 1_000 -> {
                val value = amount / 1_000
                String.format(Locale("id", "ID"), "Rp %.0f rb", value)
            }
            else -> formatRupiah(amount)
        }
    }

    fun parseAmount(input: String): Double {
        val cleanString = input.replace("[^0-9]".toRegex(), "")
        return cleanString.toDoubleOrNull() ?: 0.0
    }
}
