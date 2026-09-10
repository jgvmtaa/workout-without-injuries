package com.jgv.workoutplanner.domain.model

/**
 * Result of plan generation (spec §12.4, §25).
 *
 * A plan can be partial: when a template slot has no eligible candidate, the generator
 * emits a warning instead of crashing (spec §12.4 / §25). The caller renders the warning
 * and keeps the valid prefix of the plan.
 *
 * Determinism: same [UserProfile] ⇒ same [plan] and same [warnings] ordering, because
 * candidate ordering and tie-breaks are stable (spec §12.5).
 */
data class WorkoutPlanGenerationResult(
    val plan: WorkoutPlan,
    val warnings: List<PlanWarning>,
    val eligibleExerciseCount: Int,
)

/**
 * An unfillable template slot (spec §25).
 *
 * @param dayIndex zero-based index inside the generated plan
 * @param dayFocus which day template this slot belongs to
 * @param slotId the [TemplateSlot.id] that could not be filled
 * UI copy is deliberately not stored here: the Plan screen resolves the warning to a
 * localized resource. Keeping this model structural also prevents stale English copy
 * from being persisted across app updates.
 */
data class PlanWarning(
    val dayIndex: Int,
    val dayFocus: WorkoutDayFocus,
    val slotId: String,
)
