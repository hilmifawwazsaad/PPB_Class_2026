package com.example.budgetwise.data.repositories

import com.example.budgetwise.data.dao.TransactionDao
import com.example.budgetwise.data.entities.TransactionEntity
import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val transactionDao: TransactionDao) {

    // === GET ALL ===
    fun getAllTransactions(): Flow<List<TransactionEntity>> =
        transactionDao.getAllTransactions()

    // === FILTER ===
    fun getTransactionsByType(type: String): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsByType(type)

    fun getTransactionsByCategory(categoryId: Int): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsByCategory(categoryId)

    fun getTransactionsByMonth(month: String, year: String): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsByMonth(month, year)

    fun getTransactionsByTypeAndMonth(
        type: String,
        month: String,
        year: String
    ): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsByTypeAndMonth(type, month, year)

    // === SUMMARY ===
    fun getTotalIncome(): Flow<Double?> =
        transactionDao.getTotalIncome()

    fun getTotalExpense(): Flow<Double?> =
        transactionDao.getTotalExpense()

    fun getTotalIncomeByMonth(month: String, year: String): Flow<Double?> =
        transactionDao.getTotalIncomeByMonth(month, year)

    fun getTotalExpenseByMonth(month: String, year: String): Flow<Double?> =
        transactionDao.getTotalExpenseByMonth(month, year)

    // === CRUD ===
    suspend fun insert(transaction: TransactionEntity) =
        transactionDao.insert(transaction)

    suspend fun update(transaction: TransactionEntity) =
        transactionDao.update(transaction)

    suspend fun delete(transaction: TransactionEntity) =
        transactionDao.delete(transaction)

    suspend fun deleteAll() =
        transactionDao.deleteAll()
}