package com.jgv.workoutplanner.feature.exercisedetails

import com.jgv.workoutplanner.domain.model.ExerciseAvailability
import com.jgv.workoutplanner.domain.model.ExerciseDefinition
import com.jgv.workoutplanner.domain.model.ExerciseEligibility

/**
 * Immutable state for the exercise detail screen (spec §15, §20).
 *
 * Holds the resolved catalog entry plus its eligibility for the current profile,
 * so every exclusion is explainable (spec §15).
 */
data class ExerciseDetailsUiState(
    val isLoading: Boolean = true,
    val definition: ExerciseDefinition? = null,
    val eligibility: ExerciseEligibility? = null,
    val availability: ExerciseAvailability? = null,
    val currentExperienceLevel: com.jgv.workoutplanner.domain.model.ExperienceLevel? = null,
)

/** Events the detail screen can send (spec §20). */
sealed interface ExerciseDetailsEvent {
    data object Back : ExerciseDetailsEvent
}
