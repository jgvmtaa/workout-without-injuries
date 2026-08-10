package com.jgv.workoutplanner.domain.usecase

import com.jgv.workoutplanner.domain.model.TrainingOptions
import com.jgv.workoutplanner.domain.model.WorkoutSplit
import javax.inject.Inject

/**
 * Chooses the workout split from the weekly training frequency (README §4.3, §12.1).
 *
 * ## Why this lives in Phase 3
 * [com.jgv.workoutplanner.domain.model.UserProfile] requires a `preferredSplit`, and
 * the profile is assembled at the end of onboarding — so the mapping is needed before
 * plan generation exists. `WorkoutSplit`'s KDoc said it would arrive with Phase 5; it
 * arrives here instead, and Phase 5 calls this rather than repeating it.
 *
 * ## Why the user is not asked
 * README §4.3 lists a preferred split among the collected preferences but permits the
 * first implementation to derive it from frequency instead, and that is what happens:
 * the derived split is shown read-only on the review screen. One fewer question, and no
 * way to pick a split that contradicts the chosen frequency.
 *
 * ## The three-day case
 * README §4.3 allows either full body or push/pull/legs at three days; §12.1's
 * `determineSplit` returns push/pull/legs. This follows §12.1, which is the version
 * written as code and the one Phase 5 templates are described against.
 */
class DetermineWorkoutSplitUseCase @Inject constructor() {

    /**
     * The split for [daysPerWeek].
     *
     * Frequencies outside [TrainingOptions.DAYS_PER_WEEK] cannot be produced by the
     * preferences screen, but a persisted profile written by an older build could still
     * carry one. Those fall through to the same branch as five days rather than
     * failing — a plan built on a reasonable default beats a crash (README §24.4).
     */
    operator fun invoke(daysPerWeek: Int): WorkoutSplit = when (daysPerWeek) {
        2 -> WorkoutSplit.FULL_BODY
        3 -> WorkoutSplit.PUSH_PULL_LEGS
        4 -> WorkoutSplit.UPPER_LOWER
        else -> WorkoutSplit.UPPER_LOWER
    }
}
