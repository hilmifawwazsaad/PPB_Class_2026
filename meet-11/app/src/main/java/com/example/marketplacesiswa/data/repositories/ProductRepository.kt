package com.example.marketplacesiswa.data.repositories

import com.example.marketplacesiswa.data.dao.ProductDao
import com.example.marketplacesiswa.data.entities.Product
import kotlinx.coroutines.flow.Flow

class ProductRepository(private val productDao: ProductDao) {

    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val totalProducts: Flow<Int> = productDao.getTotalProducts()
    val totalInventoryValue: Flow<Double?> = productDao.getTotalInventoryValue()
    val allCategories: Flow<List<String>> = productDao.getAllCategories()

    fun searchProducts(query: String): Flow<List<Product>> =
        productDao.searchProducts(query)

    fun getProductsByCategory(category: String): Flow<List<Product>> =
        productDao.getProductsByCategory(category)

    suspend fun getProductById(id: Int): Product? =
        productDao.getProductById(id)

    suspend fun insertProduct(product: Product): Long =
        productDao.insertProduct(product)

    suspend fun updateProduct(product: Product) =
        productDao.updateProduct(product)

    suspend fun deleteProduct(product: Product) =
        productDao.deleteProduct(product)
}
