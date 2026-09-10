package com.jgv.workoutplanner.feature.home

import com.jgv.workoutplanner.domain.model.ExperienceLevel
import com.jgv.workoutplanner.domain.model.TrainingGoal
import com.jgv.workoutplanner.domain.model.WorkoutSplit

/**
 * Immutable state for the home screen (spec §16, §20).
 *
 * Shows the current plan summary, profile summary,
 * active limitations count, and exercise library entry point.
 */
data class HomeUiState(
    val isLoading: Boolean = true,
    val daysPerWeek: Int? = null,
    val goal: TrainingGoal? = null,
    val experienceLevel: ExperienceLevel? = null,
    val split: WorkoutSplit? = null,
    val limitationsCount: Int = 0,
    val planName: String? = null,
    val requiresRegeneration: Boolean = false,
) {
    /**
     * Derived rather than stored: a saved plan always has a name, so a separate flag
     * could only ever disagree with [planName] — and the screen would then need a
     * fallback string for a state that cannot happen.
     */
    val hasPlan: Boolean get() = planName != null
}
