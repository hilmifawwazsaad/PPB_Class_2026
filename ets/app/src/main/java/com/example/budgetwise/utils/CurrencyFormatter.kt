package com.example.budgetwise.utils

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    fun format(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return format.format(amount)
    }
}