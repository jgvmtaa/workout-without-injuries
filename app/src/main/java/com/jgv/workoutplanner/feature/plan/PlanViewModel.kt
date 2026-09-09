package com.jgv.workoutplanner.feature.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jgv.workoutplanner.domain.model.WorkoutDayFocus
import com.jgv.workoutplanner.domain.model.WorkoutExerciseEdit
import com.jgv.workoutplanner.domain.model.WorkoutExerciseEditResult
import com.jgv.workoutplanner.domain.model.WorkoutPlanTemplate
import com.jgv.workoutplanner.domain.model.UserProfile
import com.jgv.workoutplanner.domain.repository.ExerciseRepository
import com.jgv.workoutplanner.domain.repository.ProfileRepository
import com.jgv.workoutplanner.domain.repository.WorkoutPlanRepository
import com.jgv.workoutplanner.domain.usecase.GenerateWorkoutPlanUseCase
import com.jgv.workoutplanner.domain.usecase.UpdateWorkoutExerciseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the plan screen (README §18, §20, task 5.8, Phase 6).
 *
 * - Observes profile + current plan + warnings + requiresRegeneration.
 * - Auto-generates on entry if no plan exists.
 * - Manual edits go through [UpdateWorkoutExerciseUseCase] — typed results, one-shot effects.
 * - While outdated, editing is disabled, warnings hidden, existing plan visible.
 * - Regeneration uses [WorkoutPlanRepository.saveGeneratedPlan] which clears outdated only after success.
 */
@HiltViewModel
class PlanViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val workoutPlanRepository: WorkoutPlanRepository,
    private val exerciseRepository: ExerciseRepository,
    private val generateWorkoutPlanUseCase: GenerateWorkoutPlanUseCase,
    private val updateWorkoutExerciseUseCase: UpdateWorkoutExerciseUseCase,
) : ViewModel() {

    private val exercisesById = exerciseRepository.getAllExercises().associateBy { it.id }

    private val generatingFlow = MutableStateFlow(false)
    private val showRegenerateConfirmFlow = MutableStateFlow(false)
    private val pendingRemovalFlow = MutableStateFlow<PendingExerciseRemoval?>(null)

    private val _effects = Channel<PlanEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    val uiState: StateFlow<PlanUiState> = combine(
        profileRepository.profile,
        workoutPlanRepository.currentPlan,
        workoutPlanRepository.currentWarnings,
        workoutPlanRepository.requiresRegeneration,
        generatingFlow,
    ) { profile, plan, warnings, requiresRegeneration, generating ->
        when {
            profile == null -> PlanUiState(
                isLoading = false,
                hasNoPlan = true,
                isGenerating = generating,
                requiresRegeneration = false,
                warnings = emptyList(),
            )
            plan == null -> PlanUiState(
                isLoading = false,
                hasNoPlan = true,
                isGenerating = generating,
                requiresRegeneration = requiresRegeneration,
                warnings = if (requiresRegeneration) emptyList() else warnings,
            )
            else -> {
                val dayUiModels = plan.days.map { day ->
                    val exerciseModels = day.exercises.sortedBy { it.order }.map { pe ->
                        PlannedExerciseUiModel(
                            exerciseId = pe.exerciseId,
                            definition = exercisesById[pe.exerciseId],
                            sets = pe.sets,
                            repRange = pe.repRange,
                            restSeconds = pe.restSeconds,
                            order = pe.order,
                        )
                    }
                    val capacity = WorkoutPlanTemplate.slotsFor(day.focus).size
                    WorkoutDayUiModel(
                        id = day.id,
                        name = day.name,
                        focus = day.focus,
                        exercises = exerciseModels,
                        remainingCapacity = (capacity - exerciseModels.size).coerceAtLeast(0),
                    )
                }
                PlanUiState(
                    isLoading = false,
                    isGenerating = generating,
                    planName = plan.name,
                    days = dayUiModels,
                    // Preserve in storage but hide in UI when outdated
                    warnings = if (requiresRegeneration) emptyList() else warnings,
                    hasNoPlan = false,
                    requiresRegeneration = requiresRegeneration,
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PlanUiState(isLoading = true),
    )

    val showRegenerateConfirm: StateFlow<Boolean> = showRegenerateConfirmFlow.asStateFlow()
    val pendingRemoval: StateFlow<PendingExerciseRemoval?> = pendingRemovalFlow.asStateFlow()

    fun onEvent(event: PlanEvent) {
        when (event) {
            PlanEvent.GeneratePlan -> generatePlan()
            PlanEvent.RegeneratePlan -> {
                showRegenerateConfirmFlow.value = true
            }
            PlanEvent.ConfirmRegeneration -> {
                showRegenerateConfirmFlow.value = false
                generatePlan()
            }
            PlanEvent.DismissRegenerationConfirm -> {
                showRegenerateConfirmFlow.value = false
            }
            is PlanEvent.RequestRemoveExercise -> {
                pendingRemovalFlow.value = PendingExerciseRemoval(event.dayId, event.exerciseId)
            }
            PlanEvent.ConfirmRemoveExercise -> {
                val pending = pendingRemovalFlow.value ?: return
                pendingRemovalFlow.value = null
                removeExercise(pending)
            }
            PlanEvent.DismissRemoveExerciseConfirm -> {
                pendingRemovalFlow.value = null
            }
            is PlanEvent.MoveUp -> {
                viewModelScope.launch {
                    val current = workoutPlanRepository.currentPlan.first() ?: return@launch
                    val day = current.days.firstOrNull { it.id == event.dayId } ?: return@launch
                    val idx = day.exercises.indexOfFirst { it.exerciseId == event.exerciseId }
                    if (idx <= 0) return@launch
                    val result = updateWorkoutExerciseUseCase(
                        WorkoutExerciseEdit.Move(event.dayId, event.exerciseId, idx - 1),
                    )
                    handleEditResult(result)
                }
            }
            is PlanEvent.MoveDown -> {
                viewModelScope.launch {
                    val current = workoutPlanRepository.currentPlan.first() ?: return@launch
                    val day = current.days.firstOrNull { it.id == event.dayId } ?: return@launch
                    val idx = day.exercises.indexOfFirst { it.exerciseId == event.exerciseId }
                    if (idx == -1 || idx >= day.exercises.size - 1) return@launch
                    val result = updateWorkoutExerciseUseCase(
                        WorkoutExerciseEdit.Move(event.dayId, event.exerciseId, idx + 1),
                    )
                    handleEditResult(result)
                }
            }
            is PlanEvent.OpenExerciseDetails,
            is PlanEvent.ReplaceExercise,
            is PlanEvent.OpenExercisePicker,
            PlanEvent.Back -> Unit
        }
    }

    fun autoGenerateIfNeeded() {
        viewModelScope.launch {
            val profile = profileRepository.profile.first() ?: return@launch
            val plan = workoutPlanRepository.currentPlan.first()
            if (plan == null) {
                generateAndPersist(profile)
            }
        }
    }

    private fun generatePlan() {
        viewModelScope.launch {
            val profile = profileRepository.profile.first() ?: return@launch
            generateAndPersist(profile)
        }
    }

    private suspend fun generateAndPersist(profile: UserProfile) {
        if (generatingFlow.value) return
        generatingFlow.value = true
        try {
            val result = generateWorkoutPlanUseCase(profile)
            workoutPlanRepository.saveGeneratedPlan(result.plan, result.warnings)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            _effects.send(PlanEffect.GenerationFailed)
        } finally {
            generatingFlow.value = false
        }
    }

    private fun removeExercise(pending: PendingExerciseRemoval) {
        viewModelScope.launch {
            val result = updateWorkoutExerciseUseCase(
                WorkoutExerciseEdit.Remove(pending.dayId, pending.exerciseId),
            )
            handleEditResult(result)
        }
    }

    private suspend fun handleEditResult(result: WorkoutExerciseEditResult) {
        when (result) {
            WorkoutExerciseEditResult.Updated, WorkoutExerciseEditResult.Unchanged -> Unit
            else -> _effects.send(PlanEffect.EditFailed(result))
        }
    }
}
