package com.jgv.workoutplanner.feature.profile.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jgv.workoutplanner.domain.repository.ProfileRepository
import com.jgv.workoutplanner.navigation.AppRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

sealed interface ProfileEditNavigationEffect {
    data object ReturnToProfile : ProfileEditNavigationEffect
    data object NavigateBack : ProfileEditNavigationEffect
}

@HiltViewModel
class ProfileEditSessionViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
) : ViewModel() {
    private val _effects = Channel<ProfileEditNavigationEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun cancel() {
        viewModelScope.launch {
            profileRepository.resetDraftFromProfile()
            _effects.send(ProfileEditNavigationEffect.ReturnToProfile)
        }
    }

    fun backFromMovementLimitations(origin: AppRoute.ProfileEditOrigin) {
        if (origin == AppRoute.ProfileEditOrigin.Profile) {
            cancel()
        } else {
            viewModelScope.launch {
                _effects.send(ProfileEditNavigationEffect.NavigateBack)
            }
        }
    }
}
