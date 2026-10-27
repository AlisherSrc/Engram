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
import com.example.engram.model.LexiconItem
import com.example.engram.ui.components.FilterOption
import com.example.engram.ui.components.LexiconItemCard
import com.example.engram.ui.components.SearchHeader

/**
 * Screen 3: Lexicon (Wordsmith Tab).
 * Multilingual sci-fi expressions and literary quotes with practice history.
 * Implements strict state hoisting.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LexiconScreen(
    lexicon: List<LexiconItem>,
    selectedFilter: String?,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onFilterSelected: (String?) -> Unit,
    onIncrementMastery: (String) -> Unit,
    onDeleteLexicon: (String) -> Unit,
    onOpenAddDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lexiconChips = listOf(
        FilterOption("EN", "EN"),
        FilterOption("RU", "RU"),
        FilterOption("KZ", "KZ"),
        FilterOption("#Sci-Fi", "#Sci-Fi"),
        FilterOption("#Atmosphere", "#Atmosphere"),
        FilterOption("#Idiom", "#Idiom")
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Wordsmith Lexicon",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Sci-Fi Expressions & Atmospheric Quotes",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary
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
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Lexicon Node"
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
                    placeholderText = "Search phrase, meaning, tag or author...",
                    filterChips = lexiconChips,
                    selectedFilterKey = selectedFilter,
                    onFilterSelected = onFilterSelected,
                    matchCount = lexicon.size
                )
            }

            // LazyColumn for Lexicon Items
            if (lexicon.isEmpty()) {
                EmptyLexiconNotice(
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
                        items = lexicon,
                        key = { it.id }
                    ) { item ->
                        LexiconItemCard(
                            item = item,
                            onIncrementMastery = { onIncrementMastery(item.id) },
                            onDelete = { onDeleteLexicon(item.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyLexiconNotice(
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
            text = "No Lexicon Phrases Found",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (searchQuery.isNotEmpty()) {
                "No expressive phrase matches \"$searchQuery\". Try adjusting your query or filter."
            } else {
                "Your wordsmith lexicon is empty for this filter. Tap + to store a vivid quote or idiom!"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
