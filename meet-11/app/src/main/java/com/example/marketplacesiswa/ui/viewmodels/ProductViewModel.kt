package com.example.marketplacesiswa.ui.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.example.marketplacesiswa.data.MarketplaceDatabase
import com.example.marketplacesiswa.data.entities.Product
import com.example.marketplacesiswa.data.repositories.ProductRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ProductUiState(
    val productList: List<Product> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val totalProducts: Int = 0,
    val totalInventoryValue: Double = 0.0,
    val categoryList: List<String> = emptyList()
)

class ProductViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProductRepository

    private val _uiState = MutableStateFlow(ProductUiState())
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Semua")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    init {
        val db = MarketplaceDatabase.getDatabase(application)
        repository = ProductRepository(db.productDao())
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                _searchQuery,
                _selectedCategory
            ) { query, category -> Pair(query, category) }
                .flatMapLatest { (query, category) ->
                    when {
                        query.isNotBlank() -> repository.searchProducts(query)
                        category != "Semua" -> repository.getProductsByCategory(category)
                        else -> repository.allProducts
                    }
                }
                .collect { productList ->
                    _uiState.update { it.copy(productList = productList) }
                }
        }

        viewModelScope.launch {
            repository.totalProducts.collect { total ->
                _uiState.update { it.copy(totalProducts = total) }
            }
        }

        viewModelScope.launch {
            repository.totalInventoryValue.collect { total ->
                _uiState.update { it.copy(totalInventoryValue = total ?: 0.0) }
            }
        }

        viewModelScope.launch {
            repository.allCategories.collect { categories ->
                _uiState.update { it.copy(categoryList = listOf("Semua") + categories) }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(category: String) {
        _selectedCategory.value = category
    }

    fun addProduct(
        name: String,
        description: String,
        price: Double,
        stock: Int,
        category: String,
        unit: String
    ) {
        viewModelScope.launch {
            try {
                val product = Product(
                    name = name,
                    description = description,
                    price = price,
                    stock = stock,
                    category = category,
                    unit = unit
                )
                repository.insertProduct(product)
                _uiState.update { it.copy(successMessage = "Produk '$name' berhasil ditambahkan!") }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Gagal menambahkan produk: ${e.message}") }
            }
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            try {
                repository.updateProduct(product)
                _uiState.update { it.copy(successMessage = "Produk '${product.name}' berhasil diperbarui!") }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Gagal memperbarui produk: ${e.message}") }
            }
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            try {
                repository.deleteProduct(product)
                _uiState.update { it.copy(successMessage = "Produk '${product.name}' berhasil dihapus!") }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Gagal menghapus produk: ${e.message}") }
            }
        }
    }

    suspend fun getProductById(id: Int): Product? = repository.getProductById(id)

    fun clearMessage() {
        _uiState.update { it.copy(successMessage = null, errorMessage = null) }
    }
}

class ProductViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProductViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
