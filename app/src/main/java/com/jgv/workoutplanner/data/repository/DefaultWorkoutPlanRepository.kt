package com.jgv.workoutplanner.data.repository

import com.jgv.workoutplanner.data.local.WorkoutPlanDataStore
import com.jgv.workoutplanner.data.local.model.StoredWorkoutPlan
import com.jgv.workoutplanner.data.local.model.toDomain
import com.jgv.workoutplanner.data.local.model.toDomainWarnings
import com.jgv.workoutplanner.data.local.model.toPersisted
import com.jgv.workoutplanner.domain.model.PlanWarning
import com.jgv.workoutplanner.domain.model.WorkoutPlan
import com.jgv.workoutplanner.domain.repository.WorkoutPlanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [WorkoutPlanRepository] backed by [WorkoutPlanDataStore] (README §19, §21, task 5.7).
 *
 * One plan at a time – MVP has no history (README §29). Stores the whole plan; Phase 6
 * edits rewrite the whole record, which is fine for a few dozen exercises.
 *
 * Also persists generation warnings (README §25) alongside the plan so they survive
 * process death – previously they lived only in PlanViewModel's in-memory flow.
 */
@Singleton
class DefaultWorkoutPlanRepository @Inject constructor(
    private val dataStore: WorkoutPlanDataStore,
) : WorkoutPlanRepository {

    override val currentPlan: Flow<WorkoutPlan?> =
        dataStore.state
            .map { it.toDomain() }
            .distinctUntilChanged()

    override val currentWarnings: Flow<List<PlanWarning>> =
        dataStore.state
            .map { it.toDomainWarnings() }
            .distinctUntilChanged()

    override suspend fun savePlan(plan: WorkoutPlan, warnings: List<PlanWarning>) {
        dataStore.update { _ ->
            StoredWorkoutPlan(
                plan = plan.toPersisted(),
                warnings = warnings.map { it.toPersisted() },
            )
        }
    }

    override suspend fun clearPlan() {
        dataStore.update { _ ->
            StoredWorkoutPlan(plan = null, warnings = emptyList())
        }
    }
}
