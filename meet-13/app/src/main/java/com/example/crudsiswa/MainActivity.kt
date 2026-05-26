package com.example.crudsiswa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.crudsiswa.data.AppDatabase
import com.example.crudsiswa.data.repositories.StudentRepository
import com.example.crudsiswa.ui.screens.StudentNavGraph
import com.example.crudsiswa.ui.theme.CRUDSiswaTheme
import com.example.crudsiswa.viewmodels.StudentViewModel
import com.example.crudsiswa.viewmodels.StudentViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: StudentViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = StudentRepository(database.studentDao())
        StudentViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CRUDSiswaTheme {
                StudentNavGraph(viewModel = viewModel)
            }
        }
    }
}
