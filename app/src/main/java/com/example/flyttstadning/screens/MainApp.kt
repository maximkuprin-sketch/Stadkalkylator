package com.example.flyttstadning.screens

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun MainApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "calculator") {
        composable("calculator") {
            CalculatorScreen(
                onNavigateToPriceList = {
                    navController.navigate("pricelist")
                }
            )
        }
        composable("pricelist") {
            PriceListScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
