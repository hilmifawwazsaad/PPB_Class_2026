package com.example.marketplacesiswa.utils

import java.text.NumberFormat
import java.util.*

object FormatterUtils {
    fun formatCurrency(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return formatter.format(amount)
    }
}
