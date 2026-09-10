package com.jgv.workoutplanner.domain.model

/**
 * How much skill, coordination, or control an exercise demands (spec §5.3).
 *
 * Compared against [ExperienceLevel] by the eligibility engine (spec §8).
 */
enum class ExerciseDifficulty {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED,
}
