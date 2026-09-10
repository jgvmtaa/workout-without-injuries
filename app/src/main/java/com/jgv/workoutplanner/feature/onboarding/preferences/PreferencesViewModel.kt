package com.jgv.workoutplanner.feature.onboarding.preferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jgv.workoutplanner.core.WhileUiSubscribed
import com.jgv.workoutplanner.domain.model.Equipment
import com.jgv.workoutplanner.domain.model.OnboardingDraft
import com.jgv.workoutplanner.domain.repository.ProfileRepository
import com.jgv.workoutplanner.domain.usecase.DetermineWorkoutSplitUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Collects goal, experience, schedule, session length and equipment (spec §4.3, §20).
 *
 * Every selection is written to the draft as it is made, so the answers survive leaving
 * the app mid-flow and the screen has no state of its own to fall out of sync
 * (spec §4.3, §21).
 *
 * The split is derived, never stored on the draft: [OnboardingDraft] holds
 * `daysPerWeek`, and the split is computed from it here for display and again when the
 * profile is assembled. One value, one source.
 */
@HiltViewModel
class PreferencesViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val determineWorkoutSplit: DetermineWorkoutSplitUseCase,
) : ViewModel() {

    val uiState: StateFlow<PreferencesUiState> =
        profileRepository.onboardingDraft
            .map { draft -> draft.toUiState() }
            .stateIn(
                scope = viewModelScope,
                started = WhileUiSubscribed,
                initialValue = PreferencesUiState(),
            )

    fun onEvent(event: PreferencesEvent) {
        when (event) {
            is PreferencesEvent.SelectGoal ->
                updateDraft { it.copy(goal = event.goal) }

            is PreferencesEvent.SelectExperience ->
                updateDraft { it.copy(experienceLevel = event.level) }

            is PreferencesEvent.SelectDaysPerWeek ->
                updateDraft { it.copy(daysPerWeek = event.days) }

            is PreferencesEvent.SelectSessionDuration ->
                updateDraft { it.copy(sessionDurationMinutes = event.minutes) }

            is PreferencesEvent.ToggleEquipment ->
                updateDraft { it.withEquipment(event.equipment, event.available) }

            // Navigation intents; handled by the route.
            PreferencesEvent.Continue, PreferencesEvent.Back -> Unit
        }
    }

    private fun updateDraft(transform: (OnboardingDraft) -> OnboardingDraft) {
        viewModelScope.launch { profileRepository.updateDraft(transform) }
    }

    private fun OnboardingDraft.toUiState() = PreferencesUiState(
        isLoading = false,
        goal = goal,
        experienceLevel = experienceLevel,
        daysPerWeek = daysPerWeek,
        sessionDurationMinutes = sessionDurationMinutes,
        // Bodyweight is always available, so it is never shown as a selection.
        selectedEquipment = availableEquipment - Equipment.BODYWEIGHT,
        derivedSplit = daysPerWeek?.let(determineWorkoutSplit::invoke),
    )
}

/**
 * Adds or removes [equipment], keeping [Equipment.BODYWEIGHT] present whatever happens.
 *
 * Enforced here rather than trusted from the UI: the draft is what gets persisted and
 * what plan generation reads, so "there is always at least bodyweight" (spec §25) has
 * to hold at the data boundary, not at the checkbox.
 */
private fun OnboardingDraft.withEquipment(
    equipment: Equipment,
    available: Boolean,
): OnboardingDraft {
    if (equipment == Equipment.BODYWEIGHT) return this
    val updated = if (available) {
        availableEquipment + equipment
    } else {
        availableEquipment - equipment
    }
    return copy(availableEquipment = updated + Equipment.BODYWEIGHT)
}
