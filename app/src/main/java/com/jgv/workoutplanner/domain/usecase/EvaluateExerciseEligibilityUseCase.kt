package com.jgv.workoutplanner.domain.usecase

import com.jgv.workoutplanner.domain.model.ExclusionReason
import com.jgv.workoutplanner.domain.model.ExerciseDefinition
import com.jgv.workoutplanner.domain.model.ExerciseEligibility
import com.jgv.workoutplanner.domain.model.UserProfile
import javax.inject.Inject

/**
 * Determines whether an exercise can be included for a given profile (spec §8).
 *
 * Android-independent by design — no Compose, Context, or @StringRes here. All
 * exclusion reasons are explainable via the returned [ExerciseEligibility].
 *
 * Deterministic: same (exercise, profile) ⇒ same result every time.
 */
class EvaluateExerciseEligibilityUseCase @Inject constructor() {

    operator fun invoke(
        exercise: ExerciseDefinition,
        profile: UserProfile,
    ): ExerciseEligibility {
        val reasons = buildSet {
            // Each conflicting limitation the user confirmed.
            exercise.conflictingLimitations
                .intersect(profile.movementLimitations)
                .forEach { limitation ->
                    add(ExclusionReason.ConflictingLimitation(limitation))
                }

            // Each piece of equipment the exercise needs that the user lacks.
            exercise.requiredEquipment
                .filterNot(profile.availableEquipment::contains)
                .forEach { equipment ->
                    add(ExclusionReason.MissingEquipment(equipment))
                }

            // Experience check — see ExperienceLevel.supports for the rule.
            if (!profile.experienceLevel.supports(exercise.difficulty)) {
                add(ExclusionReason.AboveExperienceLevel(exercise.difficulty))
            }
        }

        return ExerciseEligibility(
            exercise = exercise,
            isEligible = reasons.isEmpty(),
            exclusionReasons = reasons,
        )
    }
}
