package com.jgv.workoutplanner.feature.onboarding.welcome

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.jgv.workoutplanner.R
import com.jgv.workoutplanner.core.designsystem.AppTheme
import com.jgv.workoutplanner.core.designsystem.Dimens
import com.jgv.workoutplanner.core.ui.OnboardingScaffold
import com.jgv.workoutplanner.core.ui.SectionHeader

/**
 * Onboarding entry point (spec §4.1).
 *
 * No ViewModel and no `UiState`, unlike every other onboarding screen: there is nothing
 * to collect here and nothing to remember. spec §20's pattern exists to keep business
 * state out of composables, and adding an empty state holder to satisfy the shape of it
 * would be ceremony rather than structure.
 *
 * The disclaimer appears here as well as on the safety screen. That is deliberate
 * duplication — the first sentence a user reads should not oversell what the app is
 * (spec §2).
 */
@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit,
    onReviewSafety: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OnboardingScaffold(
        title = stringResource(R.string.screen_welcome),
        continueLabel = stringResource(R.string.action_get_started),
        onContinue = onGetStarted,
        modifier = modifier,
    ) {
        Text(
            text = stringResource(R.string.welcome_headline),
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = stringResource(R.string.welcome_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        SectionHeader(
            title = stringResource(R.string.welcome_steps_title),
            modifier = Modifier.padding(top = Dimens.SpacingSmall),
        )
        WelcomeStep(text = stringResource(R.string.welcome_step_preferences))
        WelcomeStep(text = stringResource(R.string.welcome_step_injuries))
        WelcomeStep(text = stringResource(R.string.welcome_step_limitations))

        TextButton(
            onClick = onReviewSafety,
            modifier = Modifier.padding(top = Dimens.SpacingSmall),
        ) {
            Text(text = stringResource(R.string.action_review_safety))
        }
    }
}

@Composable
private fun WelcomeStep(
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "•",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = Dimens.SpacingSmall),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WelcomeScreenPreview() {
    AppTheme {
        WelcomeScreen(onGetStarted = {}, onReviewSafety = {})
    }
}
