package com.example.engram.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.engram.model.ConstantCategory
import com.example.engram.ui.components.AddMemoryDialog
import com.example.engram.ui.components.ConstantItemCard
import com.example.engram.ui.components.FilterOption
import com.example.engram.ui.components.LexiconItemCard
import com.example.engram.ui.components.MemoryStatsBanner
import com.example.engram.ui.components.SearchHeader
import com.example.engram.viewmodel.PresqueVuViewModel

/**
 * Main application screen for Presque Vu.
 *
 * Demonstrates proper separation of state:
 * - [PresqueVuViewModel]: Holds master lists, data transformations, and active filters.
 * - [rememberSaveable]:
 *     1. selectedTabIndex (Constants tab vs Lexicon tab)
 *     2. searchQuery (active search filter text)
 *     3. isAddDialogOpen (modal dialog visibility)
 *     All survive device rotation without losing user context!
 * - [remember]: Ephemeral card visual states (spoiler peek and expansion) in child composables.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresqueVuScreen(
    viewModel: PresqueVuViewModel,
    modifier: Modifier = Modifier
) {
    // Collect ViewModel StateFlows
    val constants by viewModel.constants.collectAsState()
    val lexicon by viewModel.lexicon.collectAsState()
    val selectedCategoryFilter by viewModel.selectedConstantCategory.collectAsState()
    val selectedLexiconFilter by viewModel.selectedLexiconFilter.collectAsState()

    // UI state that MUST survive screen rotation (Activity recreation)
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var isAddDialogOpen by rememberSaveable { mutableStateOf(false) }

    // Filter computation delegated to ViewModel
    val filteredConstants = viewModel.getFilteredConstants(searchQuery, selectedCategoryFilter)
    val filteredLexicon = viewModel.getFilteredLexicon(searchQuery, selectedLexiconFilter)

    // Statistics computation
    val totalNodes = constants.size + lexicon.size
    val totalMastered = constants.count { it.masteryLevel >= 5 } + lexicon.count { it.masteryLevel >= 5 }
    val totalMasterySum = constants.sumOf { it.masteryLevel } + lexicon.sumOf { it.masteryLevel }
    val averageMastery = if (totalNodes > 0) totalMasterySum.toFloat() / totalNodes else 0f

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Presque Vu",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tip of the Tongue • Active Recall Palace",
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
                onClick = { isAddDialogOpen = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Memory Node"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Retention Stats Banner
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                MemoryStatsBanner(
                    totalItems = totalNodes,
                    masteredItems = totalMastered,
                    averageMastery = averageMastery
                )
            }

            // Primary Tabs: Vital Constants vs. Wordsmith Lexicon
            PrimaryTabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Vital Constants (${constants.size})") },
                    icon = { Icon(Icons.Default.DataObject, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Lexicon (${lexicon.size})") },
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            // Animated Tab Content
            AnimatedContent(
                targetState = selectedTabIndex,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tab_content_transition",
                modifier = Modifier.weight(1f)
            ) { targetTab ->
                if (targetTab == 0) {
                    // --- Vital Constants View ---
                    val constantChips = listOf(
                        FilterOption(ConstantCategory.CONTACT.name, "Contacts"),
                        FilterOption(ConstantCategory.ADDRESS.name, "Addresses"),
                        FilterOption(ConstantCategory.DATE.name, "Dates"),
                        FilterOption(ConstantCategory.ID_NUMBER.name, "ID Numbers")
                    )

                    Column(modifier = Modifier.fillMaxSize()) {
                        // Search & Filter Header
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            SearchHeader(
                                searchQuery = searchQuery,
                                onSearchQueryChange = { searchQuery = it },
                                placeholderText = "Search phone, address, ID or hint...",
                                filterChips = constantChips,
                                selectedFilterKey = selectedCategoryFilter?.name,
                                onFilterSelected = { key ->
                                    val cat = key?.let { ConstantCategory.valueOf(it) }
                                    viewModel.setConstantCategoryFilter(cat)
                                },
                                matchCount = filteredConstants.size
                            )
                        }

                        // LazyColumn for Constant Items
                        if (filteredConstants.isEmpty()) {
                            EmptyStateNotice(
                                title = "No Constants Found",
                                description = if (searchQuery.isNotEmpty()) {
                                    "No vital constant matches \"$searchQuery\". Try adjusting your query or filter."
                                } else {
                                    "You haven't stored any vital constants in this category yet. Tap + to add one!"
                                }
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(
                                    items = filteredConstants,
                                    key = { it.id }
                                ) { item ->
                                    ConstantItemCard(
                                        item = item,
                                        onIncrementMastery = { viewModel.incrementConstantMastery(item.id) },
                                        onDelete = { viewModel.deleteConstant(item.id) }
                                    )
                                }
                            }
                        }
                    }

                } else {
                    // --- Lexicon (Wordsmith) View ---
                    val lexiconChips = listOf(
                        FilterOption("EN", "EN"),
                        FilterOption("RU", "RU"),
                        FilterOption("KZ", "KZ"),
                        FilterOption("#Sci-Fi", "#Sci-Fi"),
                        FilterOption("#Atmosphere", "#Atmosphere"),
                        FilterOption("#Idiom", "#Idiom")
                    )

                    Column(modifier = Modifier.fillMaxSize()) {
                        // Search & Filter Header
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            SearchHeader(
                                searchQuery = searchQuery,
                                onSearchQueryChange = { searchQuery = it },
                                placeholderText = "Search phrase, meaning, tag or author...",
                                filterChips = lexiconChips,
                                selectedFilterKey = selectedLexiconFilter,
                                onFilterSelected = { key ->
                                    viewModel.setLexiconFilter(key)
                                },
                                matchCount = filteredLexicon.size
                            )
                        }

                        // LazyColumn for Lexicon Items
                        if (filteredLexicon.isEmpty()) {
                            EmptyStateNotice(
                                title = "No Lexicon Phrases Found",
                                description = if (searchQuery.isNotEmpty()) {
                                    "No expressive phrase matches \"$searchQuery\". Try adjusting your query or filter."
                                } else {
                                    "Your wordsmith lexicon is empty for this filter. Tap + to store a vivid phrase!"
                                }
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(
                                    items = filteredLexicon,
                                    key = { it.id }
                                ) { item ->
                                    LexiconItemCard(
                                        item = item,
                                        onIncrementMastery = { viewModel.incrementLexiconMastery(item.id) },
                                        onDelete = { viewModel.deleteLexicon(item.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Add Memory Dialog (Survives rotation via rememberSaveable)
    if (isAddDialogOpen) {
        AddMemoryDialog(
            initialTab = selectedTabIndex,
            onDismissRequest = { isAddDialogOpen = false },
            onAddConstant = { title, secretValue, category, hint, initialMastery ->
                viewModel.addConstant(title, secretValue, category, hint, initialMastery)
            },
            onAddLexicon = { title, phrase, meaning, language, tag, sourceBook, initialMastery ->
                viewModel.addLexicon(title, phrase, meaning, language, tag, sourceBook, initialMastery)
            }
        )
    }
}

/**
 * Friendly empty state indicator when no items match search or filter.
 */
@Composable
private fun EmptyStateNotice(
    title: String,
    description: String,
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
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
