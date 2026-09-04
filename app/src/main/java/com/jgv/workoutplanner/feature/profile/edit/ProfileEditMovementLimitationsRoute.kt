package com.jgv.workoutplanner.feature.profile.edit

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.toRoute
import com.jgv.workoutplanner.feature.onboarding.limitations.MovementLimitationsEvent
import com.jgv.workoutplanner.feature.onboarding.limitations.MovementLimitationsScreen
import com.jgv.workoutplanner.feature.onboarding.limitations.MovementLimitationsViewModel
import com.jgv.workoutplanner.navigation.AppRoute
import androidx.compose.runtime.getValue
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.ViewModel

/**
 * Edit Movement Limitations — may be first destination (origin=Profile) or second (origin=InjuryHistory).
 * Uses typed route origin to decide Back behavior per spec:
 * - origin=Profile: first destination, Back = Cancel (reset draft).
 * - origin=InjuryHistory: intermediate, Back navigates within flow without resetting.
 */
@Composable
fun ProfileEditMovementLimitationsRoute(
    onContinueToReview: () -> Unit,
    onCancelToProfile: () -> Unit,
    onBackToInjuries: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MovementLimitationsViewModel = hiltViewModel(),
    sessionViewModel: ProfileEditSessionViewModel = hiltViewModel(),
    originViewModel: ProfileEditMovementOriginViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val origin = originViewModel.origin

    LaunchedEffect(sessionViewModel) {
        sessionViewModel.effects.collect { effect ->
            when (effect) {
                ProfileEditNavigationEffect.ReturnToProfile -> onCancelToProfile()
                ProfileEditNavigationEffect.NavigateBack -> onBackToInjuries()
            }
        }
    }
    BackHandler { sessionViewModel.backFromMovementLimitations(origin) }

    MovementLimitationsScreen(
        state = state,
        onEvent = { event ->
            when (event) {
                MovementLimitationsEvent.Continue -> onContinueToReview()
                MovementLimitationsEvent.Back -> sessionViewModel.backFromMovementLimitations(origin)
                else -> viewModel.onEvent(event)
            }
        },
        modifier = modifier,
        onCancel = sessionViewModel::cancel,
    )
}

@HiltViewModel
class ProfileEditMovementOriginViewModel internal constructor(
    val origin: AppRoute.ProfileEditOrigin,
) : ViewModel() {
    @Inject
    constructor(savedStateHandle: SavedStateHandle) : this(
        origin = savedStateHandle.toRoute<AppRoute.ProfileEditMovementLimitations>().origin,
    )
}
