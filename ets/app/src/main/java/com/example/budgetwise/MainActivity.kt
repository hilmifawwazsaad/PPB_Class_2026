package com.example.budgetwise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.budgetwise.ui.navigation.NavGraph
import com.example.budgetwise.ui.theme.BudgetWiseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BudgetWiseTheme {
                NavGraph()
            }
        }
    }
}
