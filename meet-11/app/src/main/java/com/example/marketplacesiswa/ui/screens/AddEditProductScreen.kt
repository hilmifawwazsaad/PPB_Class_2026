package com.example.marketplacesiswa.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.marketplacesiswa.data.entities.Product
import com.example.marketplacesiswa.ui.theme.*
import com.example.marketplacesiswa.ui.viewmodels.ProductViewModel

val categoryList = listOf(
    "Makanan & Minuman",
    "Elektronik",
    "Pakaian & Fashion",
    "Kesehatan & Kecantikan",
    "Rumah Tangga",
    "Olahraga",
    "Otomotif",
    "Buku & Alat Tulis",
    "Mainan & Hobi",
    "Lainnya"
)

val unitList = listOf("pcs", "kg", "gram", "liter", "ml", "meter", "box", "lusin", "karton", "pack")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(
    viewModel: ProductViewModel,
    productId: Int? = null,
    onBack: () -> Unit
) {
    val isEdit = productId != null
    var productToEdit by remember { mutableStateOf<Product?>(null) }

    LaunchedEffect(productId) {
        if (productId != null) {
            productToEdit = viewModel.getProductById(productId)
        }
    }

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(categoryList[0]) }
    var selectedUnit by remember { mutableStateOf(unitList[0]) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var unitExpanded by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }
    var stockError by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(productToEdit) {
        productToEdit?.let { p ->
            name = p.name
            description = p.description
            price = p.price.toInt().toString()
            stock = p.stock.toString()
            selectedCategory = p.category
            selectedUnit = p.unit
        }
    }

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            onBack()
            viewModel.clearMessage()
        }
    }
    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage != null) {
            snackbarHostState.showSnackbar(uiState.errorMessage!!)
            viewModel.clearMessage()
        }
    }

    fun validateAndSave() {
        nameError = if (name.isBlank()) "Nama produk wajib diisi" else null
        priceError = when {
            price.isBlank() -> "Harga wajib diisi"
            price.toDoubleOrNull() == null -> "Format harga tidak valid"
            price.toDouble() < 0 -> "Harga tidak boleh negatif"
            else -> null
        }
        stockError = when {
            stock.isBlank() -> "Stok wajib diisi"
            stock.toIntOrNull() == null -> "Stok harus berupa angka"
            stock.toInt() < 0 -> "Stok tidak boleh negatif"
            else -> null
        }

        if (nameError == null && priceError == null && stockError == null) {
            if (isEdit && productToEdit != null) {
                viewModel.updateProduct(
                    productToEdit!!.copy(
                        name = name.trim(),
                        description = description.trim(),
                        price = price.toDouble(),
                        stock = stock.toInt(),
                        category = selectedCategory,
                        unit = selectedUnit
                    )
                )
            } else {
                viewModel.addProduct(
                    name = name.trim(),
                    description = description.trim(),
                    price = price.toDouble(),
                    stock = stock.toInt(),
                    category = selectedCategory,
                    unit = selectedUnit
                )
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEdit) "Ubah Produk" else "Tambah Produk",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = OnPrimary,
                    navigationIconContentColor = OnPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionHeader(title = "Informasi Dasar", icon = Icons.Filled.Info)

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = null
                },
                label = { Text("Nama Produk *") },
                leadingIcon = { Icon(Icons.Filled.Label, contentDescription = null, tint = Primary) },
                isError = nameError != null,
                supportingText = nameError?.let { { Text(it, color = Error) } },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Deskripsi Produk") },
                leadingIcon = { Icon(Icons.Filled.Description, contentDescription = null, tint = Primary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp),
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors(),
                maxLines = 4
            )

            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Kategori") },
                    leadingIcon = { Icon(Icons.Filled.Category, contentDescription = null, tint = Primary) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors()
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    categoryList.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = {
                                selectedCategory = cat
                                categoryExpanded = false
                            },
                            leadingIcon = {
                                if (selectedCategory == cat) {
                                    Icon(Icons.Filled.Check, contentDescription = null, tint = Primary)
                                }
                            }
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            
            SectionHeader(title = "Harga & Stok", icon = Icons.Filled.Inventory)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = price,
                    onValueChange = {
                        price = it
                        priceError = null
                    },
                    label = { Text("Harga (Rp) *") },
                    leadingIcon = { Icon(Icons.Filled.AttachMoney, contentDescription = null, tint = Primary) },
                    isError = priceError != null,
                    supportingText = priceError?.let { { Text(it, color = Error, fontSize = 11.sp) } },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = stock,
                    onValueChange = {
                        stock = it
                        stockError = null
                    },
                    label = { Text("Stok *") },
                    leadingIcon = { Icon(Icons.Filled.Numbers, contentDescription = null, tint = Primary) },
                    isError = stockError != null,
                    supportingText = stockError?.let { { Text(it, color = Error, fontSize = 11.sp) } },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors(),
                    singleLine = true
                )
            }
            
            ExposedDropdownMenuBox(
                expanded = unitExpanded,
                onExpandedChange = { unitExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedUnit,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Satuan") },
                    leadingIcon = { Icon(Icons.Filled.Scale, contentDescription = null, tint = Primary) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors()
                )
                ExposedDropdownMenu(
                    expanded = unitExpanded,
                    onDismissRequest = { unitExpanded = false }
                ) {
                    unitList.forEach { unit ->
                        DropdownMenuItem(
                            text = { Text(unit) },
                            onClick = {
                                selectedUnit = unit
                                unitExpanded = false
                            },
                            leadingIcon = {
                                if (selectedUnit == unit) {
                                    Icon(Icons.Filled.Check, contentDescription = null, tint = Primary)
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { validateAndSave() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Icon(
                    imageVector = if (isEdit) Icons.Filled.Save else Icons.Filled.AddCircle,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isEdit) "Simpan Perubahan" else "Tambah Produk",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary)
            ) {
                Text("Batal", fontWeight = FontWeight.Medium, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
        Text(
            text = title,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            color = Primary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Primary,
    focusedLabelColor = Primary,
    cursorColor = Primary
)
