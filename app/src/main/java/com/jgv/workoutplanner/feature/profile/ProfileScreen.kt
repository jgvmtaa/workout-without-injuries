package com.jgv.workoutplanner.feature.profile

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.jgv.workoutplanner.R
import com.jgv.workoutplanner.core.designsystem.AppTheme
import com.jgv.workoutplanner.core.ui.PlaceholderAction
import com.jgv.workoutplanner.core.ui.PlaceholderScreen

/**
 * Saved profile: preferences, injury history and confirmed limitations, each
 * editable after onboarding (README §13, §17). Placeholder until Phase 6.
 */
@Composable
fun ProfileScreen(
    onEditPreferences: () -> Unit,
    onEditInjuries: () -> Unit,
    onEditLimitations: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PlaceholderScreen(
        title = stringResource(R.string.screen_profile),
        modifier = modifier,
        onBack = onBack,
        actions = listOf(
            PlaceholderAction(
                label = stringResource(R.string.action_edit_preferences),
                onClick = onEditPreferences,
            ),
            PlaceholderAction(
                label = stringResource(R.string.action_edit_injuries),
                onClick = onEditInjuries,
            ),
            PlaceholderAction(
                label = stringResource(R.string.action_edit_limitations),
                onClick = onEditLimitations,
            ),
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    AppTheme {
        ProfileScreen(
            onEditPreferences = {},
            onEditInjuries = {},
            onEditLimitations = {},
            onBack = {},
        )
    }
}
