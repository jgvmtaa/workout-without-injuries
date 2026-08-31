package com.jgv.workoutplanner.feature.home

import com.jgv.workoutplanner.domain.model.WorkoutSplit

/**
 * Immutable state for the home screen (README §20, §16, task 5.8).
 *
 * Real implementation for Phase 5: shows current plan summary, profile summary,
 * active limitations count, and exercise library entry point.
 */
data class HomeUiState(
    val isLoading: Boolean = true,
    val daysPerWeek: Int? = null,
    val goalDisplay: String? = null,
    val experienceDisplay: String? = null,
    val split: WorkoutSplit? = null,
    val limitationsCount: Int = 0,
    val hasPlan: Boolean = false,
    val planName: String? = null,
)
