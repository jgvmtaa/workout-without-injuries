package com.jgv.workoutplanner.domain.model

/**
 * How much training experience the user reports (README §4.3).
 *
 * Compared against [ExerciseDifficulty] by the eligibility engine in Phase 4
 * (README §8). §8 leaves open whether experience should exclude an exercise outright
 * or only lower its ranking — Phase 4 uses strict exclusion for determinism; ranking
 * may reconsider this in Phase 5.
 */
enum class ExperienceLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED;

    /**
     * Whether this experience level can perform an exercise of [difficulty] (README §8).
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
