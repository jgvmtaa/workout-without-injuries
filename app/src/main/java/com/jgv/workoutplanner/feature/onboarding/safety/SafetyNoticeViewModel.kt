package com.jgv.workoutplanner.feature.onboarding.safety

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jgv.workoutplanner.core.WhileUiSubscribed
import com.jgv.workoutplanner.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Holds the safety acknowledgement (spec §4.2, §20, §21).
 *
 * ## Why the write happens on toggle, not on continue
 * Persisting when Continue is pressed would mean the write races the navigation that
 * follows it. Writing on every toggle removes the race: by the time the button is
 * enabled the acknowledgement is already on disk, and Continue is pure navigation. Every
 * onboarding screen here works this way.
 *
 * The state is derived from the repository rather than held separately, so the stored
 * draft stays the single source of truth and a write that fails cannot leave the UI
 * claiming otherwise.
 */
@HiltViewModel
class SafetyNoticeViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    val uiState: StateFlow<SafetyNoticeUiState> =
        profileRepository.onboardingDraft
            .map { draft ->
                SafetyNoticeUiState(
                    isLoading = false,
                    isAcknowledged = draft.hasAcceptedSafetyNotice,
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = WhileUiSubscribed,
                initialValue = SafetyNoticeUiState(),
            )

    fun onEvent(event: SafetyNoticeEvent) {
        when (event) {
            is SafetyNoticeEvent.SetAcknowledged -> setAcknowledged(event.acknowledged)
            // Navigation intents; handled by the route (see SafetyNoticeEvent).
            SafetyNoticeEvent.Continue, SafetyNoticeEvent.Back -> Unit
        }
    }

    private fun setAcknowledged(acknowledged: Boolean) {
        viewModelScope.launch {
            profileRepository.updateDraft { it.copy(hasAcceptedSafetyNotice = acknowledged) }
        }
    }
}
