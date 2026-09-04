package com.jgv.workoutplanner.feature.plan

import com.jgv.workoutplanner.domain.model.ExerciseDefinition
import com.jgv.workoutplanner.domain.model.ExerciseId
import com.jgv.workoutplanner.domain.model.PlanWarning
import com.jgv.workoutplanner.domain.model.WorkoutDayFocus

/**
 * Immutable state for the plan screen (README §20, task 5.8).
 *
 * Rendered by a previewable [PlanScreen] that receives this value – no ViewModel, no
 * NavController inside the composable.
 *
 * Warnings are the partial-plan signals (README §12.4 / §25): a slot could not be filled
 * without conflicting with limitations/equipment. Shown inline, not as crash.
 */
data class PlanUiState(
    val isLoading: Boolean = true,
    val isGenerating: Boolean = false,
    val planName: String? = null,
    val days: List<WorkoutDayUiModel> = emptyList(),
    val warnings: List<PlanWarning> = emptyList(),
    val hasNoPlan: Boolean = false,
    val requiresRegeneration: Boolean = false,
)

data class WorkoutDayUiModel(
    val id: String,
    val name: String,
    val focus: WorkoutDayFocus,
    val exercises: List<PlannedExerciseUiModel>,
    val remainingCapacity: Int = 0,
)

data class PlannedExerciseUiModel(
    val exerciseId: ExerciseId,
    val definition: ExerciseDefinition?,
    val sets: Int,
    val repRange: IntRange,
    val restSeconds: Int,
    val order: Int,
)

/**
 * One-shot effects for recoverable edit failures — typed, not raw String (Phase 6 §6.1).
 */
sealed interface PlanEffect {
    data class EditFailed(val result: com.jgv.workoutplanner.domain.model.WorkoutExerciseEditResult) : PlanEffect
}
