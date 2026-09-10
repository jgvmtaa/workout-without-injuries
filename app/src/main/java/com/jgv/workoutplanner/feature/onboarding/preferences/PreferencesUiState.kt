package com.jgv.workoutplanner.feature.onboarding.preferences

import com.jgv.workoutplanner.domain.model.Equipment
import com.jgv.workoutplanner.domain.model.ExperienceLevel
import com.jgv.workoutplanner.domain.model.TrainingGoal
import com.jgv.workoutplanner.domain.model.TrainingOptions
import com.jgv.workoutplanner.domain.model.WorkoutSplit

/**
 * Immutable state for the training preferences screen (spec §4.3, §20).
 *
 * Every answer is nullable because none of them has a safe default: a goal the user did
 * not choose would silently shape their whole plan. [canContinue] is what enforces that —
 * the screen cannot be left half-answered.
 *
 * [derivedSplit] is output, not input. spec §4.3 permits the MVP to choose the split
 * from the training frequency rather than asking, so it is shown read-only and updates as
 * [daysPerWeek] changes; `null` until a frequency is picked.
 */
data class PreferencesUiState(
    val isLoading: Boolean = true,
    val goal: TrainingGoal? = null,
    val experienceLevel: ExperienceLevel? = null,
    val daysPerWeek: Int? = null,
    val sessionDurationMinutes: Int? = null,
    val selectedEquipment: Set<Equipment> = emptySet(),
    val derivedSplit: WorkoutSplit? = null,
) {

    /** Frequencies offered, in order (spec §4.3). */
    val dayOptions: List<Int> = TrainingOptions.DAYS_PER_WEEK

    /** Session lengths offered, in order (spec §4.3). */
    val durationOptions: List<Int> = TrainingOptions.SESSION_DURATION_MINUTES

    /**
     * Equipment the user can toggle.
     *
     * [Equipment.BODYWEIGHT] is not in here — it is shown as a fixed, disabled row
     * because it is always available (spec §25). Neither is `CARDIO_MACHINE`: the MVP
     * catalog is strength-only, so offering it would be offering something inert.
     */
    val equipmentOptions: List<Equipment> = TrainingOptions.SELECTABLE_EQUIPMENT

    /**
     * Every question is answered.
     *
     * Equipment is not part of this: selecting none is a valid answer that means
     * "bodyweight only", which the domain guarantees is never empty (spec §25).
     */
    val canContinue: Boolean
        get() = goal != null &&
            experienceLevel != null &&
            daysPerWeek != null &&
            sessionDurationMinutes != null
}

/** Everything the preferences screen can do (spec §20). */
sealed interface PreferencesEvent {

    data class SelectGoal(val goal: TrainingGoal) : PreferencesEvent

    data class SelectExperience(val level: ExperienceLevel) : PreferencesEvent

    data class SelectDaysPerWeek(val days: Int) : PreferencesEvent

    data class SelectSessionDuration(val minutes: Int) : PreferencesEvent

    /** Toggles one piece of equipment. Bodyweight is never toggled. */
    data class ToggleEquipment(val equipment: Equipment, val available: Boolean) : PreferencesEvent

    data object Continue : PreferencesEvent

    data object Back : PreferencesEvent
}
