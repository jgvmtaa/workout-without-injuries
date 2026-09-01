package com.jgv.workoutplanner.domain.repository

import com.jgv.workoutplanner.domain.model.PlanWarning
import com.jgv.workoutplanner.domain.model.WorkoutPlan
import kotlinx.coroutines.flow.Flow

/**
 * Storage for the current workout plan (README §19, §21).
 *
 * Interface only in Phase 2; implemented in Phase 3 with the rest of persistence.
 *
 * One plan at a time — the MVP has no plan history (README §29). Every edit in Phase 6
 * writes the whole plan back, which keeps editing trivially consistent at the cost of
 * rewriting more than changed. Fine for a handful of days of exercises.
 *
 * Generation may produce [PlanWarning]s (README §25) for unfillable slots. Those warnings
 * are persisted alongside the plan so they survive process death (they were previously
 * only in-memory in PlanViewModel).
 */
interface WorkoutPlanRepository {

    /** The stored plan, re-emitting on every change. `null` before one is generated. */
    val currentPlan: Flow<WorkoutPlan?>

    /** The warnings from the last generation, persisted alongside the plan. */
    val currentWarnings: Flow<List<PlanWarning>>

    /** Writes [plan] and its generation [warnings], replacing anything stored. */
    suspend fun savePlan(plan: WorkoutPlan, warnings: List<PlanWarning> = emptyList())

    /** Removes the stored plan and its warnings. */
    suspend fun clearPlan()
}
