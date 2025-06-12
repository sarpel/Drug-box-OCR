package com.boxocr.simple.ui.templates

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.boxocr.simple.data.*

/**
 * Prescription Templates Screen - Phase 2 Feature
 * Browse and select common drug combinations for quick prescription creation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatesScreen(
    onTemplateSelected: (PrescriptionTemplate) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: TemplatesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = { 
                Text(
                    "Prescription Templates",
                    style = MaterialTheme.typography.titleMedium
                ) 
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, "Back")
                }
            },
            actions = {
                IconButton(
                    onClick = { viewModel.toggleSearchMode() }
                ) {
                    Icon(
                        if (uiState.isSearchMode) Icons.Default.Close else Icons.Default.Search,
                        "Search"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        
        // Search Bar (if in search mode)
        if (uiState.isSearchMode) {
            SearchBar(
                query = uiState.searchQuery,
                onQueryChanged = viewModel::updateSearchQuery,
                onClearSearch = viewModel::clearSearch,
                modifier = Modifier.padding(16.dp)
            )
        }
        
        // Content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Popular Templates (if not searching)
            if (!uiState.isSearchMode && uiState.popularTemplates.isNotEmpty()) {
                item {
                    PopularTemplatesSection(
                        templates = uiState.popularTemplates,
                        onTemplateClick = { template ->
                            viewModel.selectTemplate(template)
                            onTemplateSelected(template)
                        }
                    )
                }
            }
            
            // Category Filter (if not searching)
            if (!uiState.isSearchMode) {
                item {
                    CategoryFilterSection(
                        selectedCategory = uiState.selectedCategory,
                        onCategorySelected = viewModel::selectCategory
                    )
                }
            }
            
            // Templates List
            val templatesToShow = if (uiState.isSearchMode) {
                uiState.searchResults
            } else {
                uiState.filteredTemplates
            }
            
            if (templatesToShow.isEmpty()) {
                item {
                    EmptyStateSection(
                        isSearchMode = uiState.isSearchMode,
                        searchQuery = uiState.searchQuery
                    )
                }
            } else {
                items(templatesToShow) { template ->
                    TemplateCard(
                        template = template,
                        onClick = {
                            viewModel.selectTemplate(template)
                            onTemplateSelected(template)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    onClearSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Search templates or drugs...") },
        leadingIcon = { Icon(Icons.Default.Search, "Search") },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClearSearch) {
                    Icon(Icons.Default.Clear, "Clear")
                }
            }
        },
        singleLine = true
    )
}

@Composable
private fun PopularTemplatesSection(
    templates: List<PrescriptionTemplate>,
    onTemplateClick: (PrescriptionTemplate) -> Unit
) {
    Column {
        Text(
            "Popular Templates",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(templates) { template ->
                PopularTemplateChip(
                    template = template,
                    onClick = { onTemplateClick(template) }
                )
            }
        }
    }
}

@Composable
private fun PopularTemplateChip(
    template: PrescriptionTemplate,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .clickable { onClick() }
            .widthIn(min = 140.dp, max = 200.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    getCategoryIcon(template.category),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = getCategoryColor(template.category)
                )
                Text(
                    template.name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                "${template.drugs.size} drugs",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            
            if (template.usageCount > 0) {
                Text(
                    "Used ${template.usageCount}x",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun CategoryFilterSection(
    selectedCategory: TemplateCategory?,
    onCategorySelected: (TemplateCategory?) -> Unit
) {
    Column {
        Text(
            "Categories",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            // "All" filter chip
            item {
                FilterChip(
                    onClick = { onCategorySelected(null) },
                    label = { Text("All") },
                    selected = selectedCategory == null
                )
            }
            
            // Category filter chips
            items(TemplateCategory.values().toList()) { category ->
                FilterChip(
                    onClick = { onCategorySelected(category) },
                    label = { Text(category.displayName) },
                    selected = selectedCategory == category,
                    leadingIcon = {
                        Icon(
                            getCategoryIcon(category),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun TemplateCard(
    template: PrescriptionTemplate,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        template.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        template.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Category Badge
                Surface(
                    color = getCategoryColor(template.category).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            getCategoryIcon(template.category),
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = getCategoryColor(template.category)
                        )
                        Text(
                            template.category.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = getCategoryColor(template.category)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Drugs Preview
            Text(
                "Drugs (${template.drugs.size}):",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                template.drugs.take(3).forEach { drug ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Medication,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "${drug.name}${drug.dosage?.let { " - $it" } ?: ""}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                if (template.drugs.size > 3) {
                    Text(
                        "... and ${template.drugs.size - 3} more",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(start = 24.dp)
                    )
                }
            }
            
            // Footer
            if (template.usageCount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Used ${template.usageCount} times",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun EmptyStateSection(
    isSearchMode: Boolean,
    searchQuery: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                if (isSearchMode) Icons.Default.SearchOff else Icons.Default.MedicalServices,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            
            Text(
                if (isSearchMode) "No templates found" else "No templates available",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            
            if (isSearchMode && searchQuery.isNotEmpty()) {
                Text(
                    "Try searching for different terms",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

// Helper functions for category icons and colors
private fun getCategoryIcon(category: TemplateCategory): ImageVector {
    return when (category) {
        TemplateCategory.DIABETES -> Icons.Default.Bloodtype
        TemplateCategory.HYPERTENSION -> Icons.Default.Favorite
        TemplateCategory.CARDIOVASCULAR -> Icons.Default.FavoriteBorder
        TemplateCategory.RESPIRATORY -> Icons.Default.Air
        TemplateCategory.PAIN_MANAGEMENT -> Icons.Default.Healing
        TemplateCategory.ANTIBIOTIC -> Icons.Default.Science
        TemplateCategory.CHRONIC_DISEASE -> Icons.Default.MedicalServices
        TemplateCategory.PREVENTIVE -> Icons.Default.Shield
        TemplateCategory.CUSTOM -> Icons.Default.PersonalInjury
    }
}

private fun getCategoryColor(category: TemplateCategory): Color {
    return when (category) {
        TemplateCategory.DIABETES -> Color(0xFF2196F3) // Blue
        TemplateCategory.HYPERTENSION -> Color(0xFFF44336) // Red
        TemplateCategory.CARDIOVASCULAR -> Color(0xFFE91E63) // Pink
        TemplateCategory.RESPIRATORY -> Color(0xFF00BCD4) // Cyan
        TemplateCategory.PAIN_MANAGEMENT -> Color(0xFF4CAF50) // Green
        TemplateCategory.ANTIBIOTIC -> Color(0xFFFF9800) // Orange
        TemplateCategory.CHRONIC_DISEASE -> Color(0xFF9C27B0) // Purple
        TemplateCategory.PREVENTIVE -> Color(0xFF607D8B) // Blue Grey
        TemplateCategory.CUSTOM -> Color(0xFF795548) // Brown
    }
}
