package com.jgv.workoutplanner.feature.onboarding.preferences

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.jgv.workoutplanner.R
import com.jgv.workoutplanner.core.designsystem.AppTheme
import com.jgv.workoutplanner.core.ui.PlaceholderAction
import com.jgv.workoutplanner.core.ui.PlaceholderScreen

/**
 * Goal, experience level, weekly schedule, session duration and equipment
 * (README §4.3). Placeholder until Phase 3.
 */
@Composable
fun PreferencesScreen(
    onContinue: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PlaceholderScreen(
        title = stringResource(R.string.screen_preferences),
        modifier = modifier,
        onBack = onBack,
        actions = listOf(
            PlaceholderAction(
                label = stringResource(R.string.action_continue),
                onClick = onContinue,
            ),
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun PreferencesScreenPreview() {
    AppTheme {
        PreferencesScreen(onContinue = {}, onBack = {})
    }
}
