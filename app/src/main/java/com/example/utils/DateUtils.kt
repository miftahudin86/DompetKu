package com.example.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

    private val indonesianLocale = Locale("id", "ID")

    private val fullDateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", indonesianLocale)
    private val shortDateFormat = SimpleDateFormat("d MMM yyyy", indonesianLocale)
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", indonesianLocale)
    private val timeFormat = SimpleDateFormat("HH:mm", indonesianLocale)
    private val dayMonthFormat = SimpleDateFormat("d MMMM", indonesianLocale)

    fun formatRelativeDate(timestamp: Long): String {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply { timeInMillis = timestamp }

        val isSameYear = now.get(Calendar.YEAR) == target.get(Calendar.YEAR)
        val isSameDay = isSameYear && now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)

        if (isSameDay) {
            return "Hari ini, ${timeFormat.format(Date(timestamp))}"
        }

        val yesterday = (now.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
        val isYesterday = yesterday.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                yesterday.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)

        if (isYesterday) {
            return "Kemarin, ${timeFormat.format(Date(timestamp))}"
        }

        return if (isSameYear) {
            "${dayMonthFormat.format(Date(timestamp))}, ${timeFormat.format(Date(timestamp))}"
        } else {
            "${shortDateFormat.format(Date(timestamp))}, ${timeFormat.format(Date(timestamp))}"
        }
    }

    fun formatDateGroupHeader(timestamp: Long): String {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply { timeInMillis = timestamp }

        val isSameYear = now.get(Calendar.YEAR) == target.get(Calendar.YEAR)
        val isSameDay = isSameYear && now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)

        if (isSameDay) return "Hari Ini"

        val yesterday = (now.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
        val isYesterday = yesterday.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                yesterday.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)

        if (isYesterday) return "Kemarin"

        return if (isSameYear) dayMonthFormat.format(Date(timestamp)) else shortDateFormat.format(Date(timestamp))
    }

    fun formatMonthYear(month: Int, year: Int): String {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        return monthYearFormat.format(calendar.time)
    }

    fun getMonthRange(month: Int, year: Int): Pair<Long, Long> {
        val startCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endCal = (startCal.clone() as Calendar).apply {
            add(Calendar.MONTH, 1)
            add(Calendar.MILLISECOND, -1)
        }
        return Pair(startCal.timeInMillis, endCal.timeInMillis)
    }

    fun getCurrentMonth(): Int = Calendar.getInstance().get(Calendar.MONTH) + 1
    fun getCurrentYear(): Int = Calendar.getInstance().get(Calendar.YEAR)
}
