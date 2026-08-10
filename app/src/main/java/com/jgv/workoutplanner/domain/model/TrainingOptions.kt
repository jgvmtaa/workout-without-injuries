package com.jgv.workoutplanner.domain.model

/**
 * The numeric choices the preferences screen offers (README §4.3).
 *
 * Kept in the domain rather than in the screen because they are constraints, not
 * layout: [DAYS_PER_WEEK] is the input domain of
 * [com.jgv.workoutplanner.domain.usecase.DetermineWorkoutSplitUseCase], and a plan
 * generated for a frequency outside this range has no split to build from.
 */
object TrainingOptions {

    /** Training days per week the MVP supports (README §4.3). */
    val DAYS_PER_WEEK: List<Int> = listOf(2, 3, 4, 5)

    /** Approximate session lengths, in minutes (README §4.3). */
    val SESSION_DURATION_MINUTES: List<Int> = listOf(30, 45, 60, 75)

    /**
     * Equipment offered on the preferences screen.
     *
     * [Equipment.CARDIO_MACHINE] is deliberately absent: the MVP catalog is
     * strength-only, so selecting it would change nothing. The enum constant stays for
     * when conditioning work exists (see docs/follow-ups.md).
     *
     * [Equipment.BODYWEIGHT] is absent too, for the opposite reason — it is always
     * available and cannot be deselected, so it is never a choice (README §25).
     */
    val SELECTABLE_EQUIPMENT: List<Equipment> = Equipment.entries.filterNot {
        it == Equipment.BODYWEIGHT || it == Equipment.CARDIO_MACHINE
    }
}
