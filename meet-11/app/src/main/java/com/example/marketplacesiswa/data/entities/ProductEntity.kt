package com.example.marketplacesiswa.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "product")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val description: String,
    val price: Double,
    val stock: Int,
    val category: String,
    val unit: String = "pcs",
    val createdAt: Long = System.currentTimeMillis()
)
