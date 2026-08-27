package com.jgv.workoutplanner.domain.model

/**
 * Three-category classification for the exercise library and plan generation (README §9).
 *
 * - [AVAILABLE]: no conflicts found, can be used.
 * - [EXCLUDED]: conflicts with a confirmed movement limitation — safety, never auto-included.
 * - [UNAVAILABLE]: missing equipment or above experience level — logistics, not safety.
 */
enum class ExerciseAvailability {
    AVAILABLE,
    EXCLUDED,
    UNAVAILABLE,
}

/**
 * Result of categorizing the whole catalog against one profile (README §9).
 *
 * Deterministic: iterates catalog in stable order (ExerciseId.name) and preserves that
 * order within each bucket, so the same profile always yields the same grouping.
 */
data class FilteredExercises(
    val available: List<ExerciseEligibility>,
    val excluded: List<ExerciseEligibility>,
    val unavailable: List<ExerciseEligibility>,
) {
    /** Every eligibility across all buckets, in bucket order. */
    val all: List<ExerciseEligibility> get() = available + excluded + unavailable

    val availableCount: Int get() = available.size
    val excludedCount: Int get() = excluded.size
    val unavailableCount: Int get() = unavailable.size
    val totalCount: Int get() = availableCount + excludedCount + unavailableCount
}

/**
 * Derives [ExerciseAvailability] from an already evaluated [ExerciseEligibility].
 *
 * Priority matters: a limitation conflict outranks missing equipment — if an exercise is
 * both unsafe *and* needs equipment the user lacks, it is EXCLUDED, not UNAVAILABLE,
 * because the safety signal is what must be surfaced (README §2, §9).
 */
fun ExerciseEligibility.availability(): ExerciseAvailability {
    val hasLimitationConflict = exclusionReasons.any { it is ExclusionReason.ConflictingLimitation }
    val hasEquipmentOrLevel = exclusionReasons.any {
        it is ExclusionReason.MissingEquipment || it is ExclusionReason.AboveExperienceLevel
    }
    return when {
        exclusionReasons.isEmpty() -> ExerciseAvailability.AVAILABLE
        hasLimitationConflict -> ExerciseAvailability.EXCLUDED
        hasEquipmentOrLevel -> ExerciseAvailability.UNAVAILABLE
        else -> ExerciseAvailability.AVAILABLE // defensive, unreachable because isEmpty handled
    }
}
