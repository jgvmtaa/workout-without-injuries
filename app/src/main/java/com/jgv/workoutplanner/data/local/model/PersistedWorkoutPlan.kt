package com.jgv.workoutplanner.data.local.model

import kotlinx.serialization.Serializable

/**
 * On-disk shape for the current workout plan (spec §21).
 *
 * Stored separately from [PersistedState] as `workout_plan.json` for independent
 * ownership and schema evolution.
 *
 * Mirrors [com.jgv.workoutplanner.domain.model.WorkoutPlan] but with Strings for enums and
 * explicit rep range fields because `IntRange` is not serializable by default without
 * custom serializer. Tolerant on read: unknown ExerciseId is dropped (partial plan survives).
 *
 * The file holds at most one plan — MVP has no plan history (spec §29).
 */

@Serializable
data class StoredWorkoutPlan(
    val plan: PersistedWorkoutPlan? = null,
    val warnings: List<PersistedPlanWarning> = emptyList(),
    val requiresRegeneration: Boolean = false,
)

@Serializable
data class PersistedPlanWarning(
    val dayIndex: Int,
    val dayFocus: String,
    val slotId: String,
)

@Serializable
data class PersistedWorkoutPlan(
    val id: String,
    val name: String,
    val days: List<PersistedWorkoutDay>,
)

@Serializable
data class PersistedWorkoutDay(
    val id: String,
    val name: String,
    val focus: String,
    val exercises: List<PersistedPlannedExercise>,
)

@Serializable
data class PersistedPlannedExercise(
    val exerciseId: String,
    val sets: Int,
    val repStart: Int,
    val repEnd: Int,
    val restSeconds: Int,
    val order: Int,
)
