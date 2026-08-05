package com.jgv.workoutplanner.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jgv.workoutplanner.R
import com.jgv.workoutplanner.core.designsystem.AppTheme
import com.jgv.workoutplanner.core.ui.LoadingContent
import com.jgv.workoutplanner.core.ui.PlaceholderAction
import com.jgv.workoutplanner.core.ui.PlaceholderScreen

/**
 * Stateful entry point for the home destination (README §20).
 *
 * The route resolves the ViewModel and collects its state; the screen below stays
 * stateless and previewable. Navigation is expressed as lambdas — no `NavController`
 * reaches a screen composable.
 */
@Composable
fun HomeRoute(
    onOpenPlan: () -> Unit,
    onOpenExerciseLibrary: () -> Unit,
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        state = state,
        onOpenPlan = onOpenPlan,
        onOpenExerciseLibrary = onOpenExerciseLibrary,
        onOpenProfile = onOpenProfile,
        modifier = modifier,
    )
}

/** Post-onboarding start screen (README §16). Placeholder until Phase 5. */
@Composable
fun HomeScreen(
    state: HomeUiState,
    onOpenPlan: () -> Unit,
    onOpenExerciseLibrary: () -> Unit,
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.isLoading) {
        LoadingContent(modifier = modifier)
        return
    }

    PlaceholderScreen(
        title = stringResource(R.string.screen_home),
        modifier = modifier,
        actions = listOf(
            PlaceholderAction(
                label = stringResource(R.string.action_view_plan),
                onClick = onOpenPlan,
            ),
            PlaceholderAction(
                label = stringResource(R.string.action_browse_library),
                onClick = onOpenExerciseLibrary,
            ),
            PlaceholderAction(
                label = stringResource(R.string.action_open_profile),
                onClick = onOpenProfile,
            ),
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    AppTheme {
        HomeScreen(
            state = HomeUiState(),
            onOpenPlan = {},
            onOpenExerciseLibrary = {},
            onOpenProfile = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenLoadingPreview() {
    AppTheme {
        HomeScreen(
            state = HomeUiState(isLoading = true),
            onOpenPlan = {},
            onOpenExerciseLibrary = {},
            onOpenProfile = {},
        )
    }
}
