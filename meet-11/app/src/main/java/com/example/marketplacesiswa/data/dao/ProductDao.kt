package com.example.marketplacesiswa.data.dao

import androidx.room.*
import com.example.marketplacesiswa.data.entities.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Query("SELECT * FROM product ORDER BY createdAt DESC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM product WHERE id = :id")
    suspend fun getProductById(id: Int): Product?

    @Query("SELECT * FROM product WHERE name LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%'")
    fun searchProducts(query: String): Flow<List<Product>>

    @Query("SELECT * FROM product WHERE category = :category ORDER BY createdAt DESC")
    fun getProductsByCategory(category: String): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<Product>)

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("SELECT COUNT(*) FROM product")
    fun getTotalProducts(): Flow<Int>

    @Query("SELECT COUNT(*) FROM product")
    suspend fun getCount(): Int

    @Query("SELECT SUM(price * stock) FROM product")
    fun getTotalInventoryValue(): Flow<Double?>

    @Query("SELECT DISTINCT category FROM product")
    fun getAllCategories(): Flow<List<String>>
}
