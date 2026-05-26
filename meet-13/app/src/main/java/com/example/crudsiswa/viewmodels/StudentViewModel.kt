package com.example.crudsiswa.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.crudsiswa.data.entities.Student
import com.example.crudsiswa.data.repositories.StudentRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class StudentUiState(
    val students: List<Student> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class StudentViewModel(private val repository: StudentRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentUiState())
    val uiState: StateFlow<StudentUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    init {
        observeStudents()
    }

    private fun observeStudents() {
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .flatMapLatest { query ->
                    if (query.isBlank()) repository.allStudents
                    else repository.searchStudents(query)
                }
                .collect { students ->
                    _uiState.update { it.copy(students = students, isLoading = false) }
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun addStudent(student: Student) {
        viewModelScope.launch {
            try {
                repository.insertStudent(student)
                _uiState.update { it.copy(successMessage = "Student added successfully") }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to add student: ${e.message}") }
            }
        }
    }

    fun updateStudent(student: Student) {
        viewModelScope.launch {
            try {
                repository.updateStudent(student)
                _uiState.update { it.copy(successMessage = "Student updated successfully") }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to update student: ${e.message}") }
            }
        }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            try {
                repository.deleteStudent(student)
                _uiState.update { it.copy(successMessage = "Student deleted successfully") }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to delete student: ${e.message}") }
            }
        }
    }

    suspend fun getStudentById(id: Int): Student? =
        repository.getStudentById(id)

    fun clearMessage() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}

class StudentViewModelFactory(private val repository: StudentRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StudentViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}