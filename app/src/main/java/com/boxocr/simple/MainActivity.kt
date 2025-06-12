package com.boxocr.simple

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.boxocr.simple.ui.batch.BatchScanningScreen
import com.boxocr.simple.ui.camera.CameraScreen
import com.boxocr.simple.ui.enhanced.EnhancedMatchingScreen
import com.boxocr.simple.ui.home.HomeScreen
import com.boxocr.simple.ui.settings.SettingsScreen
import com.boxocr.simple.ui.verification.VerificationScreen
import com.boxocr.simple.ui.templates.TemplatesScreen
import com.boxocr.simple.ui.ai.AIAssistantScreen
import com.boxocr.simple.ui.production.AdvancedAIModelsScreen
import com.boxocr.simple.ui.production.IoTIntegrationScreen
import com.boxocr.simple.ui.production.CustomAIIntegrationScreen
import com.boxocr.simple.ui.multidrug.DrugBoxImageDatabaseScreen
import com.boxocr.simple.ui.multidrug.MultiDrugResultsScreen
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity - Simple navigation between 3 screens:
 * 1. Home (with database management)
 * 2. Camera (OCR capture)
 * 3. Settings (API key)
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            BoxOCRTheme {
                val navController = rememberNavController()
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        composable("home") {
                            HomeScreen(
                                onNavigateToCamera = { navController.navigate("camera") },
                                onNavigateToBatchScanning = { navController.navigate("batch") },
                                onNavigateToTemplates = { navController.navigate("templates") },
                                onNavigateToAIAssistant = { navController.navigate("ai_assistant") },
                                onNavigateToAdvancedAI = { navController.navigate("advanced_ai") },
                                onNavigateToIoTIntegration = { navController.navigate("iot_integration") },
                                onNavigateToCustomAI = { navController.navigate("custom_ai") },
                                onNavigateToDrugBoxDatabase = { navController.navigate("drug_box_database") },
                                onNavigateToSettings = { navController.navigate("settings") }
                            )
                        }
                        
                        composable("camera") {
                            CameraScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToVerification = { navController.navigate("verification") }
                            )
                        }
                        
                        composable("verification") {
                            VerificationScreen(
                                onConfirmed = { drugName ->
                                    // Navigate back to batch scanning with confirmed drug
                                    navController.popBackStack("batch", inclusive = false)
                                },
                                onRejected = {
                                    // Navigate back to camera for rescan
                                    navController.popBackStack("camera", inclusive = false)
                                },
                                onEnhancedMatching = { 
                                    navController.navigate("enhanced_matching")
                                },
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        
                        composable("batch") {
                            BatchScanningScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        
                        composable("enhanced_matching") {
                            EnhancedMatchingScreen(
                                onMatchConfirmed = { drugName ->
                                    // Navigate back with result
                                    navController.popBackStack()
                                },
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        
                        composable("templates") {
                            TemplatesScreen(
                                onTemplateSelected = { template ->
                                    // Navigate to batch scanning with selected template
                                    navController.navigate("batch")
                                },
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        
                        composable("ai_assistant") {
                            AIAssistantScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        
                        composable("settings") {
                            SettingsScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        
                        // 🚀 PRODUCTION FEATURES - PHASE 6 NAVIGATION
                        
                        composable("advanced_ai") {
                            AdvancedAIModelsScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        
                        composable("iot_integration") {
                            IoTIntegrationScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        
                        composable("custom_ai") {
                            CustomAIIntegrationScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        
                        // 🚀 MULTI-DRUG ENHANCEMENT - PHASE 2 NAVIGATION
                        
                        composable("drug_box_database") {
                            DrugBoxImageDatabaseScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        
                        composable("multi_drug_results") {
                            MultiDrugResultsScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToBatch = { navController.navigate("batch") }
                            )
                        }
                    }
                }
            }
        }
    }
}
