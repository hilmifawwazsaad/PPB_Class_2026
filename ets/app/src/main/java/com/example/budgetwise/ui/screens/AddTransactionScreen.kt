package com.example.budgetwise.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.budgetwise.data.entities.CategoryEntity
import com.example.budgetwise.ui.viewmodels.AddTransactionViewModel
import com.example.budgetwise.utils.DateFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: AddTransactionViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val categories by viewModel.categories.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }

    // Navigasi balik otomatis setelah tersimpan
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateToDashboard(onNavigateBack)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tambah Transaksi", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // === TIPE TRANSAKSI ===
            Text("Tipe Transaksi", fontWeight = FontWeight.Medium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf(
                    Triple("INCOME", "Pemasukan", Icons.Default.AddCircle),
                    Triple("EXPENSE", "Pengeluaran", Icons.Default.RemoveCircle)
                ).forEach { (type, label, icon) ->
                    val isSelected = uiState.type == type
                    val color = if (type == "INCOME") Color(0xFF81C784) else Color(0xFFE57373)
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isSelected) color.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .clickable { viewModel.onTypeChange(type) }
                            .padding(vertical = 12.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // === JUDUL ===
            Text("Judul", fontWeight = FontWeight.Medium)
            OutlinedTextField(
                value = uiState.title,
                onValueChange = viewModel::onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Contoh: Gaji Januari") },
                singleLine = true,
                isError = uiState.errorMessage?.contains("Judul") == true,
                shape = RoundedCornerShape(12.dp)
            )

            // === NOMINAL ===
            Text("Nominal", fontWeight = FontWeight.Medium)
            OutlinedTextField(
                value = uiState.amount,
                onValueChange = viewModel::onAmountChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("0") },
                prefix = { Text("Rp ") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = uiState.errorMessage?.contains("Nominal") == true,
                shape = RoundedCornerShape(12.dp)
            )

            // === KATEGORI ===
            Text("Kategori", fontWeight = FontWeight.Medium)
            if (categories.isEmpty() && !uiState.isLoading) {
                AddCategoryItem(onClick = { viewModel.toggleAddingCategory(true) })
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(categories) { category ->
                        CategoryItem(
                            category = category,
                            isSelected = uiState.selectedCategoryId == category.id,
                            onClick = { viewModel.onCategorySelected(category.id) }
                        )
                    }
                    item {
                        AddCategoryItem(onClick = { viewModel.toggleAddingCategory(true) })
                    }
                }
            }

            // === TANGGAL ===
            Text("Tanggal", fontWeight = FontWeight.Medium)
            OutlinedTextField(
                value = DateFormatter.format(uiState.date),
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Pilih Tanggal")
                    }
                }
            )

            // === CATATAN ===
            Text("Catatan (opsional)", fontWeight = FontWeight.Medium)
            OutlinedTextField(
                value = uiState.note,
                onValueChange = viewModel::onNoteChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Tambah catatan...") },
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(12.dp)
            )

            // === ERROR MESSAGE ===
            uiState.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            // === TOMBOL SIMPAN ===
            Button(
                onClick = viewModel::saveTransaction,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !uiState.isLoading,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Simpan Transaksi",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // === DATE PICKER ===
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.date
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        viewModel.onDateChange(it)
                    }
                    showDatePicker = false
                }) {
                    Text("Pilih")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Batal")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // === ADD CATEGORY DIALOG ===
    if (uiState.isAddingCategory) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleAddingCategory(false) },
            title = { Text("Tambah Kategori Baru") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = uiState.newCategoryName,
                        onValueChange = viewModel::onNewCategoryNameChange,
                        label = { Text("Nama Kategori") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Text("Pilih Ikon", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val icons = listOf(
                            "restaurant", "directions_car", "shopping_bag", "home",
                            "movie", "payments", "work", "health_and_safety", "school"
                        )
                        items(icons) { iconName ->
                            val isIconSelected = uiState.newCategoryIcon == iconName
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isIconSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                        else Color.Transparent
                                    )
                                    .border(
                                        1.dp,
                                        if (isIconSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                                        CircleShape
                                    )
                                    .clickable { viewModel.onNewCategoryIconChange(iconName) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getIconForName(iconName),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = if (isIconSelected) MaterialTheme.colorScheme.primary else Color.Gray
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.saveNewCategory() },
                    enabled = uiState.newCategoryName.isNotBlank()
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.toggleAddingCategory(false) }) {
                    Text("Batal")
                }
            }
        )
    }
}

private fun onNavigateToDashboard(onNavigateBack: () -> Unit) {
    onNavigateBack()
}

@Composable
fun CategoryItem(
    category: CategoryEntity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val categoryColor = try {
        Color(android.graphics.Color.parseColor(category.colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    val icon = remember(category.iconName) {
        getIconForName(category.iconName)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) categoryColor.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = category.name,
            tint = if (isSelected) categoryColor else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = category.name,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) categoryColor else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@Composable
fun AddCategoryItem(onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(
                1.dp, 
                MaterialTheme.colorScheme.outlineVariant, 
                RoundedCornerShape(16.dp)
            )
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Tambah",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "More",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

fun getIconForName(iconName: String): ImageVector {
    return when (iconName) {
        "restaurant" -> Icons.Default.Restaurant
        "directions_car" -> Icons.Default.DirectionsCar
        "shopping_bag" -> Icons.Default.ShoppingBag
        "home" -> Icons.Default.Home
        "movie" -> Icons.Default.Movie
        "payments" -> Icons.Default.AccountBalanceWallet
        "work" -> Icons.Default.Work
        "health_and_safety" -> Icons.Default.HealthAndSafety
        "school" -> Icons.Default.School
        else -> Icons.Default.Category
    }
}
