package com.jgv.workoutplanner.feature.profile

import androidx.annotation.StringRes

/**
 * Immutable state for Profile screen (Phase 6 §6.5, README §16–§17, §20).
 * Summaries derived only from saved profile, never from unfinished edit draft.
 */
data class ProfileUiState(
    val isLoading: Boolean = true,
    @StringRes val goalRes: Int? = null,
    val daysPerWeek: Int? = null,
    val sessionDurationMinutes: Int? = null,
    @StringRes val experienceRes: Int? = null,
    @StringRes val splitRes: Int? = null,
    val equipmentRes: List<Int> = emptyList(),
    val injuries: List<ProfileInjuryUiModel> = emptyList(),
    val limitationsRes: List<Int> = emptyList(),
    val hasProfile: Boolean = false,
)

data class ProfileInjuryUiModel(
    @StringRes val nameRes: Int,
    @StringRes val statusRes: Int,
)

sealed interface ProfileEvent {
    data object EditPreferences : ProfileEvent
    data object EditInjuries : ProfileEvent
    data object EditLimitations : ProfileEvent
    data object Back : ProfileEvent
}

sealed interface ProfileEffect {
    data object NavigateToEditPreferences : ProfileEffect
    data object NavigateToEditInjuries : ProfileEffect
    data object NavigateToEditLimitations : ProfileEffect
}
