package com.example.engram.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Navigation routes and destinations for Presque Vu.
 */
sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object ConstantsVault : Screen("constants_vault")
    data object Lexicon : Screen("lexicon")
    data object Challenge : Screen("challenge/{isTimedMode}") {
        fun createRoute(isTimedMode: Boolean): String = "challenge/$isTimedMode"
    }
}

/**
 * Top-level destinations represented in the Scaffold's NavigationBar.
 */
data class NavigationItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val contentDescription: String
)

val bottomNavigationItems = listOf(
    NavigationItem(
        route = Screen.Dashboard.route,
        label = "Dashboard",
        icon = Icons.Default.Home,
        contentDescription = "Navigate to Dashboard"
    ),
    NavigationItem(
        route = Screen.ConstantsVault.route,
        label = "Vault",
        icon = Icons.Default.DataObject,
        contentDescription = "Navigate to Constants Vault"
    ),
    NavigationItem(
        route = Screen.Lexicon.route,
        label = "Lexicon",
        icon = Icons.AutoMirrored.Filled.MenuBook,
        contentDescription = "Navigate to Wordsmith Lexicon"
    )
)
