package com.jgv.workoutplanner.feature.plan

import com.jgv.workoutplanner.domain.model.ExerciseId

/**
 * User actions on the plan screen (README §20).
 *
 * Immutable events consumed by [PlanViewModel.onEvent].
 */
sealed interface PlanEvent {
    data object GeneratePlan : PlanEvent
    data object RegeneratePlan : PlanEvent
    data object ConfirmRegeneration : PlanEvent
    data object DismissRegenerationConfirm : PlanEvent
    data object ConfirmRemoveExercise : PlanEvent
    data object DismissRemoveExerciseConfirm : PlanEvent
    data class OpenExerciseDetails(val exerciseId: ExerciseId) : PlanEvent
    data class ReplaceExercise(val dayId: String, val exerciseId: ExerciseId) : PlanEvent
    data class RequestRemoveExercise(val dayId: String, val exerciseId: ExerciseId) : PlanEvent
    data class MoveUp(val dayId: String, val exerciseId: ExerciseId) : PlanEvent
    data class MoveDown(val dayId: String, val exerciseId: ExerciseId) : PlanEvent
    data class OpenExercisePicker(val dayId: String) : PlanEvent
    data object Back : PlanEvent
}
