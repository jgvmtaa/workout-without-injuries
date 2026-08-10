package com.jgv.workoutplanner.feature.onboarding.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jgv.workoutplanner.core.WhileUiSubscribed
import com.jgv.workoutplanner.core.ui.labelRes
import com.jgv.workoutplanner.data.catalog.MovementLimitationCatalog
import com.jgv.workoutplanner.domain.model.Equipment
import com.jgv.workoutplanner.domain.model.OnboardingDraft
import com.jgv.workoutplanner.domain.repository.InjuryRepository
import com.jgv.workoutplanner.domain.repository.ProfileRepository
import com.jgv.workoutplanner.domain.usecase.DetermineWorkoutSplitUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Summarises the draft and turns it into a saved `UserProfile` (README §3, §4, §20).
 *
 * ## Why finishing is not just navigation
 * Every other onboarding step writes as the user taps, so its Continue button is pure
 * navigation. This one is the write: it assembles the profile, saves it, and only then
 * may the app move to Home — arriving there first would show a home screen with no
 * profile behind it. [isFinished] is the signal the route waits on.
 */
@HiltViewModel
class ProfileReviewViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val determineWorkoutSplit: DetermineWorkoutSplitUseCase,
    private val injuryRepository: InjuryRepository,
) : ViewModel() {

    private val _isFinished = MutableStateFlow(false)

    /** Flips once the profile is durably saved. The route navigates on it. */
    val isFinished: StateFlow<Boolean> = _isFinished.asStateFlow()

    val uiState: StateFlow<ProfileReviewUiState> =
        profileRepository.onboardingDraft
            .map { draft -> draft.toUiState() }
            .stateIn(
                scope = viewModelScope,
                started = WhileUiSubscribed,
                initialValue = ProfileReviewUiState(),
            )

    fun onEvent(event: ProfileReviewEvent) {
        when (event) {
            ProfileReviewEvent.Confirm -> confirm()
            // Navigation intent; handled by the route.
            ProfileReviewEvent.Back -> Unit
        }
    }

    private fun confirm() {
        viewModelScope.launch {
            // DataStore always emits the stored value first, so this reads what is on
            // disk right now rather than whatever the screen last rendered.
            val draft = profileRepository.onboardingDraft.first()
            val split = determineWorkoutSplit(draft.daysPerWeek ?: return@launch)
            // Null when the draft is incomplete. The finish button is disabled in that
            // case, so this is a guard against a race, not an expected path — and doing
            // nothing is the right response to it.
            val profile = draft.toUserProfile(split) ?: return@launch

            profileRepository.saveProfile(profile)
            _isFinished.value = true
        }
    }

    private fun OnboardingDraft.toUiState(): ProfileReviewUiState {
        val selectedIds = selectedInjuries.associateBy { it.injuryId }

        return ProfileReviewUiState(
            isLoading = false,
            isComplete = isComplete,
            goalRes = goal?.labelRes,
            experienceRes = experienceLevel?.labelRes,
            daysPerWeek = daysPerWeek,
            sessionDurationMinutes = sessionDurationMinutes,
            splitRes = daysPerWeek?.let { determineWorkoutSplit(it).labelRes },
            // Enum order, so the list reads the same way every time it is shown.
            equipment = Equipment.entries
                .filter { it in availableEquipment }
                .map { it.labelRes },
            // Catalog order rather than selection order, for the same reason.
            injuries = injuryRepository.getAllInjuries()
                .mapNotNull { definition ->
                    val selected = selectedIds[definition.id] ?: return@mapNotNull null
                    ReviewInjuryUiModel(
                        nameRes = definition.nameRes,
                        statusRes = selected.status.labelRes,
                    )
                },
            limitations = MovementLimitationCatalog.limitations
                .filter { it.id in confirmedLimitations }
                .map { it.nameRes },
        )
    }
}
