package com.jgv.workoutplanner.feature.profile.edit

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
import com.jgv.workoutplanner.domain.usecase.SaveProfileEditsUseCase
import com.jgv.workoutplanner.feature.onboarding.review.ProfileReviewEvent
import com.jgv.workoutplanner.feature.onboarding.review.ProfileReviewUiState
import com.jgv.workoutplanner.feature.onboarding.review.ReviewInjuryUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileEditReviewViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val injuryRepository: InjuryRepository,
    private val determineSplit: DetermineWorkoutSplitUseCase,
    private val saveProfileEditsUseCase: SaveProfileEditsUseCase,
) : ViewModel() {

    private val _isFinished = MutableStateFlow(false)
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
            ProfileReviewEvent.Back -> Unit
        }
    }

    private fun confirm() {
        viewModelScope.launch {
            val draft = profileRepository.onboardingDraft.first()
            if (!draft.isComplete) return@launch
            saveProfileEditsUseCase(draft)
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
            splitRes = daysPerWeek?.let { determineSplit(it).labelRes },
            equipment = Equipment.entries
                .filter { it in availableEquipment }
                .map { it.labelRes },
            injuries = injuryRepository.getAllInjuries()
                .mapNotNull { def ->
                    val sel = selectedIds[def.id] ?: return@mapNotNull null
                    ReviewInjuryUiModel(nameRes = def.nameRes, statusRes = sel.status.labelRes)
                },
            limitations = MovementLimitationCatalog.limitations
                .filter { it.id in confirmedLimitations }
                .map { it.nameRes },
        )
    }
}
