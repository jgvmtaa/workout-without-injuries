package com.jgv.workoutplanner.feature.exercisepicker

import com.jgv.workoutplanner.domain.model.Equipment
import com.jgv.workoutplanner.domain.model.ExerciseDefinition
import com.jgv.workoutplanner.domain.model.ExerciseId
import com.jgv.workoutplanner.domain.model.MuscleGroup

data class ExercisePickerUiState(
    val isLoading: Boolean = true,
    val isStale: Boolean = false,
    val dayName: String? = null,
    val remainingCapacity: Int = 0,
    val allCandidates: List<ExerciseDefinition> = emptyList(),
    val filteredCandidates: List<ExerciseDefinition> = emptyList(),
    val selectedIdsOrdered: List<ExerciseId> = emptyList(),
    val searchQuery: String = "",
    val selectedMuscleGroup: MuscleGroup? = null,
    val selectedEquipment: Equipment? = null,
) {
    /**
     * Whether the query narrows anything. Blank, not empty: the ViewModel treats a
     * whitespace-only query as "match all", so every affordance that claims a search is
     * active must agree with that or it will offer to clear a filter that is not there.
     */
    val hasSearchQuery: Boolean get() = searchQuery.isNotBlank()

    /** Whether anything is narrowing the candidate list — gates the Clear filters action. */
    val hasActiveFilters: Boolean
        get() = hasSearchQuery || selectedMuscleGroup != null || selectedEquipment != null
}

sealed interface ExercisePickerEvent {
    data class ToggleSelection(val exerciseId: ExerciseId) : ExercisePickerEvent
    data class SearchQueryChanged(val query: String) : ExercisePickerEvent
    data class SelectMuscleGroup(val group: MuscleGroup?) : ExercisePickerEvent
    data class SelectEquipment(val equipment: Equipment?) : ExercisePickerEvent
    data object ClearFilters : ExercisePickerEvent
    data object Confirm : ExercisePickerEvent
    data object Back : ExercisePickerEvent
}

sealed interface ExercisePickerEffect {
    data object NavigateBack : ExercisePickerEffect
    data class EditFailed(val result: com.jgv.workoutplanner.domain.model.WorkoutExerciseEditResult) : ExercisePickerEffect
}
