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
 * [WorkoutPlanRepository] backed by [WorkoutPlanDataStore] (README §19, §21, §25, Phase 6).
 *
 * One plan at a time – MVP has no history (README §29).
 *
 * Manual edits use [updatePlanAtomically] which runs inside DataStore.updateData transaction.
 * Commit replaces only WorkoutPlan; warnings and requiresRegeneration are preserved internally.
 *
 * Only [saveGeneratedPlan] may clear requiresRegeneration, and only together with persisting
 * new plan+warnings.
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

    override val requiresRegeneration: Flow<Boolean> =
        dataStore.state
            .map { it.requiresRegeneration }
            .distinctUntilChanged()

    override suspend fun <T> updatePlanAtomically(
        transform: (WorkoutPlanRepository.WorkoutPlanSnapshot) -> WorkoutPlanRepository.AtomicPlanMutation<T>,
    ): T {
        var resultToReturn: T? = null
        var resolved = false

        dataStore.update { stored ->
            val snapshot = WorkoutPlanRepository.WorkoutPlanSnapshot(
                plan = stored.toDomain(),
                requiresRegeneration = stored.requiresRegeneration,
            )
            when (val mutation = transform(snapshot)) {
                is WorkoutPlanRepository.AtomicPlanMutation.Commit -> {
                    @Suppress("UNCHECKED_CAST")
                    resultToReturn = mutation.result as T
                    resolved = true
                    // Preserve warnings and requiresRegeneration
                    stored.copy(
                        plan = mutation.plan.toPersisted(),
                    )
                }
                is WorkoutPlanRepository.AtomicPlanMutation.Reject -> {
                    @Suppress("UNCHECKED_CAST")
                    resultToReturn = mutation.result as T
                    resolved = true
                    stored // no write
                }
            }
        }
        check(resolved) { "transform must return Commit or Reject" }
        @Suppress("UNCHECKED_CAST")
        return resultToReturn as T
    }

    override suspend fun saveGeneratedPlan(plan: WorkoutPlan, warnings: List<PlanWarning>) {
        dataStore.update { _ ->
            StoredWorkoutPlan(
                plan = plan.toPersisted(),
                warnings = warnings.map { it.toPersisted() },
                requiresRegeneration = false,
            )
        }
    }

    override suspend fun markRequiresRegeneration(): Boolean {
        var hadPlan = false
        dataStore.update { stored ->
            if (stored.plan == null) {
                hadPlan = false
                stored
            } else {
                hadPlan = true
                if (stored.requiresRegeneration) stored else stored.copy(requiresRegeneration = true)
            }
        }
        return hadPlan
    }

    override suspend fun clearPlan() {
        dataStore.update { _ ->
            StoredWorkoutPlan(plan = null, warnings = emptyList(), requiresRegeneration = false)
        }
    }
}
