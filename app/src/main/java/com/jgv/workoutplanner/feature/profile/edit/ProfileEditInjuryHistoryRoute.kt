package com.jgv.workoutplanner.feature.profile.edit

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jgv.workoutplanner.feature.onboarding.injuries.InjuryHistoryEvent
import com.jgv.workoutplanner.feature.onboarding.injuries.InjuryHistoryScreen
import com.jgv.workoutplanner.feature.onboarding.injuries.InjuryHistoryViewModel

/**
 * Edit Injury History — first destination of Injury flow (Phase 6 §6.5).
 * Short flow: InjuryHistory -> MovementLimitations(origin=InjuryHistory) -> Review.
 * Back from first destination is Cancel: reset draft then return to Profile.
 */
@Composable
fun ProfileEditInjuryHistoryRoute(
    onContinueToLimitations: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InjuryHistoryViewModel = hiltViewModel(),
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

    InjuryHistoryScreen(
        state = state,
        onEvent = { event ->
            when (event) {
                InjuryHistoryEvent.Continue -> onContinueToLimitations()
                InjuryHistoryEvent.Back -> sessionViewModel.cancel()
                else -> viewModel.onEvent(event)
            }
        },
        modifier = modifier,
        onCancel = sessionViewModel::cancel,
    )
}
