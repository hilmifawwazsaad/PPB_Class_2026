package com.example.loginapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.loginapp.data.local.database.AppDatabase
import com.example.loginapp.data.repositories.UserRepository
import com.example.loginapp.ui.screens.LoginScreen
import com.example.loginapp.ui.theme.LoginAppTheme
import com.example.loginapp.viewmodels.LoginViewModel
import com.example.loginapp.viewmodels.LoginViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(this)
        val repository = UserRepository(database.userDao())
        val factory = LoginViewModelFactory(repository)

        enableEdgeToEdge()
        setContent {
            LoginAppTheme {
                val viewModel: LoginViewModel = viewModel(factory = factory)
                LoginScreen(viewModel = viewModel)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginAppTheme {
    }
}
