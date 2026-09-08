package com.jgv.workoutplanner.feature.home

import com.jgv.workoutplanner.domain.model.ExperienceLevel
import com.jgv.workoutplanner.domain.model.TrainingGoal
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
    val goal: TrainingGoal? = null,
    val experienceLevel: ExperienceLevel? = null,
    val split: WorkoutSplit? = null,
    val limitationsCount: Int = 0,
    val hasPlan: Boolean = false,
    val planName: String? = null,
    val requiresRegeneration: Boolean = false,
)
