package com.example.budgetwise.data.repositories

import com.example.budgetwise.data.dao.CategoryDao
import com.example.budgetwise.data.entities.CategoryEntity
import kotlinx.coroutines.flow.Flow

class CategoryRepository(private val categoryDao: CategoryDao) {

    // === GET ALL ===
    fun getAllCategories(): Flow<List<CategoryEntity>> =
        categoryDao.getAllCategories()

    // === FILTER BY TYPE ===
    fun getCategoriesByType(type: String): Flow<List<CategoryEntity>> =
        categoryDao.getCategoriesByType(type)

    // === GET BY ID ===
    suspend fun getCategoryById(id: Int): CategoryEntity? =
        categoryDao.getCategoryById(id)

    // === CRUD ===
    suspend fun insert(category: CategoryEntity) =
        categoryDao.insert(category)

    suspend fun update(category: CategoryEntity) =
        categoryDao.update(category)

    suspend fun delete(category: CategoryEntity) =
        categoryDao.delete(category)
}