package com.jgv.workoutplanner.domain.model

/**
 * An exercise scored for a specific template slot (spec §12.5).
 *
 * Ranking is deterministic: [score] descending, then [ExerciseId.name] stable
 * (spec §12.5 tie-break rule) so same profile always yields same plan.
 */
data class RankedExercise(
    val exercise: ExerciseDefinition,
    val score: Int,
)
