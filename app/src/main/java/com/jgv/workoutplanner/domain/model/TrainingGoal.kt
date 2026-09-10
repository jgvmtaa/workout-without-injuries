package com.jgv.workoutplanner.domain.model

/**
 * What the user is training for (spec §4.3).
 *
 * The current generator stores this choice in the profile but does not use it to alter
 * exercise selection or prescriptions (spec §5.3, §12).
 */
enum class TrainingGoal {
    GENERAL_FITNESS,
    BUILD_MUSCLE,
    BUILD_STRENGTH,
    IMPROVE_ENDURANCE,
}
