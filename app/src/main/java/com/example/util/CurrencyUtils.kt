package com.example.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyUtils {
    private val rupiahFormat: NumberFormat by lazy {
        NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
            maximumFractionDigits = 0
            minimumFractionDigits = 0
        }
    }

    fun formatRupiah(amount: Long): String {
        return try {
            rupiahFormat.format(amount).replace("Rp", "Rp ")
        } catch (e: Exception) {
            "Rp $amount"
        }
    }

    fun parseRupiahInput(input: String): Long {
        val cleanString = input.filter { it.isDigit() }
        return cleanString.toLongOrNull() ?: 0L
    }
}
