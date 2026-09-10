package com.jgv.workoutplanner.domain.model

/**
 * Why an exercise cannot be included in a plan (spec §8).
 *
 * Kept Android-independent: no Compose or resource references. Copy resolution
 * happens in the UI layer via the catalogs and [com.jgv.workoutplanner.core.ui.labelRes].
 */
sealed interface ExclusionReason {

    /**
     * The exercise demands a movement the user confirmed they need to avoid.
     * This is the safety-relevant reason — the only one that makes an exercise
     * `EXCLUDED` rather than merely `UNAVAILABLE` (spec §9).
     */
    data class ConflictingLimitation(
        val limitation: MovementLimitation,
    ) : ExclusionReason

    /**
     * The exercise needs equipment the user does not have.
     * Logistics, not safety — therefore `UNAVAILABLE` (spec §9).
     */
    data class MissingEquipment(
        val equipment: Equipment,
    ) : ExclusionReason

    /**
     * The exercise's difficulty exceeds what the user's experience supports.
     * Treated as `UNAVAILABLE` under the strict experience matrix in spec §8.
     */
    data class AboveExperienceLevel(
        val requiredLevel: ExerciseDifficulty,
    ) : ExclusionReason
}
