package com.example.budgetwise.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.budgetwise.data.entities.CategoryEntity
import com.example.budgetwise.data.entities.TransactionEntity
import com.example.budgetwise.ui.viewmodels.FilterState
import com.example.budgetwise.ui.viewmodels.HistoryViewModel
import com.example.budgetwise.utils.CurrencyFormatter
import com.example.budgetwise.utils.DateFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onNavigateBack: () -> Unit
) {
    val transactions by viewModel.transactions.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val totalIncome by viewModel.totalIncomeFiltered.collectAsState()
    val totalExpense by viewModel.totalExpenseFiltered.collectAsState()
    val deletedTransaction by viewModel.deletedTransaction.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState()
    var showFilterSheet by remember { mutableStateOf(false) }

    // Snackbar undo delete
    LaunchedEffect(deletedTransaction) {
        deletedTransaction?.let {
            val result = snackbarHostState.showSnackbar(
                message = "Transaksi dihapus",
                actionLabel = "Undo",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoDelete()
            } else {
                viewModel.clearDeletedTransaction()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Riwayat Transaksi", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterSheet = true }) {
                        Box {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter")
                            // Indicator if filters are active
                            if (filterState.month.isNotEmpty() || filterState.type != "ALL" || filterState.categoryId != null) {
                                Surface(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .align(Alignment.TopEnd),
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                ) {}
                            }
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Summary Card at top
            SummaryRow(
                totalIncome = totalIncome,
                totalExpense = totalExpense,
                modifier = Modifier.padding(16.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Daftar Transaksi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                if (transactions.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Tidak ada transaksi",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(
                        items = transactions,
                        key = { it.id }
                    ) { transaction ->
                        HistoryTransactionItem(
                            transaction = transaction,
                            onDelete = { viewModel.deleteTransaction(transaction) }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }

    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            FilterSheetContent(
                filterState = filterState,
                categories = categories,
                onMonthChanged = { month, year -> viewModel.setMonthFilter(month, year) },
                onResetMonth = { viewModel.setMonthFilter("", "") },
                onTypeSelected = viewModel::setTypeFilter,
                onCategorySelected = viewModel::setCategoryFilter,
                onDismiss = { showFilterSheet = false }
            )
        }
    }
}

@Composable
fun FilterSheetContent(
    filterState: FilterState,
    categories: List<CategoryEntity>,
    onMonthChanged: (String, String) -> Unit,
    onResetMonth: () -> Unit,
    onTypeSelected: (String) -> Unit,
    onCategorySelected: (Int?) -> Unit,
    onDismiss: () -> Unit
) {
    val monthNames = listOf(
        "Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
        "Jul", "Agu", "Sep", "Okt", "Nov", "Des"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp, start = 24.dp, end = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Filter",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = {
                onResetMonth()
                onTypeSelected("ALL")
                onCategorySelected(null)
            }) {
                Text("Reset Semua")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Bulan Row
        FilterCategoryRow(
            label = "Bulan",
            value = if (filterState.month.isEmpty()) "Semua" 
                    else "${monthNames[filterState.month.toInt() - 1]} ${filterState.year}"
        ) {
            MonthSelectionList(filterState, onMonthChanged, onResetMonth)
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)

        // Tipe Row
        FilterCategoryRow(
            label = "Tipe",
            value = when(filterState.type) {
                "INCOME" -> "Pemasukan"
                "EXPENSE" -> "Pengeluaran"
                else -> "Semua"
            }
        ) {
            TypeSelectionList(filterState.type, onTypeSelected)
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)

        // Kategori Row
        FilterCategoryRow(
            label = "Kategori",
            value = categories.find { it.id == filterState.categoryId }?.name ?: "Semua"
        ) {
            CategorySelectionList(categories, filterState.categoryId, onCategorySelected)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Terapkan", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun FilterCategoryRow(
    label: String,
    value: String,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, fontWeight = FontWeight.Medium, fontSize = 16.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = value, 
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        if (expanded) {
            Box(modifier = Modifier.padding(bottom = 12.dp)) {
                content()
            }
        }
    }
}

@Composable
fun MonthSelectionList(
    filterState: FilterState,
    onMonthChanged: (String, String) -> Unit,
    onResetMonth: () -> Unit
) {
    val monthNames = listOf(
        "Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
        "Jul", "Agu", "Sep", "Okt", "Nov", "Des"
    )

    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            FilterChip(
                selected = filterState.month.isEmpty(),
                onClick = onResetMonth,
                label = { Text("Semua") }
            )
        }
        items(6) { index ->
            val cal = Calendar.getInstance().apply { add(Calendar.MONTH, -index) }
            val month = cal.get(Calendar.MONTH) + 1
            val year = cal.get(Calendar.YEAR)
            val monthStr = month.toString().padStart(2, '0')
            val yearStr = year.toString()
            val label = "${monthNames[month - 1]} $year"

            FilterChip(
                selected = filterState.month == monthStr && filterState.year == yearStr,
                onClick = { onMonthChanged(monthStr, yearStr) },
                label = { Text(label) }
            )
        }
    }
}

@Composable
fun TypeSelectionList(
    selectedType: String,
    onTypeSelected: (String) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("ALL" to "Semua", "INCOME" to "Pemasukan", "EXPENSE" to "Pengeluaran").forEach { (type, label) ->
            FilterChip(
                selected = selectedType == type,
                onClick = { onTypeSelected(type) },
                label = { Text(label) }
            )
        }
    }
}

@Composable
fun CategorySelectionList(
    categories: List<CategoryEntity>,
    selectedCategoryId: Int?,
    onCategorySelected: (Int?) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            FilterChip(
                selected = selectedCategoryId == null,
                onClick = { onCategorySelected(null) },
                label = { Text("Semua") }
            )
        }
        items(categories) { category ->
            FilterChip(
                selected = selectedCategoryId == category.id,
                onClick = { onCategorySelected(category.id) },
                label = { Text(category.name) }
            )
        }
    }
}

@Composable
fun SummaryRow(
    totalIncome: Double,
    totalExpense: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Pemasukan",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
                Text(
                    text = CurrencyFormatter.format(totalIncome),
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
            VerticalDivider(
                modifier = Modifier.height(40.dp),
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f)
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Pengeluaran",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
                Text(
                    text = CurrencyFormatter.format(totalExpense),
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryTransactionItem(
    transaction: TransactionEntity,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indikator tipe
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (transaction.type == "INCOME") Color(0xFF81C784).copy(alpha = 0.2f)
                        else Color(0xFFE57373).copy(alpha = 0.2f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (transaction.type == "INCOME") Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                    contentDescription = null,
                    tint = if (transaction.type == "INCOME") Color(0xFF43A047) else Color(0xFFD32F2F),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Info transaksi
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = DateFormatter.format(transaction.date),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }

            // Nominal + tombol hapus
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (transaction.type == "INCOME") "+" else "-"} ${
                        CurrencyFormatter.format(transaction.amount)
                    }",
                    color = if (transaction.type == "INCOME") Color(0xFF388E3C) else Color(0xFFC62828),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "Hapus",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus Transaksi") },
            text = { Text("Yakin ingin menghapus transaksi \"${transaction.title}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}
