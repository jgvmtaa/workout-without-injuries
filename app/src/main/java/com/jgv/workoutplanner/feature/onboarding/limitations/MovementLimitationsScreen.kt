package com.jgv.workoutplanner.feature.onboarding.limitations

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.jgv.workoutplanner.R
import com.jgv.workoutplanner.core.designsystem.AppTheme
import com.jgv.workoutplanner.core.ui.PlaceholderAction
import com.jgv.workoutplanner.core.ui.PlaceholderScreen

/**
 * Suggested and manual movement limitations (README §4.5). Placeholder until Phase 3.
 *
 * The rule this screen exists to enforce — suggested limitations are never applied
 * silently, the user confirms each one — lands with the real implementation.
 */
@Composable
fun MovementLimitationsScreen(
    onContinue: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PlaceholderScreen(
        title = stringResource(R.string.screen_movement_limitations),
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
private fun MovementLimitationsScreenPreview() {
    AppTheme {
        MovementLimitationsScreen(onContinue = {}, onBack = {})
    }
}
