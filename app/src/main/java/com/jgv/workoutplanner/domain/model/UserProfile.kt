package com.jgv.workoutplanner.domain.model

/**
 * Everything the app knows about the user (README §10).
 *
 * The whole profile is one immutable value: plan generation takes it as input and must
 * return the same plan for the same profile every time (README §12.5). Collections are
 * `Set`s so ordering can never leak into that result.
 *
 * [movementLimitations] holds only what the user explicitly confirmed on the
 * limitations screen. It is not derived from [selectedInjuries] at read time — the
 * confirmation *is* the data (README §2).
 */
data class UserProfile(
    val goal: TrainingGoal,
    val experienceLevel: ExperienceLevel,
    val daysPerWeek: Int,
    val sessionDurationMinutes: Int,
    val preferredSplit: WorkoutSplit,
    val availableEquipment: Set<Equipment>,
    val selectedInjuries: Set<SelectedInjury>,
    val movementLimitations: Set<MovementLimitation>,
    val hasAcceptedSafetyNotice: Boolean,
)
