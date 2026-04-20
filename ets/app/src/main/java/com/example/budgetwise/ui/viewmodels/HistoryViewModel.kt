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

// State filter
data class FilterState(
    val type: String = "ALL",           // "ALL", "INCOME", "EXPENSE"
    val categoryId: Int? = null,        // null = semua kategori
    val month: String = "",             // "" = semua bulan
    val year: String = ""
)

class HistoryViewModel(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    private val _deletedTransaction = MutableStateFlow<TransactionEntity?>(null)
    val deletedTransaction: StateFlow<TransactionEntity?> = _deletedTransaction.asStateFlow()

    // Semua kategori untuk dropdown filter
    val categories: StateFlow<List<CategoryEntity>> = categoryRepository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Transaksi yang ditampilkan — reaktif terhadap filter
    @OptIn(ExperimentalCoroutinesApi::class)
    val transactions: StateFlow<List<TransactionEntity>> = _filterState
        .flatMapLatest { filter ->
            when {
                // Filter bulan + tipe
                filter.month.isNotEmpty() && filter.type != "ALL" -> {
                    transactionRepository.getTransactionsByTypeAndMonth(
                        type = filter.type,
                        month = filter.month,
                        year = filter.year
                    )
                }
                // Filter bulan saja
                filter.month.isNotEmpty() -> {
                    transactionRepository.getTransactionsByMonth(
                        month = filter.month,
                        year = filter.year
                    )
                }
                // Filter tipe saja
                filter.type != "ALL" -> {
                    transactionRepository.getTransactionsByType(filter.type)
                }
                // Filter kategori saja
                filter.categoryId != null -> {
                    transactionRepository.getTransactionsByCategory(filter.categoryId)
                }
                // Semua transaksi
                else -> transactionRepository.getAllTransactions()
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Summary dari hasil filter
    val totalFiltered: StateFlow<Double> = transactions
        .map { list -> list.sumOf { it.amount } }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val totalIncomeFiltered: StateFlow<Double> = transactions
        .map { list ->
            list.filter { it.type == "INCOME" }.sumOf { it.amount }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val totalExpenseFiltered: StateFlow<Double> = transactions
        .map { list ->
            list.filter { it.type == "EXPENSE" }.sumOf { it.amount }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    // === UPDATE FILTER ===
    fun setTypeFilter(type: String) {
        _filterState.update { it.copy(type = type, categoryId = null) }
    }

    fun setCategoryFilter(categoryId: Int?) {
        _filterState.update { it.copy(categoryId = categoryId) }
    }

    fun setMonthFilter(month: String, year: String) {
        _filterState.update { it.copy(month = month, year = year) }
    }

    fun resetFilter() {
        _filterState.value = FilterState()
    }

    // === DELETE ===
    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            transactionRepository.delete(transaction)
            _deletedTransaction.value = transaction  // untuk snackbar "undo"
        }
    }

    // Undo delete — insert kembali transaksi yang dihapus
    fun undoDelete() {
        viewModelScope.launch {
            _deletedTransaction.value?.let { transaction ->
                transactionRepository.insert(transaction)
                _deletedTransaction.value = null
            }
        }
    }

    fun clearDeletedTransaction() {
        _deletedTransaction.value = null
    }

    companion object {
        fun factory(
            transactionRepository: TransactionRepository,
            categoryRepository: CategoryRepository
        ) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return HistoryViewModel(transactionRepository, categoryRepository) as T
            }
        }
    }
}