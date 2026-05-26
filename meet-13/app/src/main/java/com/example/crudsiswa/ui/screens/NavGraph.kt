package com.example.crudsiswa.ui.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.crudsiswa.viewmodels.StudentViewModel

object Routes {
    const val STUDENT_LIST = "student_list"
    const val STUDENT_ADD = "student_add"
    const val STUDENT_EDIT = "student_edit/{studentId}"
    fun studentEdit(studentId: Int) = "student_edit/$studentId"
}

@Composable
fun StudentNavGraph(viewModel: StudentViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.STUDENT_LIST
    ) {
        composable(Routes.STUDENT_LIST) {
            StudentListScreen(
                viewModel = viewModel,
                onNavigateToAdd = { navController.navigate(Routes.STUDENT_ADD) },
                onNavigateToDetail = { id -> navController.navigate(Routes.studentEdit(id)) }
            )
        }

        composable(Routes.STUDENT_ADD) {
            StudentFormScreen(
                viewModel = viewModel,
                studentId = null,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.STUDENT_EDIT,
            arguments = listOf(navArgument("studentId") { type = NavType.IntType })
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getInt("studentId")
            StudentFormScreen(
                viewModel = viewModel,
                studentId = studentId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
