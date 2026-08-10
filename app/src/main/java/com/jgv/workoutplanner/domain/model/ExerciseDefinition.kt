package com.jgv.workoutplanner.domain.model

import androidx.annotation.StringRes

/**
 * One exercise in the static catalog (README §5.3).
 *
 * Definitions are immutable reference data — a plan stores an [ExerciseId] and looks
 * the definition back up, so editing the catalog never invalidates a saved plan
 * (README §11).
 *
 * [conflictingLimitations] is the field that does the safety work: it lists the
 * movements this exercise demands, so the filter can exclude it whenever the user has
 * confirmed one of them (README §8). Getting it wrong in either direction is a real
 * cost — too broad and the user is left with nothing to train, too narrow and the app
 * suggests something they told us to avoid. It describes the *standard* execution of
 * the movement; regressions and variations are separate entries.
 *
 * @param requiredEquipment everything needed to perform it. Missing equipment makes the
 *   exercise `Unavailable`, not `Excluded` (README §9).
 */
data class ExerciseDefinition(
    val id: ExerciseId,
    @StringRes val nameRes: Int,
    @StringRes val descriptionRes: Int,
    val primaryMuscle: MuscleGroup,
    val secondaryMuscles: Set<MuscleGroup>,
    val movementPattern: MovementPattern,
    val requiredEquipment: Set<Equipment>,
    val difficulty: ExerciseDifficulty,
    val conflictingLimitations: Set<MovementLimitation>,
    val tags: Set<ExerciseTag>,
    val defaultPrescription: ExercisePrescription,
)
