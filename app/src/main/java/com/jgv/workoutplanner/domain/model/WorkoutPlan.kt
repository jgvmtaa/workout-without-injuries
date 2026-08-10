package com.jgv.workoutplanner.domain.model

/**
 * The user's current workout plan (README §11).
 *
 * A plan references exercises by [ExerciseId] and stores only what the user can change
 * — sets, reps, rest, order. Names, descriptions, and safety data stay in the catalog,
 * so a catalog correction reaches every saved plan instead of leaving stale copies
 * behind.
 *
 * The MVP keeps exactly one plan at a time (README §29 defers history).
 */
data class WorkoutPlan(
    val id: String,
    val name: String,
    val days: List<WorkoutDay>,
)

/** One training session within a [WorkoutPlan] (README §11). */
data class WorkoutDay(
    val id: String,
    val name: String,
    val focus: WorkoutDayFocus,
    val exercises: List<PlannedExercise>,
)

/**
 * One exercise as scheduled in a [WorkoutDay] (README §11).
 *
 * [order] is stored rather than inferred from list position so reordering is an
 * explicit edit (README §13) and survives serialisation unambiguously.
 */
data class PlannedExercise(
    val exerciseId: ExerciseId,
    val sets: Int,
    val repRange: IntRange,
    val restSeconds: Int,
    val order: Int,
)

/**
 * What a single day trains (README §11).
 *
 * Derived from the [WorkoutSplit]: full body produces [FULL_BODY] days, upper/lower
 * produces [UPPER_BODY] and [LOWER_BODY], push/pull/legs produces [PUSH], [PULL], and
 * [LEGS]. Each focus maps to a template of movement-pattern slots (README §12.4).
 */
enum class WorkoutDayFocus {
    FULL_BODY,
    UPPER_BODY,
    LOWER_BODY,
    PUSH,
    PULL,
    LEGS,
}
