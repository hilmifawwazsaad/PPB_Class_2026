package com.example.budgetwise.data.dao

import androidx.room.*
import com.example.budgetwise.data.entities.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    // === GET ALL ===
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    // === FILTER BY TYPE ===
    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC")
    fun getTransactionsByType(type: String): Flow<List<TransactionEntity>>

    // === FILTER BY CATEGORY ===
    @Query("SELECT * FROM transactions WHERE categoryId = :categoryId ORDER BY date DESC")
    fun getTransactionsByCategory(categoryId: Int): Flow<List<TransactionEntity>>

    // === FILTER BY MONTH & YEAR ===
    @Query("""
        SELECT * FROM transactions 
        WHERE strftime('%m', date / 1000, 'unixepoch') = :month
        AND strftime('%Y', date / 1000, 'unixepoch') = :year
        ORDER BY date DESC
    """)
    fun getTransactionsByMonth(month: String, year: String): Flow<List<TransactionEntity>>

    // === FILTER BY TYPE + MONTH ===
    @Query("""
        SELECT * FROM transactions 
        WHERE type = :type
        AND strftime('%m', date / 1000, 'unixepoch') = :month
        AND strftime('%Y', date / 1000, 'unixepoch') = :year
        ORDER BY date DESC
    """)
    fun getTransactionsByTypeAndMonth(
        type: String,
        month: String,
        year: String
    ): Flow<List<TransactionEntity>>

    // === SUMMARY ===
    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'INCOME'")
    fun getTotalIncome(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE'")
    fun getTotalExpense(): Flow<Double?>

    @Query("""
        SELECT SUM(amount) FROM transactions 
        WHERE type = 'INCOME'
        AND strftime('%m', date / 1000, 'unixepoch') = :month
        AND strftime('%Y', date / 1000, 'unixepoch') = :year
    """)
    fun getTotalIncomeByMonth(month: String, year: String): Flow<Double?>

    @Query("""
        SELECT SUM(amount) FROM transactions 
        WHERE type = 'EXPENSE'
        AND strftime('%m', date / 1000, 'unixepoch') = :month
        AND strftime('%Y', date / 1000, 'unixepoch') = :year
    """)
    fun getTotalExpenseByMonth(month: String, year: String): Flow<Double?>

    // === CRUD ===
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity)

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}