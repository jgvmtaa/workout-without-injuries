package com.jgv.workoutplanner.feature.exerciselibrary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jgv.workoutplanner.core.WhileUiSubscribed
import com.jgv.workoutplanner.domain.model.Equipment
import com.jgv.workoutplanner.domain.model.ExerciseAvailability
import com.jgv.workoutplanner.domain.model.ExerciseEligibility
import com.jgv.workoutplanner.domain.model.MuscleGroup
import com.jgv.workoutplanner.domain.model.availability
import com.jgv.workoutplanner.domain.repository.ProfileRepository
import com.jgv.workoutplanner.domain.usecase.GetEligibleExercisesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Locale
import javax.inject.Inject

/**
 * Loads eligibility for the current profile and applies library filters (README §14, §20).
 *
 * Filtering is deterministic and runs in the ViewModel, not the composable, so the same
 * (profile, muscle, equipment, query) always produces the same ordered list.
 * Order is stable by ExerciseId.name — no timestamp, no random.
 *
 * Search is by ExerciseId.name (case-insensitive) with underscores normalized to spaces
 * so multi-word queries like "chest press" match MACHINE_CHEST_PRESS. Display names need
 * Context for resolution, so they cannot be searched in the ViewModel without breaking
 * the Android-independence of the domain use cases. The compromise is documented on the
 * UiState and is acceptable for MVP — id names contain the human-readable tokens
 * (e.g. MACHINE_CHEST_PRESS matches "chest press" after normalization).
 */
@HiltViewModel
class ExerciseLibraryViewModel @Inject constructor(
    profileRepository: ProfileRepository,
    private val getEligibleExercises: GetEligibleExercisesUseCase,
) : ViewModel() {

    private val selectedMuscleGroup = MutableStateFlow<MuscleGroup?>(null)
    private val searchQuery = MutableStateFlow("")
    private val selectedEquipment = MutableStateFlow<Equipment?>(null)

    private val profileFlow = profileRepository.profile

    /** Profile-only snapshot that drives [allRowsStateFlow]. */
    private sealed interface AllRowsState {
        data object Loading : AllRowsState
        data object NoProfile : AllRowsState
        data class Ready(val rows: List<ExerciseRowUiModel>) : AllRowsState
    }

    /**
     * Profile-only expensive derivation: evaluate all 59 exercises and build stable sorted rows.
     * Depends only on [profileFlow], so search/muscle/equipment keystrokes do not re-evaluate
     * eligibility. Runs on Default dispatcher to avoid blocking Main.immediate.
     */
    private val allRowsStateFlow: StateFlow<AllRowsState> =
        profileFlow
            .map { profile ->
                if (profile == null) {
                    AllRowsState.NoProfile
                } else {
                    val filtered = getEligibleExercises(profile)
                    val rows =
                        (filtered.available + filtered.excluded + filtered.unavailable)
                            .map { eligibility -> eligibility.toRow() }
                            .sortedBy { it.definition.id.name }
                    AllRowsState.Ready(rows)
                }
            }
            .flowOn(Dispatchers.Default)
            .stateIn(
                scope = viewModelScope,
                started = WhileUiSubscribed,
                initialValue = AllRowsState.Loading,
            )

    val uiState: StateFlow<ExerciseLibraryUiState> =
        combine(
            allRowsStateFlow,
            selectedMuscleGroup,
            searchQuery,
            selectedEquipment,
        ) { allRowsState, muscle, query, equipment ->
            when (allRowsState) {
                is AllRowsState.Loading -> ExerciseLibraryUiState(isLoading = true)
                is AllRowsState.NoProfile -> ExerciseLibraryUiState(isLoading = false)
                is AllRowsState.Ready ->
                    buildFilteredState(
                        allRows = allRowsState.rows,
                        muscleFilter = muscle,
                        query = query,
                        equipmentFilter = equipment,
                    )
            }
        }.stateIn(
            scope = viewModelScope,
            started = WhileUiSubscribed,
            initialValue = ExerciseLibraryUiState(isLoading = true),
        )

    fun onEvent(event: ExerciseLibraryEvent) {
        when (event) {
            is ExerciseLibraryEvent.SelectMuscleGroup -> selectedMuscleGroup.value = event.group
            is ExerciseLibraryEvent.SearchQueryChanged -> searchQuery.value = event.query
            is ExerciseLibraryEvent.SelectEquipment -> selectedEquipment.value = event.equipment
            ExerciseLibraryEvent.ClearFilters -> {
                selectedMuscleGroup.value = null
                searchQuery.value = ""
                selectedEquipment.value = null
            }
            // Navigation intents handled by the Route.
            is ExerciseLibraryEvent.OpenExercise, ExerciseLibraryEvent.Back -> Unit
        }
    }

    private fun buildFilteredState(
        allRows: List<ExerciseRowUiModel>,
        muscleFilter: MuscleGroup?,
        query: String,
        equipmentFilter: Equipment?,
    ): ExerciseLibraryUiState {
        val filteredRows = allRows.filter { row ->
            val matchesMuscle = muscleFilter == null || row.definition.primaryMuscle == muscleFilter
            val matchesEquipment = equipmentFilter == null || equipmentFilter in row.definition.requiredEquipment
            val matchesQuery = query.isBlank() || row.matchesQuery(query)
            matchesMuscle && matchesEquipment && matchesQuery
        }

        return ExerciseLibraryUiState(
            isLoading = false,
            allRows = allRows,
            filteredRows = filteredRows,
            selectedMuscleGroup = muscleFilter,
            searchQuery = query,
            selectedEquipment = equipmentFilter,
            availableCount = filteredRows.count { it.availability == ExerciseAvailability.AVAILABLE },
            excludedCount = filteredRows.count { it.availability == ExerciseAvailability.EXCLUDED },
            unavailableCount = filteredRows.count { it.availability == ExerciseAvailability.UNAVAILABLE },
        )
    }

    private fun ExerciseEligibility.toRow(): ExerciseRowUiModel =
        ExerciseRowUiModel(
            definition = exercise,
            eligibility = this,
            availability = availability(),
        )

    private fun ExerciseRowUiModel.matchesQuery(query: String): Boolean {
        val normalizedQuery = query.lowercase(Locale.ROOT).replace('_', ' ').trim()
        if (normalizedQuery.isBlank()) return true

        fun String.normalized() = lowercase(Locale.ROOT).replace('_', ' ')

        // Search on enum name (deterministic, no Context needed).
        // Normalize underscores to spaces on both haystack and query so
        // multi-word queries like "chest press" match MACHINE_CHEST_PRESS.
        return definition.id.name.normalized().contains(normalizedQuery) ||
            definition.primaryMuscle.name.normalized().contains(normalizedQuery) ||
            definition.movementPattern.name.normalized().contains(normalizedQuery)
    }
}
