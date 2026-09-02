package com.jgv.workoutplanner.domain.repository

import com.jgv.workoutplanner.domain.model.PlanWarning
import com.jgv.workoutplanner.domain.model.WorkoutPlan
import kotlinx.coroutines.flow.Flow

/**
 * Storage for the current workout plan (README §19, §21, §25).
 *
 * One plan at a time — the MVP has no plan history (README §29).
 * Phase 6 owns manual edits through an atomic mutation contract that can only replace
 * the domain WorkoutPlan. Warnings and requiresRegeneration are preserved internally
 * by the implementation and cannot be written by callers of the manual-edit path.
 *
 * Only [saveGeneratedPlan] is allowed to clear [requiresRegeneration]; it does so only
 * when the new plan+ warnings are persisted together.
 */
interface WorkoutPlanRepository {

    /** The stored plan, re-emitting on every change. `null` before one is generated. */
    val currentPlan: Flow<WorkoutPlan?>

    /** The warnings from the last generation, persisted alongside the plan. */
    val currentWarnings: Flow<List<PlanWarning>>

    /** Whether the existing plan is outdated due to a material profile change. */
    val requiresRegeneration: Flow<Boolean>

    // ----------------------------------------------------------------
    // Atomic manual-edit contract (Phase 6 §6.1)

    data class WorkoutPlanSnapshot(
        val plan: WorkoutPlan?,
        val requiresRegeneration: Boolean,
    )

    sealed interface AtomicPlanMutation<out T> {
        data class Commit<T>(val plan: WorkoutPlan, val result: T) : AtomicPlanMutation<T>
        data class Reject<T>(val result: T) : AtomicPlanMutation<T>
    }

    /**
     * Atomically transforms the stored plan.
     * A Commit replaces only the WorkoutPlan inside StoredWorkoutPlan;
     * a Reject writes nothing. Warnings and requiresRegeneration are preserved by impl.
     */
    suspend fun <T> updatePlanAtomically(
        transform: (WorkoutPlanSnapshot) -> AtomicPlanMutation<T>,
    ): T

    // ----------------------------------------------------------------
    // Generation / invalidation

    /** Persists a freshly generated plan + warnings and clears outdated flag together. */
    suspend fun saveGeneratedPlan(plan: WorkoutPlan, warnings: List<PlanWarning>)

    /**
     * Atomically marks existing plan as needing regeneration.
     * Returns true iff a plan existed. Never clears the flag.
     */
    suspend fun markRequiresRegeneration(): Boolean

    /** Removes stored plan, warnings, and outdated flag. */
    suspend fun clearPlan()
}
