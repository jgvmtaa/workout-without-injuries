package com.jgv.workoutplanner.feature.plan

import com.jgv.workoutplanner.domain.model.ExerciseDefinition
import com.jgv.workoutplanner.domain.model.ExerciseId

/**
 * Immutable state for the replacement screen (spec §13, §20).
 */
data class ExerciseReplacementUiState(
    val isLoading: Boolean = true,
    val currentExerciseName: String? = null,
    val candidates: List<ExerciseDefinition> = emptyList(),
    val selectedExerciseId: ExerciseId? = null,
    val noCandidates: Boolean = false,
    val isStale: Boolean = false,
)

sealed interface ExerciseReplacementEvent {
    data class SelectCandidate(val exerciseId: com.jgv.workoutplanner.domain.model.ExerciseId) : ExerciseReplacementEvent
    data object ConfirmReplacement : ExerciseReplacementEvent
    data object Back : ExerciseReplacementEvent
}
