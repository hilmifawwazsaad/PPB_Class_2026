package com.example.budgetwise.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.budgetwise.R
import com.example.budgetwise.data.entities.TransactionEntity
import com.example.budgetwise.ui.viewmodels.DashboardViewModel
import com.example.budgetwise.utils.CurrencyFormatter
import com.example.budgetwise.utils.DateFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val balance by viewModel.balance.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()
    val recentTransactions by viewModel.recentTransactions.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()
    val dailyTrend by viewModel.dailyTrend.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.budget_wise_logo),
                            contentDescription = null,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("BudgetWise", fontWeight = FontWeight.SemiBold)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Default.History, contentDescription = "History")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddTransaction,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Transaksi")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Pilih bulan
            item {
                MonthSelector(
                    selectedMonth = selectedMonth,
                    selectedYear = selectedYear,
                    onMonthChanged = { month, year -> viewModel.setMonth(month, year) }
                )
            }

            // Chart Trend
            item {
                DailyTrendChart(dailyTrend = dailyTrend)
            }

            // Kartu saldo
            item {
                BalanceCard(
                    balance = balance,
                    totalIncome = totalIncome,
                    totalExpense = totalExpense
                )
            }

            // Header transaksi terakhir
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Transaksi Terakhir",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    TextButton(onClick = onNavigateToHistory) {
                        Text("Lihat Semua")
                    }
                }
            }

            // List transaksi terakhir
            if (recentTransactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Belum ada transaksi bulan ini",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(recentTransactions) { transaction ->
                    TransactionItem(transaction = transaction)
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun DailyTrendChart(dailyTrend: List<Pair<Int, Float>>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Trend Saldo Harian",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            if (dailyTrend.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Data tidak tersedia", fontSize = 12.sp, color = Color.Gray)
                }
            } else {
                val primaryColor = MaterialTheme.colorScheme.primary
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    
                    val maxVal = dailyTrend.maxByOrNull { it.second }?.second?.coerceAtLeast(1f) ?: 1f
                    val minVal = dailyTrend.minByOrNull { it.second }?.second?.coerceAtMost(0f) ?: 0f
                    val range = maxVal - minVal
                    
                    val path = Path()
                    dailyTrend.forEachIndexed { index, point ->
                        val x = (index.toFloat() / (dailyTrend.size - 1).coerceAtLeast(1)) * width
                        val y = height - ((point.second - minVal) / range) * height
                        
                        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    
                    drawPath(
                        path = path,
                        color = primaryColor,
                        style = Stroke(width = 3.dp.toPx())
                    )
                    
                    // Fill area under the curve
                    val fillPath = Path().apply {
                        addPath(path)
                        lineTo(width, height)
                        lineTo(0f, height)
                        close()
                    }
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(primaryColor.copy(alpha = 0.3f), Color.Transparent)
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun MonthSelector(
    selectedMonth: Int,
    selectedYear: Int,
    onMonthChanged: (Int, Int) -> Unit
) {
    val monthNames = listOf(
        "Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
        "Jul", "Agu", "Sep", "Okt", "Nov", "Des"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = {
            val cal = Calendar.getInstance().apply {
                set(Calendar.MONTH, selectedMonth - 1)
                set(Calendar.YEAR, selectedYear)
                add(Calendar.MONTH, -1)
            }
            onMonthChanged(
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.YEAR)
            )
        }) {
            Text("<", fontWeight = FontWeight.Bold)
        }

        Text(
            text = "${monthNames[selectedMonth - 1]} $selectedYear",
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )

        IconButton(onClick = {
            val cal = Calendar.getInstance().apply {
                set(Calendar.MONTH, selectedMonth - 1)
                set(Calendar.YEAR, selectedYear)
                add(Calendar.MONTH, 1)
            }
            onMonthChanged(
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.YEAR)
            )
        }) {
            Text(">", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun BalanceCard(
    balance: Double,
    totalIncome: Double,
    totalExpense: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Saldo Bulan Ini",
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = CurrencyFormatter.format(balance),
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Income
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Pemasukan",
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                    Text(
                        text = CurrencyFormatter.format(totalIncome),
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
                // Divider vertikal
                VerticalDivider(
                    modifier = Modifier.height(40.dp),
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f)
                )
                // Expense
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Pengeluaran",
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                    Text(
                        text = CurrencyFormatter.format(totalExpense),
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: TransactionEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indikator income/expense
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (transaction.type == "INCOME") Color(0xFF81C784)
                        else Color(0xFFE57373)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (transaction.type == "INCOME") "+" else "-",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title & tanggal
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.title,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
                Text(
                    text = DateFormatter.format(transaction.date),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }

            // Nominal
            Text(
                text = "${if (transaction.type == "INCOME") "+" else "-"} ${CurrencyFormatter.format(transaction.amount)}",
                color = if (transaction.type == "INCOME") Color(0xFF388E3C) else Color(0xFFC62828),
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
    }
}
