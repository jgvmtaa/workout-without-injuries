package com.jgv.workoutplanner.domain.usecase

import com.jgv.workoutplanner.domain.model.ExerciseId
import com.jgv.workoutplanner.domain.model.PlannedExercise
import com.jgv.workoutplanner.domain.model.WorkoutDay
import com.jgv.workoutplanner.domain.model.WorkoutExerciseEdit
import com.jgv.workoutplanner.domain.model.WorkoutExerciseEditResult
import com.jgv.workoutplanner.domain.model.WorkoutPlan
import com.jgv.workoutplanner.domain.model.WorkoutPlanTemplate
import com.jgv.workoutplanner.domain.repository.ExerciseRepository
import com.jgv.workoutplanner.domain.repository.ProfileRepository
import com.jgv.workoutplanner.domain.repository.WorkoutPlanRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * Single entry point for manual plan mutations (spec §13).
 *
 * - Reads latest profile immediately before atomic transaction.
 * - Inside transaction checks snapshot.requiresRegeneration first -> PlanOutdated.
 * - Validates filters, capacity, duplicates, order normalization.
 * - Preserves warnings and invalidation metadata — handled by repository impl.
 */
class UpdateWorkoutExerciseUseCase @Inject constructor(
    private val workoutPlanRepository: WorkoutPlanRepository,
    private val profileRepository: ProfileRepository,
    private val getEligibleExercisesUseCase: GetEligibleExercisesUseCase,
    private val exerciseRepository: ExerciseRepository,
) {

    suspend operator fun invoke(edit: WorkoutExerciseEdit): WorkoutExerciseEditResult {
        val profile = profileRepository.profile.first()

        return workoutPlanRepository.updatePlanAtomically { snapshot ->
            // 6.1: check outdated first, before any existence checks
            if (snapshot.requiresRegeneration) {
                return@updatePlanAtomically WorkoutPlanRepository.AtomicPlanMutation.Reject(
                    WorkoutExerciseEditResult.PlanOutdated,
                )
            }

            val currentPlan = snapshot.plan
            if (currentPlan == null) {
                return@updatePlanAtomically WorkoutPlanRepository.AtomicPlanMutation.Reject(
                    WorkoutExerciseEditResult.PlanNotFound,
                )
            }

            when (edit) {
                is WorkoutExerciseEdit.Remove -> handleRemove(currentPlan, edit)
                is WorkoutExerciseEdit.Replace -> handleReplace(currentPlan, edit, profile)
                is WorkoutExerciseEdit.Move -> handleMove(currentPlan, edit)
                is WorkoutExerciseEdit.AddExercises -> handleAdd(currentPlan, edit, profile)
            }
        }
    }

    private fun handleRemove(
        plan: WorkoutPlan,
        edit: WorkoutExerciseEdit.Remove,
    ): WorkoutPlanRepository.AtomicPlanMutation<WorkoutExerciseEditResult> {
        val day = plan.days.firstOrNull { it.id == edit.workoutDayId }
            ?: return WorkoutPlanRepository.AtomicPlanMutation.Reject(WorkoutExerciseEditResult.DayNotFound)

        if (day.exercises.none { it.exerciseId == edit.exerciseId }) {
            return WorkoutPlanRepository.AtomicPlanMutation.Reject(WorkoutExerciseEditResult.ExerciseNotFound)
        }

        val remaining = day.exercises.filterNot { it.exerciseId == edit.exerciseId }
        val normalized = remaining.mapIndexed { idx, pe -> pe.copy(order = idx) }

        val newDay = day.copy(exercises = normalized)
        val newPlan = plan.replaceDay(newDay)
        return WorkoutPlanRepository.AtomicPlanMutation.Commit(newPlan, WorkoutExerciseEditResult.Updated)
    }

    private fun handleReplace(
        plan: WorkoutPlan,
        edit: WorkoutExerciseEdit.Replace,
        profile: com.jgv.workoutplanner.domain.model.UserProfile?,
    ): WorkoutPlanRepository.AtomicPlanMutation<WorkoutExerciseEditResult> {
        if (profile == null) {
            return WorkoutPlanRepository.AtomicPlanMutation.Reject(WorkoutExerciseEditResult.PlanNotFound)
        }

        val day = plan.days.firstOrNull { it.id == edit.workoutDayId }
            ?: return WorkoutPlanRepository.AtomicPlanMutation.Reject(WorkoutExerciseEditResult.DayNotFound)

        val currentIdx = day.exercises.indexOfFirst { it.exerciseId == edit.currentExerciseId }
        if (currentIdx == -1) {
            return WorkoutPlanRepository.AtomicPlanMutation.Reject(WorkoutExerciseEditResult.ExerciseNotFound)
        }

        if (edit.currentExerciseId == edit.replacementExerciseId) {
            return WorkoutPlanRepository.AtomicPlanMutation.Reject(WorkoutExerciseEditResult.Unchanged)
        }

        // Duplicate check within day (excluding current being replaced)
        if (day.exercises.any { it.exerciseId == edit.replacementExerciseId }) {
            return WorkoutPlanRepository.AtomicPlanMutation.Reject(WorkoutExerciseEditResult.DuplicateExercise)
        }

        // Revalidate eligibility against current profile filters
        val eligibleIds = getEligibleExercisesUseCase(profile).available.map { it.exercise.id }.toSet()
        if (edit.replacementExerciseId !in eligibleIds) {
            return WorkoutPlanRepository.AtomicPlanMutation.Reject(WorkoutExerciseEditResult.IneligibleExercise)
        }

        val definition = exerciseRepository.getAllExercises().firstOrNull { it.id == edit.replacementExerciseId }
            ?: return WorkoutPlanRepository.AtomicPlanMutation.Reject(WorkoutExerciseEditResult.IneligibleExercise)

        // Build replacement prescription from catalog defaults, retaining existing position
        val existing = day.exercises[currentIdx]
        val newPlanned = PlannedExercise(
            exerciseId = definition.id,
            sets = definition.defaultPrescription.sets.first,
            repRange = definition.defaultPrescription.reps,
            restSeconds = definition.defaultPrescription.restSeconds,
            order = existing.order,
        )

        val newExercises = day.exercises.toMutableList()
        newExercises[currentIdx] = newPlanned
        val newDay = day.copy(exercises = newExercises.sortedBy { it.order })
        val newPlan = plan.replaceDay(newDay)

        return WorkoutPlanRepository.AtomicPlanMutation.Commit(newPlan, WorkoutExerciseEditResult.Updated)
    }

    private fun handleMove(
        plan: WorkoutPlan,
        edit: WorkoutExerciseEdit.Move,
    ): WorkoutPlanRepository.AtomicPlanMutation<WorkoutExerciseEditResult> {
        val day = plan.days.firstOrNull { it.id == edit.workoutDayId }
            ?: return WorkoutPlanRepository.AtomicPlanMutation.Reject(WorkoutExerciseEditResult.DayNotFound)

        val fromIdx = day.exercises.indexOfFirst { it.exerciseId == edit.exerciseId }
        if (fromIdx == -1) {
            return WorkoutPlanRepository.AtomicPlanMutation.Reject(WorkoutExerciseEditResult.ExerciseNotFound)
        }

        if (edit.targetIndex < 0 || edit.targetIndex >= day.exercises.size) {
            return WorkoutPlanRepository.AtomicPlanMutation.Reject(WorkoutExerciseEditResult.InvalidTargetIndex)
        }

        if (fromIdx == edit.targetIndex) {
            return WorkoutPlanRepository.AtomicPlanMutation.Reject(WorkoutExerciseEditResult.Unchanged)
        }

        val mutable = day.exercises.sortedBy { it.order }.toMutableList()
        val item = mutable.removeAt(fromIdx)
        mutable.add(edit.targetIndex, item)
        val normalized = mutable.mapIndexed { idx, pe -> pe.copy(order = idx) }

        val newDay = day.copy(exercises = normalized)
        val newPlan = plan.replaceDay(newDay)
        return WorkoutPlanRepository.AtomicPlanMutation.Commit(newPlan, WorkoutExerciseEditResult.Updated)
    }

    private fun handleAdd(
        plan: WorkoutPlan,
        edit: WorkoutExerciseEdit.AddExercises,
        profile: com.jgv.workoutplanner.domain.model.UserProfile?,
    ): WorkoutPlanRepository.AtomicPlanMutation<WorkoutExerciseEditResult> {
        if (profile == null) {
            return WorkoutPlanRepository.AtomicPlanMutation.Reject(WorkoutExerciseEditResult.PlanNotFound)
        }

        if (edit.exerciseIds.isEmpty()) {
            return WorkoutPlanRepository.AtomicPlanMutation.Reject(WorkoutExerciseEditResult.Unchanged)
        }

        val day = plan.days.firstOrNull { it.id == edit.workoutDayId }
            ?: return WorkoutPlanRepository.AtomicPlanMutation.Reject(WorkoutExerciseEditResult.DayNotFound)

        // Duplicate within selection
        if (edit.exerciseIds.size != edit.exerciseIds.toSet().size) {
            return WorkoutPlanRepository.AtomicPlanMutation.Reject(WorkoutExerciseEditResult.DuplicateExercise)
        }

        // Duplicate within day
        val existingIds = day.exercises.map { it.exerciseId }.toSet()
        if (edit.exerciseIds.any { it in existingIds }) {
            return WorkoutPlanRepository.AtomicPlanMutation.Reject(WorkoutExerciseEditResult.DuplicateExercise)
        }

        val capacity = WorkoutPlanTemplate.slotsFor(day.focus).size
        val remaining = capacity - day.exercises.size
        if (edit.exerciseIds.size > remaining) {
            return WorkoutPlanRepository.AtomicPlanMutation.Reject(WorkoutExerciseEditResult.CapacityExceeded)
        }

        // Validate eligibility, day-focus compatibility
        val eligibleDefinitions = getEligibleExercisesUseCase(profile).available.map { it.exercise }
        val eligibleById = eligibleDefinitions.associateBy { it.id }
        val allowedPatterns = WorkoutPlanTemplate.slotsFor(day.focus).flatMap { it.allowedPatterns }.toSet()

        for (id in edit.exerciseIds) {
            val def = eligibleById[id]
                ?: return WorkoutPlanRepository.AtomicPlanMutation.Reject(WorkoutExerciseEditResult.IneligibleExercise)
            if (def.movementPattern !in allowedPatterns) {
                return WorkoutPlanRepository.AtomicPlanMutation.Reject(WorkoutExerciseEditResult.IneligibleExercise)
            }
        }

        val catalogById = exerciseRepository.getAllExercises().associateBy { it.id }
        var nextOrder = day.exercises.size
        val toAppend = edit.exerciseIds.map { id ->
            val def = catalogById[id] ?: eligibleById[id]
            ?: return WorkoutPlanRepository.AtomicPlanMutation.Reject(WorkoutExerciseEditResult.IneligibleExercise)
            PlannedExercise(
                exerciseId = def.id,
                sets = def.defaultPrescription.sets.first,
                repRange = def.defaultPrescription.reps,
                restSeconds = def.defaultPrescription.restSeconds,
                order = nextOrder++,
            )
        }

        val newDay = day.copy(exercises = day.exercises + toAppend)
        val newPlan = plan.replaceDay(newDay)

        return WorkoutPlanRepository.AtomicPlanMutation.Commit(newPlan, WorkoutExerciseEditResult.Updated)
    }

    private fun WorkoutPlan.replaceDay(newDay: WorkoutDay): WorkoutPlan {
        return copy(days = days.map { if (it.id == newDay.id) newDay else it })
    }
}
