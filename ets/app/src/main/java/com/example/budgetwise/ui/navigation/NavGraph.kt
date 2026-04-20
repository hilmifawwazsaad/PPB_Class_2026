package com.example.budgetwise.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.budgetwise.BudgetWiseApp
import com.example.budgetwise.data.repositories.CategoryRepository
import com.example.budgetwise.data.repositories.TransactionRepository
import com.example.budgetwise.ui.screens.DashboardScreen
import com.example.budgetwise.ui.screens.HistoryScreen
import com.example.budgetwise.ui.screens.AddTransactionScreen
import com.example.budgetwise.ui.screens.SplashScreen
import com.example.budgetwise.ui.viewmodels.DashboardViewModel
import com.example.budgetwise.ui.viewmodels.HistoryViewModel
import com.example.budgetwise.ui.viewmodels.AddTransactionViewModel

// Daftar route
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Dashboard : Screen("dashboard")
    object AddTransaction : Screen("add_transaction")
    object History : Screen("history")
}

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val app = context.applicationContext as BudgetWiseApp

    // Repository
    val transactionRepository = app.database.transactionDao().let {
        TransactionRepository(it)
    }
    val categoryRepository = app.database.categoryDao().let {
        CategoryRepository(it)
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // Splash Screen
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // Dashboard
        composable(Screen.Dashboard.route) {
            val viewModel: DashboardViewModel = viewModel(
                factory = DashboardViewModel.factory(transactionRepository, categoryRepository)
            )
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToAddTransaction = {
                    navController.navigate(Screen.AddTransaction.route)
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.History.route)
                }
            )
        }

        // Tambah Transaksi
        composable(Screen.AddTransaction.route) {
            val viewModel: AddTransactionViewModel = viewModel(
                factory = AddTransactionViewModel.factory(transactionRepository, categoryRepository)
            )
            AddTransactionScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // History
        composable(Screen.History.route) {
            val viewModel: HistoryViewModel = viewModel(
                factory = HistoryViewModel.factory(transactionRepository, categoryRepository)
            )
            HistoryScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}