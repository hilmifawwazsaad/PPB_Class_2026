package com.example.crudsiswa.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val studentId: String,
    val email: String,
    val major: String,
    val gpa: Double
)
 