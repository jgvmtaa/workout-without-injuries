package com.jgv.workoutplanner.domain.model

/**
 * Manual plan mutations — single-use-case sealed contract (Phase 6 §6.1, README §13).
 *
 * UI does not edit WorkoutPlan directly. Every mutation goes through
 * UpdateWorkoutExerciseUseCase which validates against profile filters and capacity.
 */
sealed interface WorkoutExerciseEdit {
    data class Remove(
        val workoutDayId: String,
        val exerciseId: ExerciseId,
    ) : WorkoutExerciseEdit

    data class Replace(
        val workoutDayId: String,
        val currentExerciseId: ExerciseId,
        val replacementExerciseId: ExerciseId,
    ) : WorkoutExerciseEdit

    data class Move(
        val workoutDayId: String,
        val exerciseId: ExerciseId,
        val targetIndex: Int,
    ) : WorkoutExerciseEdit

    data class AddExercises(
        val workoutDayId: String,
        val exerciseIds: List<ExerciseId>,
    ) : WorkoutExerciseEdit
}

/**
 * Typed result for manual edits — no throws for expected failures (Phase 6 §6.1).
 */
sealed interface WorkoutExerciseEditResult {
    data object Updated : WorkoutExerciseEditResult
    data object Unchanged : WorkoutExerciseEditResult
    data object PlanNotFound : WorkoutExerciseEditResult
    data object PlanOutdated : WorkoutExerciseEditResult
    data object DayNotFound : WorkoutExerciseEditResult
    data object ExerciseNotFound : WorkoutExerciseEditResult
    data object InvalidTargetIndex : WorkoutExerciseEditResult
    data object IneligibleExercise : WorkoutExerciseEditResult
    data object DuplicateExercise : WorkoutExerciseEditResult
    data object CapacityExceeded : WorkoutExerciseEditResult
}
