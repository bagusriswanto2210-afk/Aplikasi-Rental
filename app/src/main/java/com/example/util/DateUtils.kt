package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateUtils {
    private val localeId = Locale("id", "ID")

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", localeId)
        return sdf.format(Date(timestamp))
    }

    fun formatDateShort(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", localeId)
        return sdf.format(Date(timestamp))
    }

    fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", localeId)
        return sdf.format(Date(timestamp))
    }

    fun calculateDays(startMillis: Long, endMillis: Long): Int {
        if (endMillis <= startMillis) return 1
        val diff = endMillis - startMillis
        val days = TimeUnit.MILLISECONDS.toDays(diff).toInt()
        return if (days <= 0) 1 else days
    }

    fun getStartOfDay(timestamp: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getEndOfDay(timestamp: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    fun isToday(timestamp: Long): Boolean {
        val today = getStartOfDay(System.currentTimeMillis())
        val check = getStartOfDay(timestamp)
        return today == check
    }

    fun isPast(timestamp: Long): Boolean {
        val today = getStartOfDay(System.currentTimeMillis())
        val check = getStartOfDay(timestamp)
        return check < today
    }
}
