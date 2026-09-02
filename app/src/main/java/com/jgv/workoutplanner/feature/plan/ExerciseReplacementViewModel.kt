package com.jgv.workoutplanner.feature.plan

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.jgv.workoutplanner.core.WhileUiSubscribed
import com.jgv.workoutplanner.domain.model.ExerciseId
import com.jgv.workoutplanner.domain.model.WorkoutExerciseEdit
import com.jgv.workoutplanner.domain.model.WorkoutExerciseEditResult
import com.jgv.workoutplanner.domain.repository.ExerciseRepository
import com.jgv.workoutplanner.domain.repository.ProfileRepository
import com.jgv.workoutplanner.domain.repository.WorkoutPlanRepository
import com.jgv.workoutplanner.domain.usecase.GetExerciseReplacementsUseCase
import com.jgv.workoutplanner.domain.usecase.UpdateWorkoutExerciseUseCase
import com.jgv.workoutplanner.navigation.AppRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ExerciseReplacementViewModel internal constructor(
    route: AppRoute.ExerciseReplacement,
    private val profileRepository: ProfileRepository,
    private val workoutPlanRepository: WorkoutPlanRepository,
    private val exerciseRepository: ExerciseRepository,
    private val getReplacementsUseCase: GetExerciseReplacementsUseCase,
    private val updateUseCase: UpdateWorkoutExerciseUseCase,
) : ViewModel() {

    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        profileRepository: ProfileRepository,
        workoutPlanRepository: WorkoutPlanRepository,
        exerciseRepository: ExerciseRepository,
        getReplacementsUseCase: GetExerciseReplacementsUseCase,
        updateUseCase: UpdateWorkoutExerciseUseCase,
    ) : this(
        route = savedStateHandle.toRoute<AppRoute.ExerciseReplacement>(),
        profileRepository = profileRepository,
        workoutPlanRepository = workoutPlanRepository,
        exerciseRepository = exerciseRepository,
        getReplacementsUseCase = getReplacementsUseCase,
        updateUseCase = updateUseCase,
    )

    val workoutDayId: String = route.workoutDayId
    val currentExerciseId: ExerciseId = route.exerciseId

    private val selectedFlow = MutableStateFlow<ExerciseId?>(null)

    private val _effects = Channel<ExerciseReplacementEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    val uiState: StateFlow<ExerciseReplacementUiState> = combine(
        profileRepository.profile,
        workoutPlanRepository.currentPlan,
        workoutPlanRepository.requiresRegeneration,
        selectedFlow,
    ) { profile, plan, requiresRegeneration, selected ->
        if (requiresRegeneration || profile == null || plan == null) {
            return@combine ExerciseReplacementUiState(
                isLoading = false,
                isStale = true,
                noCandidates = false,
            )
        }

        val day = plan.days.firstOrNull { it.id == workoutDayId }
        if (day == null) {
            return@combine ExerciseReplacementUiState(isLoading = false, isStale = true)
        }

        if (day.exercises.none { it.exerciseId == currentExerciseId }) {
            return@combine ExerciseReplacementUiState(isLoading = false, isStale = true)
        }

        val exerciseDefsById = exerciseRepository.getAllExercises().associateBy { it.id }
        val currentDef = exerciseDefsById[currentExerciseId]
        val currentName = currentDef?.let { def ->
            // Name resolution will be done in UI via stringResource; keep enum name for now
            def.id.name
        } ?: currentExerciseId.name

        val excludedFromDay = day.exercises
            .filter { it.exerciseId != currentExerciseId }
            .map { it.exerciseId }
            .toSet()

        val candidates = getReplacementsUseCase(
            currentExerciseId = currentExerciseId,
            profile = profile,
            excludedExerciseIds = excludedFromDay,
        )

        ExerciseReplacementUiState(
            isLoading = false,
            currentExerciseName = currentName,
            candidates = candidates,
            selectedExerciseId = selected,
            noCandidates = candidates.isEmpty(),
            isStale = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = WhileUiSubscribed,
        initialValue = ExerciseReplacementUiState(isLoading = true),
    )

    fun onEvent(event: ExerciseReplacementEvent) {
        when (event) {
            is ExerciseReplacementEvent.SelectCandidate -> {
                selectedFlow.value = event.exerciseId
            }
            ExerciseReplacementEvent.ConfirmReplacement -> {
                val selected = selectedFlow.value ?: return
                viewModelScope.launch {
                    val result = updateUseCase(
                        WorkoutExerciseEdit.Replace(
                            workoutDayId = workoutDayId,
                            currentExerciseId = currentExerciseId,
                            replacementExerciseId = selected,
                        ),
                    )
                    handleResult(result)
                }
            }
            ExerciseReplacementEvent.Back -> {
                viewModelScope.launch { _effects.send(ExerciseReplacementEffect.NavigateBack) }
            }
        }
    }

    private suspend fun handleResult(result: WorkoutExerciseEditResult) {
        when (result) {
            WorkoutExerciseEditResult.Updated -> {
                _effects.send(ExerciseReplacementEffect.NavigateBack)
            }
            WorkoutExerciseEditResult.Unchanged,
            WorkoutExerciseEditResult.PlanNotFound,
            WorkoutExerciseEditResult.PlanOutdated,
            WorkoutExerciseEditResult.DayNotFound,
            WorkoutExerciseEditResult.ExerciseNotFound,
            WorkoutExerciseEditResult.InvalidTargetIndex,
            WorkoutExerciseEditResult.IneligibleExercise,
            WorkoutExerciseEditResult.DuplicateExercise,
            WorkoutExerciseEditResult.CapacityExceeded -> {
                // Explicit handling for every typed result — failures emit typed effect
                _effects.send(ExerciseReplacementEffect.EditFailed(result))
            }
        }
    }
}

sealed interface ExerciseReplacementEffect {
    data object NavigateBack : ExerciseReplacementEffect
    data class EditFailed(val result: WorkoutExerciseEditResult) : ExerciseReplacementEffect
}
