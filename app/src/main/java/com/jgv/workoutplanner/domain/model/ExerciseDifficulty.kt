package com.jgv.workoutplanner.domain.model

/**
 * How much skill, coordination, or control an exercise demands (README §5.3).
 *
 * Compared against [ExperienceLevel] in Phase 4 (README §8).
 */
enum class ExerciseDifficulty {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED,
}
