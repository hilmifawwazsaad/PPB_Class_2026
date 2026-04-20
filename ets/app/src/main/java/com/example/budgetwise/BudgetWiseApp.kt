package com.example.budgetwise

import android.app.Application
import com.example.budgetwise.data.AppDatabase

class BudgetWiseApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
}