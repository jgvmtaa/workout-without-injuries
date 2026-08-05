package com.jgv.workoutplanner.feature.home

/**
 * Immutable state for the home screen (README §20).
 *
 * Phase 1 only establishes the shape: one state model per screen, hoisted out of the
 * composable. The plan summary described in README §16 arrives in Phase 5.
 */
data class HomeUiState(
    val isLoading: Boolean = false,
)
