package com.jgv.workoutplanner.domain.model

/**
 * Descriptive traits used for ranking and substitution (spec §5.3).
 *
 * Tags never exclude an exercise on their own — only [MovementLimitation]s and missing
 * [Equipment] do that (spec §9). They feed the deterministic generation score: a
 * [MACHINE_SUPPORTED] exercise outranks a free-weight one when the user has balance
 * limitations, [COMPOUND] work is preferred early in a session, and a second
 * [UNILATERAL] exercise in the same day is penalised (spec §12.5).
 */
enum class ExerciseTag {
    COMPOUND,
    ISOLATION,
    UNILATERAL,
    BILATERAL,
    MACHINE_SUPPORTED,
    CHEST_SUPPORTED,
    REQUIRES_BALANCE,
    HIGH_IMPACT,
    BODYWEIGHT,
    FREE_WEIGHT,
}
