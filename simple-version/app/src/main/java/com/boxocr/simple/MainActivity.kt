package com.boxocr.simple

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.boxocr.simple.ui.camera.CameraScreen
import com.boxocr.simple.ui.home.HomeScreen
import com.boxocr.simple.ui.settings.SettingsScreen
import com.boxocr.simple.ui.theme.BoxOCRTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BoxOCRTheme {
                BoxOCRApp()
            }
        }
    }
}

@Composable
fun BoxOCRApp() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onNavigateToCamera = { navController.navigate("camera") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        composable("camera") {
            CameraScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("settings") {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
