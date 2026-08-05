package com.jgv.workoutplanner.feature.onboarding.welcome

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.jgv.workoutplanner.R
import com.jgv.workoutplanner.core.designsystem.AppTheme
import com.jgv.workoutplanner.core.ui.PlaceholderAction
import com.jgv.workoutplanner.core.ui.PlaceholderScreen

/** Onboarding entry point (README §4.1). Placeholder until Phase 3. */
@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit,
    onReviewSafety: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PlaceholderScreen(
        title = stringResource(R.string.screen_welcome),
        modifier = modifier,
        actions = listOf(
            PlaceholderAction(
                label = stringResource(R.string.action_get_started),
                onClick = onGetStarted,
            ),
            PlaceholderAction(
                label = stringResource(R.string.action_review_safety),
                onClick = onReviewSafety,
            ),
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun WelcomeScreenPreview() {
    AppTheme {
        WelcomeScreen(onGetStarted = {}, onReviewSafety = {})
    }
}
