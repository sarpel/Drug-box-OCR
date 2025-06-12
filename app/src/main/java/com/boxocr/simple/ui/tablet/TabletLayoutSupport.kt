package com.boxocr.simple.ui.tablet

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.navigationsuite.*
import androidx.compose.material3.windowsizeclass.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.window.layout.DisplayFeature
import androidx.window.layout.FoldingFeature

/**
 * Enhanced Tablet Layout Support for Turkish Medical Application
 * Adaptive layouts optimized for different screen sizes and orientations
 * Supports tablets, foldables, and multi-window scenarios
 */

// Screen size detection utilities
@Composable
fun rememberWindowSizeClass(): WindowSizeClass {
    val configuration = LocalConfiguration.current
    return WindowSizeClass.calculateFromSize(
        size = androidx.compose.ui.unit.DpSize(
            width = configuration.screenWidthDp.dp,
            height = configuration.screenHeightDp.dp
        )
    )
}

// Device type detection
enum class DeviceType {
    PHONE, TABLET, FOLDABLE, DESKTOP
}

@Composable
fun rememberDeviceType(): DeviceType {
    val windowSizeClass = rememberWindowSizeClass()
    
    return when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> DeviceType.PHONE
        WindowWidthSizeClass.Medium -> DeviceType.TABLET
        WindowWidthSizeClass.Expanded -> DeviceType.DESKTOP
        else -> DeviceType.PHONE
    }
}

// Layout configuration for different devices
data class LayoutConfiguration(
    val columns: Int,
    val padding: PaddingValues,
    val spacing: Dp,
    val cardWidth: Dp,
    val useNavigationRail: Boolean,
    val showSecondaryPane: Boolean
)

@Composable
fun rememberLayoutConfiguration(): LayoutConfiguration {
    val windowSizeClass = rememberWindowSizeClass()
    val deviceType = rememberDeviceType()
    
    return when (deviceType) {
        DeviceType.PHONE -> LayoutConfiguration(
            columns = 1,
            padding = PaddingValues(16.dp),
            spacing = 8.dp,
            cardWidth = 0.dp, // Full width
            useNavigationRail = false,
            showSecondaryPane = false
        )
        DeviceType.TABLET -> LayoutConfiguration(
            columns = if (windowSizeClass.heightSizeClass == WindowHeightSizeClass.Compact) 3 else 2,
            padding = PaddingValues(24.dp),
            spacing = 12.dp,
            cardWidth = 320.dp,
            useNavigationRail = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact,
            showSecondaryPane = true
        )
        DeviceType.DESKTOP -> LayoutConfiguration(
            columns = 4,
            padding = PaddingValues(32.dp),
            spacing = 16.dp,
            cardWidth = 280.dp,
            useNavigationRail = true,
            showSecondaryPane = true
        )
        DeviceType.FOLDABLE -> LayoutConfiguration(
            columns = 2,
            padding = PaddingValues(20.dp),
            spacing = 10.dp,
            cardWidth = 300.dp,
            useNavigationRail = true,
            showSecondaryPane = true
        )
    }
}

// Adaptive navigation for tablets
@Composable
fun TurkishMedicalAdaptiveNavigation(
    currentDestination: String,
    onDestinationChange: (String) -> Unit,
    navigationItems: List<NavigationItem>,
    content: @Composable () -> Unit
) {
    val layoutConfig = rememberLayoutConfiguration()
    
    if (layoutConfig.useNavigationRail) {
        Row {
            NavigationRail(
                modifier = Modifier.width(80.dp)
            ) {
                navigationItems.forEach { item ->
                    NavigationRailItem(
                        icon = { Icon(item.icon, contentDescription = item.contentDescription) },
                        label = { Text(item.label) },
                        selected = currentDestination == item.route,
                        onClick = { onDestinationChange(item.route) },
                        alwaysShowLabel = false
                    )
                }
            }
            
            content()
        }
    } else {
        Column {
            content()
            
            NavigationBar {
                navigationItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.contentDescription) },
                        label = { Text(item.label) },
                        selected = currentDestination == item.route,
                        onClick = { onDestinationChange(item.route) }
                    )
                }
            }
        }
    }
}

// Data class for navigation items
data class NavigationItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val contentDescription: String
)

// Adaptive drug list for tablets
@Composable
fun AdaptiveDrugList(
    drugs: List<DrugItemData>,
    onDrugClick: (DrugItemData) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState()
) {
    val layoutConfig = rememberLayoutConfiguration()
    
    when (layoutConfig.columns) {
        1 -> {
            // Single column for phones
            LazyColumn(
                state = listState,
                modifier = modifier.fillMaxSize(),
                contentPadding = layoutConfig.padding,
                verticalArrangement = Arrangement.spacedBy(layoutConfig.spacing)
            ) {
                items(drugs) { drug ->
                    DrugCard(
                        drug = drug,
                        onClick = { onDrugClick(drug) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        else -> {
            // Grid layout for tablets and larger screens
            LazyVerticalGrid(
                columns = GridCells.Fixed(layoutConfig.columns),
                modifier = modifier.fillMaxSize(),
                contentPadding = layoutConfig.padding,
                horizontalArrangement = Arrangement.spacedBy(layoutConfig.spacing),
                verticalArrangement = Arrangement.spacedBy(layoutConfig.spacing)
            ) {
                items(drugs) { drug ->
                    DrugCard(
                        drug = drug,
                        onClick = { onDrugClick(drug) },
                        modifier = if (layoutConfig.cardWidth > 0.dp) {
                            Modifier.width(layoutConfig.cardWidth)
                        } else {
                            Modifier.fillMaxWidth()
                        }
                    )
                }
            }
        }
    }
}

// Tablet optimized two-pane layout
@Composable
fun TwoPaneLayout(
    listContent: @Composable () -> Unit,
    detailContent: @Composable () -> Unit,
    showDetailPane: Boolean,
    modifier: Modifier = Modifier
) {
    val layoutConfig = rememberLayoutConfiguration()
    
    if (layoutConfig.showSecondaryPane && showDetailPane) {
        Row(
            modifier = modifier.fillMaxSize()
        ) {
            // List pane
            Surface(
                modifier = Modifier.weight(1f),
                tonalElevation = 1.dp
            ) {
                listContent()
            }
            
            // Divider
            VerticalDivider()
            
            // Detail pane
            Surface(
                modifier = Modifier.weight(1.5f),
                tonalElevation = 2.dp
            ) {
                detailContent()
            }
        }
    } else {
        // Single pane for phones or when detail is not shown
        listContent()
    }
}

// Adaptive prescription workflow layout
@Composable
fun AdaptivePrescriptionWorkflow(
    currentStep: Int,
    totalSteps: Int,
    stepContent: @Composable () -> Unit,
    sidebarContent: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val layoutConfig = rememberLayoutConfiguration()
    val deviceType = rememberDeviceType()
    
    when (deviceType) {
        DeviceType.PHONE -> {
            // Single column layout for phones
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(layoutConfig.padding)
            ) {
                // Progress indicator
                LinearProgressIndicator(
                    progress = currentStep.toFloat() / totalSteps.toFloat(),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                stepContent()
            }
        }
        DeviceType.TABLET, DeviceType.DESKTOP, DeviceType.FOLDABLE -> {
            // Multi-pane layout for larger screens
            Row(
                modifier = modifier.fillMaxSize()
            ) {
                // Main content area
                Column(
                    modifier = Modifier
                        .weight(if (sidebarContent != null) 2f else 1f)
                        .padding(layoutConfig.padding)
                ) {
                    // Progress indicator
                    LinearProgressIndicator(
                        progress = currentStep.toFloat() / totalSteps.toFloat(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    stepContent()
                }
                
                // Sidebar for additional content
                sidebarContent?.let { sidebar ->
                    VerticalDivider()
                    
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        tonalElevation = 1.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(layoutConfig.padding)
                        ) {
                            sidebar()
                        }
                    }
                }
            }
        }
    }
}

// Tablet-optimized drug scanning interface
@Composable
fun TabletScanningInterface(
    cameraContent: @Composable () -> Unit,
    scanResultContent: @Composable () -> Unit,
    controlsContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val layoutConfig = rememberLayoutConfiguration()
    val deviceType = rememberDeviceType()
    
    when (deviceType) {
        DeviceType.PHONE -> {
            // Stacked layout for phones
            Column(
                modifier = modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    cameraContent()
                }
                
                Surface(
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        controlsContent()
                        Spacer(modifier = Modifier.height(8.dp))
                        scanResultContent()
                    }
                }
            }
        }
        else -> {
            // Side-by-side layout for tablets
            Row(
                modifier = modifier.fillMaxSize()
            ) {
                // Camera area
                Box(
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxHeight()
                ) {
                    cameraContent()
                }
                
                VerticalDivider()
                
                // Controls and results area
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    tonalElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier.padding(layoutConfig.padding)
                    ) {
                        controlsContent()
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        scanResultContent()
                    }
                }
            }
        }
    }
}

// Foldable screen support
@Composable
fun FoldableAwareLayout(
    displayFeatures: List<DisplayFeature>,
    content: @Composable (isFolded: Boolean, foldBounds: androidx.compose.ui.geometry.Rect?) -> Unit
) {
    val foldingFeature = displayFeatures.filterIsInstance<FoldingFeature>().firstOrNull()
    
    val isFolded = foldingFeature?.state == FoldingFeature.State.HALF_OPENED
    val foldBounds = foldingFeature?.bounds?.let { bounds ->
        androidx.compose.ui.geometry.Rect(
            left = bounds.left.toFloat(),
            top = bounds.top.toFloat(),
            right = bounds.right.toFloat(),
            bottom = bounds.bottom.toFloat()
        )
    }
    
    content(isFolded, foldBounds)
}

// Sample drug item data class
data class DrugItemData(
    val id: String,
    val name: String,
    val activeIngredient: String?,
    val confidence: Double,
    val price: Double?,
    val sgkActive: Boolean
)

// Sample drug card component
@Composable
private fun DrugCard(
    drug: DrugItemData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = drug.name,
                style = MaterialTheme.typography.titleMedium
            )
            
            drug.activeIngredient?.let { ingredient ->
                Text(
                    text = ingredient,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${(drug.confidence * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium
                )
                
                if (drug.sgkActive) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "SGK",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

// Responsive spacing utility
@Composable
fun responsiveSpacing(): Dp {
    val layoutConfig = rememberLayoutConfiguration()
    return layoutConfig.spacing
}

// Responsive padding utility
@Composable
fun responsivePadding(): PaddingValues {
    val layoutConfig = rememberLayoutConfiguration()
    return layoutConfig.padding
}

// Screen orientation utilities
enum class ScreenOrientation {
    PORTRAIT, LANDSCAPE
}

@Composable
fun rememberScreenOrientation(): ScreenOrientation {
    val configuration = LocalConfiguration.current
    return if (configuration.screenWidthDp > configuration.screenHeightDp) {
        ScreenOrientation.LANDSCAPE
    } else {
        ScreenOrientation.PORTRAIT
    }
}

// Adaptive content sizing
@Composable
fun AdaptiveContent(
    phoneContent: @Composable () -> Unit,
    tabletContent: @Composable () -> Unit = phoneContent,
    desktopContent: @Composable () -> Unit = tabletContent
) {
    val deviceType = rememberDeviceType()
    
    when (deviceType) {
        DeviceType.PHONE -> phoneContent()
        DeviceType.TABLET, DeviceType.FOLDABLE -> tabletContent()
        DeviceType.DESKTOP -> desktopContent()
    }
}
