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
import java.util.Calendar

class DashboardViewModel(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    // Bulan & tahun aktif (default: bulan sekarang)
    private val _selectedMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH) + 1)
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    private val _selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    // Format bulan & tahun ke string untuk query ("01", "2025")
    private val monthYear: StateFlow<Pair<String, String>> = combine(
        _selectedMonth, _selectedYear
    ) { month, year ->
        Pair(month.toString().padStart(2, '0'), year.toString())
    }.stateIn(viewModelScope, SharingStarted.Lazily, Pair("01", "2025"))

    // Semua transaksi bulan ini untuk chart & total
    @OptIn(ExperimentalCoroutinesApi::class)
    private val monthlyTransactions: StateFlow<List<TransactionEntity>> = monthYear.flatMapLatest { (month, year) ->
        transactionRepository.getTransactionsByMonth(month, year)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Total income & expense bulan ini
    val totalIncome: StateFlow<Double> = monthlyTransactions.map { transactions ->
        transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val totalExpense: StateFlow<Double> = monthlyTransactions.map { transactions ->
        transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    // Saldo = income - expense
    val balance: StateFlow<Double> = combine(totalIncome, totalExpense) { income, expense ->
        income - expense
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    // 5 transaksi terakhir bulan ini untuk ditampilkan di dashboard
    val recentTransactions: StateFlow<List<TransactionEntity>> = monthlyTransactions.map { 
        it.take(5) 
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Data trend harian untuk chart (mengelompokkan saldo per hari)
    val dailyTrend: StateFlow<List<Pair<Int, Float>>> = monthlyTransactions.map { transactions ->
        val calendar = Calendar.getInstance()
        transactions.groupBy {
            calendar.timeInMillis = it.date
            calendar.get(Calendar.DAY_OF_MONTH)
        }.mapValues { entry ->
            entry.value.sumOf { if (it.type == "INCOME") it.amount else -it.amount }.toFloat()
        }.toList().sortedBy { it.first }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Semua kategori
    val categories: StateFlow<List<CategoryEntity>> = categoryRepository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Ganti bulan yang ditampilkan
    fun setMonth(month: Int, year: Int) {
        _selectedMonth.value = month
        _selectedYear.value = year
    }

    // Factory untuk instansiasi ViewModel dengan parameter
    companion object {
        fun factory(
            transactionRepository: TransactionRepository,
            categoryRepository: CategoryRepository
        ) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return DashboardViewModel(transactionRepository, categoryRepository) as T
            }
        }
    }
}