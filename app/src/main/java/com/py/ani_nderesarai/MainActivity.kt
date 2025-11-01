package com.py.ani_nderesarai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.py.ani_nderesarai.ui.screens.AddEditReminderScreen
import com.py.ani_nderesarai.ui.screens.HomeScreen
import com.py.ani_nderesarai.ui.theme.AniNderesaraiTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AniNderesaraiTheme {
                AniNderesaraiApp()
            }
        }
    }
}

@Composable
fun AniNderesaraiApp() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onNavigateToAddReminder = {
                    navController.navigate("add_reminder")
                },
                onNavigateToEditReminder = { reminderId ->
                    navController.navigate("edit_reminder/$reminderId")
                }
            )
        }
        
        composable("add_reminder") {
            AddEditReminderScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("edit_reminder/{reminderId}") { backStackEntry ->
            val reminderId = backStackEntry.arguments?.getString("reminderId")?.toLongOrNull() ?: 0L
            AddEditReminderScreen(
                reminderId = reminderId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}