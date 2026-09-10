package com.jgv.workoutplanner.domain.usecase

import com.jgv.workoutplanner.domain.model.TrainingOptions
import com.jgv.workoutplanner.domain.model.WorkoutSplit
import javax.inject.Inject

/**
 * Chooses the workout split from weekly training frequency (spec §4.3, §12.1).
 * The result is displayed during onboarding and stored in the completed profile.
 */
class DetermineWorkoutSplitUseCase @Inject constructor() {

    /**
     * The split for [daysPerWeek].
     *
     * Frequencies outside [TrainingOptions.DAYS_PER_WEEK] cannot be produced by the
     * preferences screen, but a persisted profile written by an older build could still
     * carry one. Those fall through to the same branch as five days rather than
     * failing — a plan built on a reasonable default beats a crash (spec §24.4).
     */
    operator fun invoke(daysPerWeek: Int): WorkoutSplit = when (daysPerWeek) {
        2 -> WorkoutSplit.FULL_BODY
        3 -> WorkoutSplit.PUSH_PULL_LEGS
        4 -> WorkoutSplit.UPPER_LOWER
        else -> WorkoutSplit.UPPER_LOWER
    }
}
