package com.example.marketplacesiswa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.marketplacesiswa.ui.navigation.Screen
import com.example.marketplacesiswa.ui.screens.AddEditProductScreen
import com.example.marketplacesiswa.ui.screens.HomeScreen
import com.example.marketplacesiswa.ui.screens.OnboardingScreen
import com.example.marketplacesiswa.ui.screens.SplashScreen
import com.example.marketplacesiswa.ui.theme.MarketplaceSiswaTheme
import com.example.marketplacesiswa.ui.viewmodels.OnboardingViewModel
import com.example.marketplacesiswa.ui.viewmodels.ProductViewModel

class MainActivity : ComponentActivity() {

    private val productViewModel: ProductViewModel by viewModels()
    private val onboardingViewModel: OnboardingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MarketplaceSiswaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MarketplaceApp(
                        productViewModel = productViewModel,
                        onboardingViewModel = onboardingViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun MarketplaceApp(
    productViewModel: ProductViewModel,
    onboardingViewModel: OnboardingViewModel
) {
    val isOnboardingCompleted by onboardingViewModel.isOnboardingCompleted.collectAsState()
    val navController = rememberNavController()

    if (isOnboardingCompleted == null) return

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    val dest = if (isOnboardingCompleted == true) Screen.Home.route
                    else Screen.Onboarding.route
                    navController.navigate(dest) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinish = {
                    onboardingViewModel.setOnboardingCompleted()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = productViewModel,
                onNavigateToEdit = { product ->
                    navController.navigate(Screen.EditProduct.createRoute(product.id))
                },
                onAddProduct = {
                    navController.navigate(Screen.AddProduct.route)
                }
            )
        }

        composable(Screen.AddProduct.route) {
            AddEditProductScreen(
                viewModel = productViewModel,
                productId = null,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.EditProduct.route,
            arguments = listOf(navArgument("productId") { type = NavType.IntType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("productId")
            AddEditProductScreen(
                viewModel = productViewModel,
                productId = productId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
