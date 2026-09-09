package com.jgv.workoutplanner.feature.exerciselibrary

import com.jgv.workoutplanner.domain.model.Equipment
import com.jgv.workoutplanner.domain.model.ExerciseAvailability
import com.jgv.workoutplanner.domain.model.ExerciseDefinition
import com.jgv.workoutplanner.domain.model.ExerciseEligibility
import com.jgv.workoutplanner.domain.model.ExclusionReason
import com.jgv.workoutplanner.domain.model.MuscleGroup

/**
 * Row displayed in the exercise library (README §14).
 *
 * Combines the catalog definition with the eligibility result for the current profile,
 * so the screen can render both the description and the availability badge without
 * recomputing eligibility on every composition.
 */
data class ExerciseRowUiModel(
    val definition: ExerciseDefinition,
    val eligibility: ExerciseEligibility,
    val availability: ExerciseAvailability,
) {
    /** Number of conflicting limitations — surfaced as "Excluded by N limitation(s)" (Phase 4.4). */
    val conflictingLimitationCount: Int =
        eligibility.exclusionReasons.count { it is ExclusionReason.ConflictingLimitation }
}

/**
 * Immutable state for the exercise library screen (README §14, §20).
 *
 * Filtering is deterministic: same profile + same filters ⇒ same [filteredRows],
 * because sorting is always by ExerciseId.name.
 */
data class ExerciseLibraryUiState(
    val isLoading: Boolean = true,
    val allRows: List<ExerciseRowUiModel> = emptyList(),
    val filteredRows: List<ExerciseRowUiModel> = emptyList(),
    val selectedMuscleGroup: MuscleGroup? = null,
    val searchQuery: String = "",
    val selectedEquipment: Equipment? = null,
    val availableCount: Int = 0,
    val excludedCount: Int = 0,
    val unavailableCount: Int = 0,
) {
    val isEmpty: Boolean get() = !isLoading && filteredRows.isEmpty()

    /**
     * True only when limitation conflicts exclude every row in the current category.
     * Equipment- or experience-unavailable rows are intentionally not classified as
     * limitation exclusions (README §25).
     */
    val areAllFilteredExercisesExcluded: Boolean
        get() = !isLoading &&
            filteredRows.isNotEmpty() &&
            filteredRows.all { it.availability == ExerciseAvailability.EXCLUDED }

    // Instance getters delegate to shared constants to avoid per-emission allocation.
    // Previously these were per-instance vals: `MuscleGroup.entries` and
    // `Equipment.entries.filter {}` were re-allocated on every state emission.
    val muscleGroupOptions: List<MuscleGroup> get() = DefaultMuscleGroupOptions
    val equipmentOptions: List<Equipment> get() = DefaultEquipmentOptions

    companion object {
        /** Shared — MuscleGroup.entries is stable, no need to recreate per UiState. */
        val DefaultMuscleGroupOptions: List<MuscleGroup> = MuscleGroup.entries

        /** Shared filtered list — strength-only catalog excludes cardio machine. */
        val DefaultEquipmentOptions: List<Equipment> =
            Equipment.entries.filter { it != Equipment.CARDIO_MACHINE }
    }
}

/** Everything the library screen can do (README §20). */
sealed interface ExerciseLibraryEvent {
    data class SelectMuscleGroup(val group: MuscleGroup?) : ExerciseLibraryEvent
    data class SearchQueryChanged(val query: String) : ExerciseLibraryEvent
    data class SelectEquipment(val equipment: Equipment?) : ExerciseLibraryEvent
    data object ClearFilters : ExerciseLibraryEvent
    data class OpenExercise(val exerciseId: com.jgv.workoutplanner.domain.model.ExerciseId) : ExerciseLibraryEvent
    data object Back : ExerciseLibraryEvent
}
