package com.example.engram.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.engram.model.ConstantCategory
import com.example.engram.model.ConstantItem
import com.example.engram.ui.components.ConstantItemCard
import com.example.engram.ui.components.FilterOption
import com.example.engram.ui.components.SearchHeader

/**
 * Screen 2: Constants Vault (Presque Vu Tab).
 * Vault of vital personal numbers, addresses, dates, and IDs.
 * Implements strict state hoisting.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConstantsVaultScreen(
    constants: List<ConstantItem>,
    selectedCategory: ConstantCategory?,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onCategorySelected: (ConstantCategory?) -> Unit,
    onIncrementMastery: (String) -> Unit,
    onDeleteConstant: (String) -> Unit,
    onOpenAddDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val constantChips = listOf(
        FilterOption(ConstantCategory.CONTACT.name, "Contacts"),
        FilterOption(ConstantCategory.ADDRESS.name, "Addresses"),
        FilterOption(ConstantCategory.DATE.name, "Dates"),
        FilterOption(ConstantCategory.ID_NUMBER.name, "ID Numbers")
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Constants Vault",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Vital Codes, Numbers & Landmarks",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onOpenAddDialog,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Constant Node"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search & Filter Header
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                SearchHeader(
                    searchQuery = searchQuery,
                    onSearchQueryChange = onSearchQueryChange,
                    placeholderText = "Search phone, address, ID or hint...",
                    filterChips = constantChips,
                    selectedFilterKey = selectedCategory?.name,
                    onFilterSelected = { key ->
                        val cat = key?.let { ConstantCategory.valueOf(it) }
                        onCategorySelected(cat)
                    },
                    matchCount = constants.size
                )
            }

            // LazyColumn for Constant Items
            if (constants.isEmpty()) {
                EmptyConstantsNotice(
                    searchQuery = searchQuery,
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = constants,
                        key = { it.id }
                    ) { item ->
                        ConstantItemCard(
                            item = item,
                            onIncrementMastery = { onIncrementMastery(item.id) },
                            onDelete = { onDeleteConstant(item.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyConstantsNotice(
    searchQuery: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.SearchOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Constants Found",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (searchQuery.isNotEmpty()) {
                "No vital constant matches \"$searchQuery\". Try adjusting your query or filter."
            } else {
                "You haven't stored any vital constants in this category yet. Tap + to store a vital number!"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
