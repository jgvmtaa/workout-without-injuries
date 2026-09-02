package com.jgv.workoutplanner.feature.exercisepicker

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.jgv.workoutplanner.core.WhileUiSubscribed
import com.jgv.workoutplanner.domain.model.Equipment
import com.jgv.workoutplanner.domain.model.ExerciseId
import com.jgv.workoutplanner.domain.model.MuscleGroup
import com.jgv.workoutplanner.domain.model.WorkoutExerciseEdit
import com.jgv.workoutplanner.domain.model.WorkoutExerciseEditResult
import com.jgv.workoutplanner.domain.model.WorkoutPlanTemplate
import com.jgv.workoutplanner.domain.repository.ProfileRepository
import com.jgv.workoutplanner.domain.repository.WorkoutPlanRepository
import com.jgv.workoutplanner.domain.usecase.GetExercisesForWorkoutDayUseCase
import com.jgv.workoutplanner.domain.usecase.UpdateWorkoutExerciseUseCase
import com.jgv.workoutplanner.navigation.AppRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ExercisePickerViewModel internal constructor(
    route: AppRoute.ExercisePicker,
    private val profileRepository: ProfileRepository,
    private val workoutPlanRepository: WorkoutPlanRepository,
    private val getExercisesForDayUseCase: GetExercisesForWorkoutDayUseCase,
    private val updateUseCase: UpdateWorkoutExerciseUseCase,
) : ViewModel() {

    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        profileRepository: ProfileRepository,
        workoutPlanRepository: WorkoutPlanRepository,
        getExercisesForDayUseCase: GetExercisesForWorkoutDayUseCase,
        updateUseCase: UpdateWorkoutExerciseUseCase,
    ) : this(
        route = savedStateHandle.toRoute<AppRoute.ExercisePicker>(),
        profileRepository = profileRepository,
        workoutPlanRepository = workoutPlanRepository,
        getExercisesForDayUseCase = getExercisesForDayUseCase,
        updateUseCase = updateUseCase,
    )

    val workoutDayId: String = route.workoutDayId

    private val searchQuery = MutableStateFlow("")
    private val selectedMuscle = MutableStateFlow<MuscleGroup?>(null)
    private val selectedEquipment = MutableStateFlow<Equipment?>(null)
    private val selectedOrdered = MutableStateFlow<List<ExerciseId>>(emptyList())

    private val _effects = Channel<ExercisePickerEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private data class Filters(
        val query: String,
        val muscle: MuscleGroup?,
        val equipment: Equipment?,
        val selected: List<ExerciseId>,
    )

    private val baseFlow = combine(
        profileRepository.profile,
        workoutPlanRepository.currentPlan,
        workoutPlanRepository.requiresRegeneration,
    ) { profile, plan, requires ->
        Triple(profile, plan, requires)
    }

    private val filtersFlow = combine(
        searchQuery,
        selectedMuscle,
        selectedEquipment,
        selectedOrdered,
    ) { q, m, e, s ->
        Filters(q, m, e, s)
    }

    val uiState: StateFlow<ExercisePickerUiState> = combine(
        baseFlow,
        filtersFlow,
    ) { base, filters ->
        val (profile, plan, requiresRegeneration) = base
        val query = filters.query
        val muscle = filters.muscle
        val equipment = filters.equipment
        val selected = filters.selected

        if (requiresRegeneration || profile == null || plan == null) {
            return@combine ExercisePickerUiState(isLoading = false, isStale = true)
        }

        val day = plan.days.firstOrNull { it.id == workoutDayId }
        if (day == null) {
            return@combine ExercisePickerUiState(isLoading = false, isStale = true)
        }

        val remainingCapacity = WorkoutPlanTemplate.slotsFor(day.focus).size - day.exercises.size
        if (remainingCapacity <= 0) {
            return@combine ExercisePickerUiState(isLoading = false, isStale = true)
        }

        val excludedIds = day.exercises.map { it.exerciseId }.toSet()

        val allCandidates = getExercisesForDayUseCase(
            profile = profile,
            dayFocus = day.focus,
            excludedExerciseIds = excludedIds,
        )

        val filtered = allCandidates.filter { def ->
            val matchesMuscle = muscle == null || def.primaryMuscle == muscle
            val matchesEquipment = equipment == null || equipment in def.requiredEquipment
            val normalizedQuery = query.lowercase(Locale.ROOT).replace('_', ' ').trim()
            val matchesQuery = normalizedQuery.isBlank() ||
                def.id.name.lowercase(Locale.ROOT).replace('_', ' ').contains(normalizedQuery)
            matchesMuscle && matchesEquipment && matchesQuery
        }

        ExercisePickerUiState(
            isLoading = false,
            isStale = false,
            dayName = day.name,
            remainingCapacity = remainingCapacity,
            allCandidates = allCandidates,
            filteredCandidates = filtered,
            selectedIdsOrdered = selected,
            searchQuery = query,
            selectedMuscleGroup = muscle,
            selectedEquipment = equipment,
        )
    }.stateIn(
        scope = viewModelScope,
        started = WhileUiSubscribed,
        initialValue = ExercisePickerUiState(isLoading = true),
    )

    fun onEvent(event: ExercisePickerEvent) {
        when (event) {
            is ExercisePickerEvent.ToggleSelection -> {
                val current = selectedOrdered.value
                val capacity = uiState.value.remainingCapacity
                if (event.exerciseId in current) {
                    selectedOrdered.value = current.filterNot { it == event.exerciseId }
                } else {
                    if (current.size >= capacity) return
                    selectedOrdered.value = current + event.exerciseId
                }
            }
            is ExercisePickerEvent.SearchQueryChanged -> searchQuery.value = event.query
            is ExercisePickerEvent.SelectMuscleGroup -> selectedMuscle.value = event.group
            is ExercisePickerEvent.SelectEquipment -> selectedEquipment.value = event.equipment
            ExercisePickerEvent.ClearFilters -> {
                selectedMuscle.value = null
                selectedEquipment.value = null
                searchQuery.value = ""
            }
            ExercisePickerEvent.Confirm -> {
                val ids = selectedOrdered.value
                if (ids.isEmpty()) return
                viewModelScope.launch {
                    val result = updateUseCase(
                        WorkoutExerciseEdit.AddExercises(
                            workoutDayId = workoutDayId,
                            exerciseIds = ids,
                        ),
                    )
                    handleResult(result)
                }
            }
            ExercisePickerEvent.Back -> {
                viewModelScope.launch { _effects.send(ExercisePickerEffect.NavigateBack) }
            }
        }
    }

    private suspend fun handleResult(result: WorkoutExerciseEditResult) {
        when (result) {
            WorkoutExerciseEditResult.Updated -> {
                _effects.send(ExercisePickerEffect.NavigateBack)
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
                _effects.send(ExercisePickerEffect.EditFailed(result))
            }
        }
    }
}
