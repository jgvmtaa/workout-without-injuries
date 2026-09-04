package com.jgv.workoutplanner.feature.profile.edit

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jgv.workoutplanner.feature.onboarding.preferences.PreferencesEvent
import com.jgv.workoutplanner.feature.onboarding.preferences.PreferencesScreen
import com.jgv.workoutplanner.feature.onboarding.preferences.PreferencesViewModel

/**
 * Edit Preferences — first destination when origin = Profile (Phase 6 §6.5).
 * Reuses PreferencesScreen composable with edit-specific navigation.
 * Treats Back from first destination as Cancel: reset draft before returning to Profile.
 */
@Composable
fun ProfileEditPreferencesRoute(
    onContinueToReview: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PreferencesViewModel = hiltViewModel(),
    sessionViewModel: ProfileEditSessionViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(sessionViewModel) {
        sessionViewModel.effects.collect { effect ->
            when (effect) {
                ProfileEditNavigationEffect.ReturnToProfile -> onCancel()
                ProfileEditNavigationEffect.NavigateBack -> Unit
            }
        }
    }
    BackHandler(onBack = sessionViewModel::cancel)

    PreferencesScreen(
        state = state,
        onEvent = { event ->
            when (event) {
                PreferencesEvent.Continue -> onContinueToReview()
                PreferencesEvent.Back -> sessionViewModel.cancel()
                else -> viewModel.onEvent(event)
            }
        },
        modifier = modifier,
        onCancel = sessionViewModel::cancel,
    )
}
