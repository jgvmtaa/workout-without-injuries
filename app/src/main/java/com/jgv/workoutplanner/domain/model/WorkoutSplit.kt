package com.jgv.workoutplanner.domain.model

/**
 * How training volume is divided across the week (README §4.3).
 *
 * Only some split/frequency combinations make sense (README §4.3):
 * 2 days full body, 3 days full body or push/pull/legs, 4 days upper/lower,
 * 5 days upper/lower plus an optional full-body day. The MVP derives the split from
 * the weekly frequency instead of asking (README §12.1), so the mapping lives with
 * plan generation in Phase 5.
 */
enum class WorkoutSplit {
    FULL_BODY,
    UPPER_LOWER,
    PUSH_PULL_LEGS,
}
