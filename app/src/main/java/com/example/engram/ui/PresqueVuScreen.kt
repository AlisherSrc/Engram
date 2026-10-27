package com.example.engram.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.engram.viewmodel.PresqueVuViewModel

/**
 * Top-level screen entrypoint for Presque Vu.
 * Delegates to [PresqueVuApp], featuring Jetpack Compose Navigation,
 * reactive StateFlow architecture, and Coroutine-based recall drills.
 */
@Composable
fun PresqueVuScreen(
    viewModel: PresqueVuViewModel,
    modifier: Modifier = Modifier
) {
    PresqueVuApp(
        viewModel = viewModel,
        modifier = modifier
    )
}
