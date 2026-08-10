package com.jgv.workoutplanner.feature.onboarding.injuries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jgv.workoutplanner.core.WhileUiSubscribed
import com.jgv.workoutplanner.core.ui.labelRes
import com.jgv.workoutplanner.domain.model.InjuryId
import com.jgv.workoutplanner.domain.model.InjuryStatus
import com.jgv.workoutplanner.domain.model.OnboardingDraft
import com.jgv.workoutplanner.domain.model.SelectedInjury
import com.jgv.workoutplanner.domain.repository.InjuryRepository
import com.jgv.workoutplanner.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Collects injury history, grouped by body region (README §4.4, §20).
 *
 * Selecting an injury changes nothing about which exercises are eligible. It only widens
 * the set of limitations the next screen asks about (README §2) — that separation is the
 * product rule, and it is why this ViewModel writes to `selectedInjuries` and never
 * touches `confirmedLimitations`.
 */
@HiltViewModel
class InjuryHistoryViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    injuryRepository: InjuryRepository,
) : ViewModel() {

    /**
     * The catalog half of the state, built once.
     *
     * The catalog is static, so re-deriving it on every draft emission would be work
     * with no possible change in the result.
     */
    private val injuryGroups: List<InjuryGroupUiModel> =
        injuryRepository.getInjuriesGroupedByBodyRegion().map { (region, injuries) ->
            InjuryGroupUiModel(
                region = region,
                regionNameRes = region.labelRes,
                injuries = injuries.map { InjuryUiModel(id = it.id, nameRes = it.nameRes) },
            )
        }

    val uiState: StateFlow<InjuryHistoryUiState> =
        profileRepository.onboardingDraft
            .map { draft ->
                InjuryHistoryUiState(
                    isLoading = false,
                    injuryGroups = injuryGroups,
                    selectedInjuries = draft.selectedInjuries.associate { it.injuryId to it.status },
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = WhileUiSubscribed,
                initialValue = InjuryHistoryUiState(injuryGroups = injuryGroups),
            )

    fun onEvent(event: InjuryHistoryEvent) {
        when (event) {
            is InjuryHistoryEvent.ToggleInjury -> updateDraft { it.toggle(event.injuryId) }
            is InjuryHistoryEvent.SetStatus -> updateDraft {
                it.withStatus(event.injuryId, event.status)
            }
            // Navigation intents; handled by the route.
            InjuryHistoryEvent.Continue, InjuryHistoryEvent.Back -> Unit
        }
    }

    private fun updateDraft(transform: (OnboardingDraft) -> OnboardingDraft) {
        viewModelScope.launch { profileRepository.updateDraft(transform) }
    }
}

/**
 * Adds [injuryId] with the default status, or removes it if it is already selected.
 *
 * Deselecting drops the status with it. Keeping a "remembered" status for an injury the
 * user has unselected would mean re-selecting it silently restores an answer they may
 * have changed their mind about.
 */
private fun OnboardingDraft.toggle(injuryId: InjuryId): OnboardingDraft {
    val existing = selectedInjuries.firstOrNull { it.injuryId == injuryId }
    val updated = if (existing != null) {
        selectedInjuries - existing
    } else {
        selectedInjuries + SelectedInjury(injuryId = injuryId)
    }
    return copy(selectedInjuries = updated)
}

/** Sets the status of an already-selected injury; a no-op for anything else. */
private fun OnboardingDraft.withStatus(
    injuryId: InjuryId,
    status: InjuryStatus,
): OnboardingDraft {
    val existing = selectedInjuries.firstOrNull { it.injuryId == injuryId } ?: return this
    return copy(selectedInjuries = selectedInjuries - existing + existing.copy(status = status))
}
