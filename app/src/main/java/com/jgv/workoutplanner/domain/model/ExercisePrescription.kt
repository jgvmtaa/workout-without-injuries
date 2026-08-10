package com.jgv.workoutplanner.domain.model

/**
 * The catalog's default dosing for an exercise (README §5.3).
 *
 * Ranges rather than fixed numbers: plan generation narrows them to a concrete set
 * count and rep range based on the user's goal (README §12), and the user can adjust
 * from there.
 *
 * Isometric holds (planks) have no natural rep count. They use `1..1` and state the
 * hold duration in their description — the model has no duration field yet, tracked
 * in docs/follow-ups.md.
 */
data class ExercisePrescription(
    val sets: IntRange,
    val reps: IntRange,
    val restSeconds: Int,
)
