package com.jgv.workoutplanner.domain.usecase

import com.jgv.workoutplanner.domain.model.ExerciseEligibility
import com.jgv.workoutplanner.domain.model.FilteredExercises
import com.jgv.workoutplanner.domain.model.UserProfile
import com.jgv.workoutplanner.domain.model.availability
import com.jgv.workoutplanner.domain.repository.ExerciseRepository
import javax.inject.Inject

/**
 * Categorizes every exercise in the catalog for a profile (README §9, §14).
 *
 * Deterministic (README §12.5, §27 Phase 4): same profile ⇒ same grouping, because
 * the catalog order is fixed and each eligibility check is pure. Ties broken by
 * ExerciseId.name inside [FilteredExercises] construction.
 *
 * Excluded exercises remain inspectable but are never auto-included by the planner
 * (Phase 5 respects this).
 */
class GetEligibleExercisesUseCase @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    private val evaluateUseCase: EvaluateExerciseEligibilityUseCase,
) {

    operator fun invoke(profile: UserProfile): FilteredExercises {
        // Stable order: catalog order is already deterministic, but sorting by id name
        // removes any future risk if catalog order changes for readability.
        val allEligibilities: List<ExerciseEligibility> =
            exerciseRepository.getAllExercises()
                .sortedBy { it.id.name }
                .map { exercise -> evaluateUseCase(exercise, profile) }

        val available = mutableListOf<ExerciseEligibility>()
        val excluded = mutableListOf<ExerciseEligibility>()
        val unavailable = mutableListOf<ExerciseEligibility>()

        for (eligibility in allEligibilities) {
            when (eligibility.availability()) {
                com.jgv.workoutplanner.domain.model.ExerciseAvailability.AVAILABLE -> available.add(eligibility)
                com.jgv.workoutplanner.domain.model.ExerciseAvailability.EXCLUDED -> excluded.add(eligibility)
                com.jgv.workoutplanner.domain.model.ExerciseAvailability.UNAVAILABLE -> unavailable.add(eligibility)
            }
        }

        return FilteredExercises(
            available = available,
            excluded = excluded,
            unavailable = unavailable,
        )
    }
}
