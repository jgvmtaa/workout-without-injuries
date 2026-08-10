package com.jgv.workoutplanner.domain.model

/**
 * How much training experience the user reports (README §4.3).
 *
 * Compared against [ExerciseDifficulty] by the eligibility engine in Phase 4
 * (README §8). §8 leaves open whether experience should exclude an exercise outright
 * or only lower its ranking — that decision belongs to Phase 4, not here.
 */
enum class ExperienceLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED,
}
