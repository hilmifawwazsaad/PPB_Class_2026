package com.example.budgetwise.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val iconName: String = "",
    val colorHex: String = "#6200EE",
    val type: String = "BOTH",      // "INCOME", "EXPENSE", "BOTH"
    val isDefault: Boolean = false
)