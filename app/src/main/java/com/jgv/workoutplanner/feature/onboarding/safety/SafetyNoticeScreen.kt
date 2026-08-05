package com.jgv.workoutplanner.feature.onboarding.safety

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.jgv.workoutplanner.R
import com.jgv.workoutplanner.core.designsystem.AppTheme
import com.jgv.workoutplanner.core.ui.PlaceholderAction
import com.jgv.workoutplanner.core.ui.PlaceholderScreen

/**
 * Safety acknowledgement (README §4.2). Placeholder until Phase 3 — the acknowledgement
 * text and the persisted `hasAcceptedSafetyNotice` flag arrive with that phase.
 */
@Composable
fun SafetyNoticeScreen(
    onAccept: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PlaceholderScreen(
        title = stringResource(R.string.screen_safety_notice),
        modifier = modifier,
        onBack = onBack,
        actions = listOf(
            PlaceholderAction(
                label = stringResource(R.string.action_accept_safety),
                onClick = onAccept,
            ),
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun SafetyNoticeScreenPreview() {
    AppTheme {
        SafetyNoticeScreen(onAccept = {}, onBack = {})
    }
}
