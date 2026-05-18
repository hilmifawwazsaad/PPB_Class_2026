package com.example.loginapp.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.loginapp.data.local.User
import com.example.loginapp.data.repositories.UserRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val userRepository: UserRepository) : ViewModel() {
    var username by mutableStateOf("")
    var password by mutableStateOf("")

    private val _loginResult = MutableSharedFlow<Boolean>()
    val loginResult = _loginResult.asSharedFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage = _errorMessage.asSharedFlow()

    private val _seedStatus = MutableSharedFlow<String>()
    val seedStatus = _seedStatus.asSharedFlow()

    fun login() {
        if (username.isBlank() || password.isBlank()) {
            viewModelScope.launch { _errorMessage.emit("Username and password cannot be empty") }
            return
        }

        viewModelScope.launch {
            val user = userRepository.getUserByUsername(username)
            if (user != null && user.password == password) {
                _loginResult.emit(true)
            } else {
                _errorMessage.emit("Invalid username or password")
            }
        }
    }
    fun seedUser() {
        viewModelScope.launch {
            val dummyUser = User(username = "admin", password = "123")
            userRepository.insertUser(dummyUser)
            _seedStatus.emit("User 'admin' with password '123' has been created!")
        }
    }
}

class LoginViewModelFactory(private val repository: UserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
