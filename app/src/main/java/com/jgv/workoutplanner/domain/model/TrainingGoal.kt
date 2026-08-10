package com.jgv.workoutplanner.domain.model

/**
 * What the user is training for (README §4.3).
 *
 * The goal steers rep ranges and exercise emphasis during plan generation (Phase 5);
 * it never affects eligibility — that is limitation- and equipment-driven only.
 */
enum class TrainingGoal {
    GENERAL_FITNESS,
    BUILD_MUSCLE,
    BUILD_STRENGTH,
    IMPROVE_ENDURANCE,
}
