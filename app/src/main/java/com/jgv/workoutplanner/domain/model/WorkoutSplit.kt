package com.jgv.workoutplanner.domain.model

/**
 * How training volume is divided across the week (spec §4.3).
 *
 * The app derives the split from weekly frequency rather than asking the user. The
 * exact mapping lives in
 * [com.jgv.workoutplanner.domain.usecase.DetermineWorkoutSplitUseCase] and spec §12.1.
 */
enum class WorkoutSplit {
    FULL_BODY,
    UPPER_LOWER,
    PUSH_PULL_LEGS,
}
