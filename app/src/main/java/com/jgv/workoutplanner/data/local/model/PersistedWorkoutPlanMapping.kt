package com.jgv.workoutplanner.data.local.model

import com.jgv.workoutplanner.domain.model.ExerciseId
import com.jgv.workoutplanner.domain.model.PlannedExercise
import com.jgv.workoutplanner.domain.model.WorkoutDay
import com.jgv.workoutplanner.domain.model.WorkoutDayFocus
import com.jgv.workoutplanner.domain.model.WorkoutPlan

// ---------------------------------------------------------------- Domain ← storage (tolerant)

fun StoredWorkoutPlan.toDomain(): WorkoutPlan? = plan?.toDomain()

fun PersistedWorkoutPlan.toDomain(): WorkoutPlan? {
    val mappedDays = days.mapNotNull { it.toDomain() }
    // If every day failed to parse (e.g., all exercises unknown) we still return a plan
    // with whatever survived – an empty day list is still a valid partial result (README §25).
    return WorkoutPlan(
        id = id,
        name = name,
        days = mappedDays,
    )
}

private fun PersistedWorkoutDay.toDomain(): WorkoutDay? {
    val focus = focus.toEnumOrNull<WorkoutDayFocus>() ?: return null
    return WorkoutDay(
        id = id,
        name = name,
        focus = focus,
        exercises = exercises.mapNotNull { it.toDomain() }.sortedBy { it.order },
    )
}

private fun PersistedPlannedExercise.toDomain(): PlannedExercise? {
    val eid = exerciseId.toEnumOrNull<ExerciseId>() ?: return null
    return PlannedExercise(
        exerciseId = eid,
        sets = sets,
        repRange = repStart..repEnd,
        restSeconds = restSeconds,
        order = order,
    )
}

// ---------------------------------------------------------------- Domain → storage (exact, sorted)

fun WorkoutPlan.toPersisted(): PersistedWorkoutPlan = PersistedWorkoutPlan(
    id = id,
    name = name,
    days = days.map { it.toPersisted() },
)

private fun WorkoutDay.toPersisted(): PersistedWorkoutDay = PersistedWorkoutDay(
    id = id,
    name = name,
    focus = focus.name,
    exercises = exercises.sortedBy { it.order }.map { it.toPersisted() },
)

private fun PlannedExercise.toPersisted(): PersistedPlannedExercise = PersistedPlannedExercise(
    exerciseId = exerciseId.name,
    sets = sets,
    repStart = repRange.first,
    repEnd = repRange.last,
    restSeconds = restSeconds,
    order = order,
)

// ---------------------------------------------------------------- Internals

private inline fun <reified T : Enum<T>> String.toEnumOrNull(): T? =
    enumValues<T>().firstOrNull { it.name == this }
