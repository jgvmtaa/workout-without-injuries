package com.jgv.workoutplanner.feature.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jgv.workoutplanner.domain.repository.ExerciseRepository
import com.jgv.workoutplanner.domain.repository.ProfileRepository
import com.jgv.workoutplanner.domain.repository.WorkoutPlanRepository
import com.jgv.workoutplanner.domain.usecase.GenerateWorkoutPlanUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the plan screen (README §18, §20, task 5.8).
 *
 * - Observes profile + current plan + exercise catalog + persisted warnings.
 * - Auto-generates on entry if no plan exists (per Phase 5 decision: Home button + auto-gen).
 * - Generation is deterministic and never crashes on restrictive profiles – unfillable slots
 *   become warnings (README §25, task 5.6). Warnings are now persisted alongside the plan
 *   in [WorkoutPlanRepository] so they survive process death / navigation.
 */
@HiltViewModel
class PlanViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val workoutPlanRepository: WorkoutPlanRepository,
    private val exerciseRepository: ExerciseRepository,
    private val generateWorkoutPlanUseCase: GenerateWorkoutPlanUseCase,
) : ViewModel() {

    private val exercisesById = exerciseRepository.getAllExercises().associateBy { it.id }

    private val generatingFlow = MutableStateFlow(false)

    val uiState: StateFlow<PlanUiState> = combine(
        profileRepository.profile,
        workoutPlanRepository.currentPlan,
        workoutPlanRepository.currentWarnings,
        generatingFlow,
    ) { profile, plan, warnings, generating ->
        when {
            profile == null -> PlanUiState(isLoading = false, hasNoPlan = true, isGenerating = generating)
            plan == null -> PlanUiState(isLoading = false, hasNoPlan = true, isGenerating = generating, warnings = warnings)
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
                    WorkoutDayUiModel(
                        id = day.id,
                        name = day.name,
                        focus = day.focus,
                        exercises = exerciseModels,
                    )
                }
                PlanUiState(
                    isLoading = false,
                    isGenerating = generating,
                    planName = plan.name,
                    days = dayUiModels,
                    warnings = warnings,
                    hasNoPlan = false,
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PlanUiState(isLoading = true),
    )

    fun onEvent(event: PlanEvent) {
        when (event) {
            PlanEvent.GeneratePlan, PlanEvent.RegeneratePlan -> generatePlan()
            is PlanEvent.OpenExerciseDetails, is PlanEvent.ReplaceExercise, PlanEvent.Back -> Unit
        }
    }

    fun autoGenerateIfNeeded() {
        viewModelScope.launch {
            val profile = profileRepository.profile.first() ?: return@launch
            val plan = workoutPlanRepository.currentPlan.first()
            if (plan == null) {
                generatingFlow.value = true
                val result = generateWorkoutPlanUseCase(profile)
                workoutPlanRepository.savePlan(result.plan, result.warnings)
                generatingFlow.value = false
            }
        }
    }

    private fun generatePlan() {
        viewModelScope.launch {
            val profile = profileRepository.profile.first() ?: return@launch
            generatingFlow.value = true
            val result = generateWorkoutPlanUseCase(profile)
            workoutPlanRepository.savePlan(result.plan, result.warnings)
            generatingFlow.value = false
        }
    }
}
