package com.example.engram.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.engram.navigation.Screen
import com.example.engram.navigation.bottomNavigationItems
import com.example.engram.ui.components.AddMemoryDialog
import com.example.engram.ui.screens.ChallengeScreen
import com.example.engram.ui.screens.ConstantsVaultScreen
import com.example.engram.ui.screens.DashboardScreen
import com.example.engram.ui.screens.LexiconScreen
import com.example.engram.viewmodel.PresqueVuViewModel

/**
 * Main application composable for Presque Vu.
 * Configures the Navigation architecture:
 * - Scaffold with Material 3 NavigationBar and NavHost.
 * - Collects StateFlow state reactively using [collectAsStateWithLifecycle].
 * - Strictly enforces state hoisting: passes state values and event lambdas to child screens.
 */
@Composable
fun PresqueVuApp(
    viewModel: PresqueVuViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    // Reactive StateFlow collection using collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Navigation backstack tracking for Bottom Bar selection
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Dialog state (survives screen rotation via rememberSaveable)
    var isAddDialogOpen by rememberSaveable { mutableStateOf(false) }
    var initialDialogTab by rememberSaveable { mutableIntStateOf(0) }

    // Bottom Navigation Bar is shown on top-level screens, hidden on ChallengeScreen
    val showBottomBar = currentRoute in listOf(
        Screen.Dashboard.route,
        Screen.ConstantsVault.route,
        Screen.Lexicon.route
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    bottomNavigationItems.forEach { item ->
                        val isSelected = currentRoute == item.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.contentDescription
                                )
                            },
                            label = { Text(item.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // 1. DashboardScreen (Home)
            composable(route = Screen.Dashboard.route) {
                DashboardScreen(
                    uiState = uiState,
                    onStartRecallDrill = { isTimedMode ->
                        navController.navigate(Screen.Challenge.createRoute(isTimedMode))
                    },
                    onRefreshSpotlight = {
                        viewModel.refreshSpotlightPhrase()
                    },
                    onNavigateToVault = {
                        navController.navigate(Screen.ConstantsVault.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToLexicon = {
                        navController.navigate(Screen.Lexicon.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            // 2. ConstantsVaultScreen (Presque Vu Tab)
            composable(route = Screen.ConstantsVault.route) {
                ConstantsVaultScreen(
                    constants = uiState.filteredConstants,
                    selectedCategory = uiState.selectedConstantCategory,
                    searchQuery = uiState.constantsSearchQuery,
                    onSearchQueryChange = { query ->
                        viewModel.setConstantsSearchQuery(query)
                    },
                    onCategorySelected = { category ->
                        viewModel.setConstantCategoryFilter(category)
                    },
                    onIncrementMastery = { id ->
                        viewModel.incrementConstantMastery(id)
                    },
                    onDeleteConstant = { id ->
                        viewModel.deleteConstant(id)
                    },
                    onOpenAddDialog = {
                        initialDialogTab = 0
                        isAddDialogOpen = true
                    }
                )
            }

            // 3. LexiconScreen (Wordsmith Tab)
            composable(route = Screen.Lexicon.route) {
                LexiconScreen(
                    lexicon = uiState.filteredLexicon,
                    selectedFilter = uiState.selectedLexiconFilter,
                    searchQuery = uiState.lexiconSearchQuery,
                    onSearchQueryChange = { query ->
                        viewModel.setLexiconSearchQuery(query)
                    },
                    onFilterSelected = { filter ->
                        viewModel.setLexiconFilter(filter)
                    },
                    onIncrementMastery = { id ->
                        viewModel.incrementLexiconMastery(id)
                    },
                    onDeleteLexicon = { id ->
                        viewModel.deleteLexicon(id)
                    },
                    onOpenAddDialog = {
                        initialDialogTab = 1
                        isAddDialogOpen = true
                    }
                )
            }

            // 4. ChallengeScreen (Interactive Quiz & Timed Mode)
            composable(
                route = Screen.Challenge.route,
                arguments = listOf(
                    navArgument("isTimedMode") {
                        type = NavType.BoolType
                        defaultValue = false
                    }
                )
            ) { backStackEntry ->
                val isTimedMode = backStackEntry.arguments?.getBoolean("isTimedMode") ?: false

                ChallengeScreen(
                    isTimedMode = isTimedMode,
                    constants = uiState.constants,
                    lexicon = uiState.lexicon,
                    onAnswerConstant = { id, isCorrect ->
                        viewModel.recordConstantRecallResult(id, isCorrect)
                    },
                    onRecordLexiconSentence = { id, sentence ->
                        viewModel.recordLexiconPracticeSentence(id, sentence)
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }

    // Modal Add Memory Dialog (Survives rotation via rememberSaveable)
    if (isAddDialogOpen) {
        AddMemoryDialog(
            initialTab = initialDialogTab,
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
