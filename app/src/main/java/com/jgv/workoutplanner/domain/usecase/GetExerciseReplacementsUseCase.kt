package com.jgv.workoutplanner.domain.usecase

import com.jgv.workoutplanner.domain.model.ExerciseDefinition
import com.jgv.workoutplanner.domain.model.ExerciseId
import com.jgv.workoutplanner.domain.model.UserProfile
import com.jgv.workoutplanner.domain.repository.ExerciseRepository
import javax.inject.Inject

/**
 * Deterministic replacement candidates (Phase 6 §6.3).
 *
 * Starts from profile-eligible, excludes current + day duplicates,
 * ranks in tiers, tie-break ExerciseId.name.
 */
class GetExerciseReplacementsUseCase @Inject constructor(
    private val getEligibleExercisesUseCase: GetEligibleExercisesUseCase,
    private val exerciseRepository: ExerciseRepository,
) {

    operator fun invoke(
        currentExerciseId: ExerciseId,
        profile: UserProfile,
        excludedExerciseIds: Set<ExerciseId>,
    ): List<ExerciseDefinition> {
        val currentDef = exerciseRepository.getAllExercises().firstOrNull { it.id == currentExerciseId }
            ?: return emptyList()

        val available = getEligibleExercisesUseCase(profile).available.map { it.exercise }

        val candidates = available.filter { def ->
            def.id != currentExerciseId && def.id !in excludedExerciseIds
        }

        return candidates.sortedWith(
            compareBy<ExerciseDefinition> { def ->
                when {
                    def.movementPattern == currentDef.movementPattern &&
                        def.primaryMuscle == currentDef.primaryMuscle -> 0
                    def.movementPattern == currentDef.movementPattern -> 1
                    def.primaryMuscle == currentDef.primaryMuscle -> 2
                    else -> 3
                }
            }.thenBy { it.id.name },
        )
    }
}
