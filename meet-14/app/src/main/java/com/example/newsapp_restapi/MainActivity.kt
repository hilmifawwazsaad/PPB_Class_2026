package com.example.newsapp_restapi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.newsapp_restapi.data.model.Article
import com.example.newsapp_restapi.ui.screen.DetailScreen
import com.example.newsapp_restapi.ui.screen.HomeScreen
import com.example.newsapp_restapi.ui.screen.SearchScreen
import com.example.newsapp_restapi.ui.theme.NewsAppTheme
import com.example.newsapp_restapi.viewmodel.NewsViewModel

object Routes {
    const val HOME   = "home"
    const val SEARCH = "search"
    const val DETAIL = "detail"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NewsAppTheme {
                NewsApp()
            }
        }
    }
}

@Composable
fun NewsApp() {
    val navController   = rememberNavController()
    val viewModel: NewsViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var selectedArticle by remember { mutableStateOf<Article?>(null) }

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                uiState        = uiState,
                viewModel      = viewModel,
                onArticleClick = { article ->
                    selectedArticle = article
                    navController.navigate(Routes.DETAIL)
                },
                onSearchClick  = { navController.navigate(Routes.SEARCH) }
            )
        }
        composable(Routes.SEARCH) {
            SearchScreen(
                uiState        = uiState,
                viewModel      = viewModel,
                onArticleClick = { article ->
                    selectedArticle = article
                    navController.navigate(Routes.DETAIL)
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.DETAIL) {
            selectedArticle?.let { article ->
                DetailScreen(
                    article = article,
                    onBack  = { navController.popBackStack() }
                )
            }
        }
    }
}