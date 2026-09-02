package com.jgv.workoutplanner.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jgv.workoutplanner.core.WhileUiSubscribed
import com.jgv.workoutplanner.core.ui.labelRes
import com.jgv.workoutplanner.data.catalog.MovementLimitationCatalog
import com.jgv.workoutplanner.domain.model.Equipment
import com.jgv.workoutplanner.domain.repository.InjuryRepository
import com.jgv.workoutplanner.domain.repository.ProfileRepository
import com.jgv.workoutplanner.domain.usecase.DetermineWorkoutSplitUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val injuryRepository: InjuryRepository,
    private val determineSplit: DetermineWorkoutSplitUseCase,
) : ViewModel() {

    private val _effects = Channel<ProfileEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    val uiState: StateFlow<ProfileUiState> = profileRepository.profile
        .map { profile ->
            if (profile == null) {
                ProfileUiState(isLoading = false, hasProfile = false)
            } else {
                val selectedIds = profile.selectedInjuries.associateBy { it.injuryId }
                ProfileUiState(
                    isLoading = false,
                    hasProfile = true,
                    goalRes = profile.goal.labelRes,
                    daysPerWeek = profile.daysPerWeek,
                    sessionDurationMinutes = profile.sessionDurationMinutes,
                    experienceRes = profile.experienceLevel.labelRes,
                    splitRes = determineSplit(profile.daysPerWeek).labelRes,
                    equipmentRes = Equipment.entries
                        .filter { it in profile.availableEquipment }
                        .map { it.labelRes },
                    injuries = injuryRepository.getAllInjuries().mapNotNull { def ->
                        val sel = selectedIds[def.id] ?: return@mapNotNull null
                        ProfileInjuryUiModel(
                            nameRes = def.nameRes,
                            statusRes = sel.status.labelRes,
                        )
                    },
                    limitationsRes = MovementLimitationCatalog.limitations
                        .filter { it.id in profile.movementLimitations }
                        .map { it.nameRes },
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = WhileUiSubscribed,
            initialValue = ProfileUiState(),
        )

    fun onEvent(event: ProfileEvent) {
        when (event) {
            ProfileEvent.EditPreferences -> {
                viewModelScope.launch {
                    profileRepository.resetDraftFromProfile()
                    _effects.send(ProfileEffect.NavigateToEditPreferences)
                }
            }
            ProfileEvent.EditInjuries -> {
                viewModelScope.launch {
                    profileRepository.resetDraftFromProfile()
                    _effects.send(ProfileEffect.NavigateToEditInjuries)
                }
            }
            ProfileEvent.EditLimitations -> {
                viewModelScope.launch {
                    profileRepository.resetDraftFromProfile()
                    _effects.send(ProfileEffect.NavigateToEditLimitations)
                }
            }
            ProfileEvent.Back -> Unit
        }
    }
}
