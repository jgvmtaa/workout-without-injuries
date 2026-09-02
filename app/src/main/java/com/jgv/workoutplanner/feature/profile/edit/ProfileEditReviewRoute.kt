package com.jgv.workoutplanner.feature.profile.edit

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jgv.workoutplanner.R
import com.jgv.workoutplanner.feature.onboarding.review.ProfileReviewEvent
import com.jgv.workoutplanner.feature.onboarding.review.ProfileReviewScreen

/**
 * Edit Review — final destination for all edit flows (Phase 6 §6.5).
 * Reuses ProfileReviewScreen but saving uses SaveProfileEditsUseCase.
 * After saving, returns to Profile without clearing back stack or re-entering onboarding.
 */
@Composable
fun ProfileEditReviewRoute(
    onFinishedToProfile: () -> Unit,
    onBack: () -> Unit,
    onCancelToProfile: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileEditReviewViewModel = hiltViewModel(),
    sessionViewModel: ProfileEditSessionViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isFinished by viewModel.isFinished.collectAsStateWithLifecycle()

    LaunchedEffect(isFinished) {
        if (isFinished) onFinishedToProfile()
    }
    LaunchedEffect(sessionViewModel) {
        sessionViewModel.effects.collect { effect ->
            when (effect) {
                ProfileEditNavigationEffect.ReturnToProfile -> onCancelToProfile()
                ProfileEditNavigationEffect.NavigateBack -> onBack()
            }
        }
    }
    BackHandler(onBack = onBack)

    ProfileReviewScreen(
        state = state,
        onEvent = { event ->
            when (event) {
                ProfileReviewEvent.Back -> onBack()
                else -> viewModel.onEvent(event)
            }
        },
        modifier = modifier,
        onCancel = sessionViewModel::cancel,
        continueLabel = stringResource(R.string.profile_action_save),
    )
}
