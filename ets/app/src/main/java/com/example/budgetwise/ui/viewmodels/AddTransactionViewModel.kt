package com.example.budgetwise.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.budgetwise.data.entities.CategoryEntity
import com.example.budgetwise.data.entities.TransactionEntity
import com.example.budgetwise.data.repositories.CategoryRepository
import com.example.budgetwise.data.repositories.TransactionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// State UI form tambah transaksi
data class AddTransactionUiState(
    val title: String = "",
    val amount: String = "",
    val type: String = "EXPENSE",
    val selectedCategoryId: Int? = null,
    val note: String = "",
    val date: Long = System.currentTimeMillis(),
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null,
    
    // UI state for adding new category
    val isAddingCategory: Boolean = false,
    val newCategoryName: String = "",
    val newCategoryIcon: String = "category",
    val newCategoryColor: String = "#FC5D39"
)

class AddTransactionViewModel(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    // Kategori yang ditampilkan sesuai tipe transaksi
    @OptIn(ExperimentalCoroutinesApi::class)
    val categories: StateFlow<List<CategoryEntity>> = _uiState
        .map { it.type }
        .distinctUntilChanged()
        .flatMapLatest { type ->
            categoryRepository.getCategoriesByType(type)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // === UPDATE STATE ===
    fun onTitleChange(value: String) {
        _uiState.update { it.copy(title = value, errorMessage = null) }
    }

    fun onAmountChange(value: String) {
        // hanya angka yang diperbolehkan
        if (value.isEmpty() || value.all { it.isDigit() || it == '.' }) {
            _uiState.update { it.copy(amount = value, errorMessage = null) }
        }
    }

    fun onTypeChange(value: String) {
        // reset kategori saat ganti tipe
        _uiState.update { it.copy(type = value, selectedCategoryId = null) }
    }

    fun onCategorySelected(categoryId: Int) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
    }

    fun onNoteChange(value: String) {
        _uiState.update { it.copy(note = value) }
    }

    fun onDateChange(value: Long) {
        _uiState.update { it.copy(date = value) }
    }

    // === CATEGORY CRUD ACTIONS ===
    fun toggleAddingCategory(show: Boolean) {
        _uiState.update { it.copy(isAddingCategory = show, newCategoryName = "") }
    }

    fun onNewCategoryNameChange(name: String) {
        _uiState.update { it.copy(newCategoryName = name) }
    }

    fun onNewCategoryIconChange(icon: String) {
        _uiState.update { it.copy(newCategoryIcon = icon) }
    }

    fun saveNewCategory() {
        val state = _uiState.value
        if (state.newCategoryName.isBlank()) return

        viewModelScope.launch {
            categoryRepository.insert(
                CategoryEntity(
                    name = state.newCategoryName.trim(),
                    iconName = state.newCategoryIcon,
                    colorHex = state.newCategoryColor,
                    type = state.type
                )
            )
            toggleAddingCategory(false)
        }
    }

    // === VALIDASI & SIMPAN TRANSAKSI ===
    fun saveTransaction() {
        val state = _uiState.value

        // Validasi
        when {
            state.title.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Judul tidak boleh kosong") }
                return
            }
            state.amount.isBlank() || state.amount.toDoubleOrNull() == null -> {
                _uiState.update { it.copy(errorMessage = "Nominal tidak valid") }
                return
            }
            state.amount.toDouble() <= 0 -> {
                _uiState.update { it.copy(errorMessage = "Nominal harus lebih dari 0") }
                return
            }
            state.selectedCategoryId == null -> {
                _uiState.update { it.copy(errorMessage = "Pilih kategori terlebih dahulu") }
                return
            }
        }

        // Simpan ke database
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                transactionRepository.insert(
                    TransactionEntity(
                        title = state.title.trim(),
                        amount = state.amount.toDouble(),
                        type = state.type,
                        categoryId = state.selectedCategoryId,
                        note = state.note.trim(),
                        date = state.date
                    )
                )
                _uiState.update { it.copy(isLoading = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Gagal menyimpan transaksi")
                }
            }
        }
    }

    // Reset form setelah saved
    fun resetForm() {
        _uiState.value = AddTransactionUiState()
    }

    companion object {
        fun factory(
            transactionRepository: TransactionRepository,
            categoryRepository: CategoryRepository
        ) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return AddTransactionViewModel(transactionRepository, categoryRepository) as T
            }
        }
    }
}