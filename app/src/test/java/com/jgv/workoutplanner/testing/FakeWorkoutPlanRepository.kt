package com.jgv.workoutplanner.testing

import com.jgv.workoutplanner.domain.model.PlanWarning
import com.jgv.workoutplanner.domain.model.WorkoutPlan
import com.jgv.workoutplanner.domain.repository.WorkoutPlanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class FakeWorkoutPlanRepository(
    initialPlan: WorkoutPlan? = null,
    initialWarnings: List<PlanWarning> = emptyList(),
    initialRequiresRegeneration: Boolean = false,
    private val operationLog: MutableList<String>? = null,
    private val failOnSaveGeneratedPlan: Boolean = false,
) : WorkoutPlanRepository {

    data class Stored(
        val plan: WorkoutPlan? = null,
        val warnings: List<PlanWarning> = emptyList(),
        val requiresRegeneration: Boolean = false,
    )

    private val mutex = Mutex()
    private var stored = Stored(initialPlan, initialWarnings, initialRequiresRegeneration)

    private val _planFlow = MutableStateFlow(initialPlan)
    private val _warningsFlow = MutableStateFlow(initialWarnings)
    private val _requiresFlow = MutableStateFlow(initialRequiresRegeneration)

    override val currentPlan: Flow<WorkoutPlan?> get() = _planFlow.asStateFlow()
    override val currentWarnings: Flow<List<PlanWarning>> get() = _warningsFlow.asStateFlow()
    override val requiresRegeneration: Flow<Boolean> get() = _requiresFlow.asStateFlow()

    val currentStored: Stored get() = stored

    private fun sync() {
        _planFlow.value = stored.plan
        _warningsFlow.value = stored.warnings
        _requiresFlow.value = stored.requiresRegeneration
    }

    override suspend fun <T> updatePlanAtomically(
        transform: (WorkoutPlanRepository.WorkoutPlanSnapshot) -> WorkoutPlanRepository.AtomicPlanMutation<T>,
    ): T = mutex.withLock {
        val snapshot = WorkoutPlanRepository.WorkoutPlanSnapshot(
            plan = stored.plan,
            requiresRegeneration = stored.requiresRegeneration,
        )
        when (val mutation = transform(snapshot)) {
            is WorkoutPlanRepository.AtomicPlanMutation.Commit -> {
                stored = stored.copy(plan = mutation.plan)
                sync()
                mutation.result
            }
            is WorkoutPlanRepository.AtomicPlanMutation.Reject -> {
                mutation.result
            }
        }
    }

    override suspend fun saveGeneratedPlan(plan: WorkoutPlan, warnings: List<PlanWarning>) = mutex.withLock {
        if (failOnSaveGeneratedPlan) error("Simulated persistence failure")
        stored = Stored(plan = plan, warnings = warnings, requiresRegeneration = false)
        sync()
    }

    override suspend fun markRequiresRegeneration(): Boolean = mutex.withLock {
        operationLog?.add("markRequiresRegeneration")
        if (stored.plan == null) {
            false
        } else {
            if (!stored.requiresRegeneration) {
                stored = stored.copy(requiresRegeneration = true)
                sync()
            }
            true
        }
    }

    override suspend fun clearPlan() {
        mutex.withLock {
            stored = Stored(plan = null, warnings = emptyList(), requiresRegeneration = false)
            sync()
        }
    }
}
