package com.jgv.workoutplanner.domain.model

/**
 * The catalog's default dosing for an exercise (spec §5.3).
 *
 * Plan generation uses the first set count, the complete repetition range, and the
 * default rest duration without goal-based adjustment (spec §5.3, §12).
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
