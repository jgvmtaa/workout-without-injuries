package com.jgv.workoutplanner.domain.usecase

import com.jgv.workoutplanner.domain.model.ExerciseDefinition
import com.jgv.workoutplanner.domain.model.ExerciseId
import com.jgv.workoutplanner.domain.model.UserProfile
import com.jgv.workoutplanner.domain.model.WorkoutDayFocus
import com.jgv.workoutplanner.domain.model.WorkoutPlanTemplate
import javax.inject.Inject

/**
 * Candidates for adding exercises to a specific workout day (spec §13).
 *
 * - Start from profile-eligible.
 * - Keep exercises whose movement pattern occurs in at least one template slot for dayFocus.
 * - Exclude exercises already in that day.
 * - Sort deterministically by ExerciseId.name.
 */
class GetExercisesForWorkoutDayUseCase @Inject constructor(
    private val getEligibleExercisesUseCase: GetEligibleExercisesUseCase,
) {

    operator fun invoke(
        profile: UserProfile,
        dayFocus: WorkoutDayFocus,
        excludedExerciseIds: Set<ExerciseId>,
    ): List<ExerciseDefinition> {
        val allowedPatterns = WorkoutPlanTemplate.slotsFor(dayFocus)
            .flatMap { it.allowedPatterns }
            .toSet()

        return getEligibleExercisesUseCase(profile).available
            .map { it.exercise }
            .filter { def ->
                def.movementPattern in allowedPatterns && def.id !in excludedExerciseIds
            }
            .sortedBy { it.id.name }
    }
}
