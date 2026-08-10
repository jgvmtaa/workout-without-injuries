package com.jgv.workoutplanner.feature.onboarding.limitations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jgv.workoutplanner.core.WhileUiSubscribed
import com.jgv.workoutplanner.core.ui.labelRes
import com.jgv.workoutplanner.data.catalog.MovementLimitationCatalog
import com.jgv.workoutplanner.domain.model.MovementLimitation
import com.jgv.workoutplanner.domain.model.MovementLimitationDefinition
import com.jgv.workoutplanner.domain.model.OnboardingDraft
import com.jgv.workoutplanner.domain.repository.ProfileRepository
import com.jgv.workoutplanner.domain.usecase.GetSuggestedLimitationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Presents suggested limitations and records the ones the user confirms
 * (README §4.5, §20).
 *
 * ## What this ViewModel deliberately does not do
 * It never writes a suggestion into `confirmedLimitations`. Suggestions are recomputed
 * from the draft's injuries on every emission and exist only in the UI state; the draft
 * gains a limitation solely through [MovementLimitationsEvent.SetConfirmed], which is
 * only ever sent by a user tapping a checkbox. That is the whole of README §2 expressed
 * as code, and the test in `MovementLimitationsViewModelTest` pins it.
 *
 * A limitation the user confirmed stays confirmed if they go back and remove the injury
 * that suggested it. It simply moves out of the suggested section and into its group,
 * still ticked — the app has no business un-answering a question the user answered.
 */
@HiltViewModel
class MovementLimitationsViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val getSuggestedLimitations: GetSuggestedLimitationsUseCase,
) : ViewModel() {

    val uiState: StateFlow<MovementLimitationsUiState> =
        profileRepository.onboardingDraft
            .map { draft -> draft.toUiState() }
            .stateIn(
                scope = viewModelScope,
                started = WhileUiSubscribed,
                initialValue = MovementLimitationsUiState(),
            )

    fun onEvent(event: MovementLimitationsEvent) {
        when (event) {
            is MovementLimitationsEvent.SetConfirmed ->
                setConfirmed(event.limitation, event.confirmed)
            // Navigation intents; handled by the route.
            MovementLimitationsEvent.Continue, MovementLimitationsEvent.Back -> Unit
        }
    }

    private fun setConfirmed(limitation: MovementLimitation, confirmed: Boolean) {
        viewModelScope.launch {
            profileRepository.updateDraft { draft ->
                val updated = if (confirmed) {
                    draft.confirmedLimitations + limitation
                } else {
                    draft.confirmedLimitations - limitation
                }
                draft.copy(confirmedLimitations = updated)
            }
        }
    }

    private fun OnboardingDraft.toUiState(): MovementLimitationsUiState {
        val suggestions = getSuggestedLimitations(selectedInjuries)

        return MovementLimitationsUiState(
            isLoading = false,
            suggested = MovementLimitationCatalog.limitations
                .filter { it.id in suggestions }
                .map { it.toUiModel() },
            otherGroups = MovementLimitationCatalog.groupedForDisplay()
                .mapNotNull { (group, definitions) ->
                    val remaining = definitions.filterNot { it.id in suggestions }
                    if (remaining.isEmpty()) {
                        // Every limitation in this group was suggested, so the group has
                        // nothing left to browse. Dropped rather than shown empty.
                        null
                    } else {
                        LimitationGroupUiModel(
                            group = group,
                            groupNameRes = group.labelRes,
                            limitations = remaining.map { it.toUiModel() },
                        )
                    }
                },
            confirmed = confirmedLimitations,
        )
    }
}

private fun MovementLimitationDefinition.toUiModel() =
    LimitationUiModel(id = id, nameRes = nameRes)
