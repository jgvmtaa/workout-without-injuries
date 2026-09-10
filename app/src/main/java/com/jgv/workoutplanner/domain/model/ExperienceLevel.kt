package com.jgv.workoutplanner.domain.model

/**
 * How much training experience the user reports (spec §4.3).
 *
 * Compared against [ExerciseDifficulty] by the eligibility engine using the strict
 * support matrix in spec §8.
 */
enum class ExperienceLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED;

    /**
     * Whether this experience level can perform an exercise of [difficulty] (spec §8).
     *
     * BEGINNER → only BEGINNER, INTERMEDIATE → BEGINNER + INTERMEDIATE, ADVANCED → all.
     */
    fun supports(difficulty: ExerciseDifficulty): Boolean =
        when (this) {
            BEGINNER -> difficulty == ExerciseDifficulty.BEGINNER
            INTERMEDIATE -> difficulty != ExerciseDifficulty.ADVANCED
            ADVANCED -> true
        }
}
