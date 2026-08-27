package com.jgv.workoutplanner.domain.model

/**
 * Result of checking one exercise against one profile (README §8).
 *
 * @param exercise the catalog entry that was checked.
 * @param isEligible true when no [ExclusionReason] was found.
 * @param exclusionReasons all reasons that prevent inclusion — empty when [isEligible].
 */
data class ExerciseEligibility(
    val exercise: ExerciseDefinition,
    val isEligible: Boolean,
    val exclusionReasons: Set<ExclusionReason>,
)
