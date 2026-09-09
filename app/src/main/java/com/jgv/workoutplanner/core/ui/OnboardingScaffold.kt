package com.jgv.workoutplanner.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.jgv.workoutplanner.R
import com.jgv.workoutplanner.core.designsystem.AppTheme
import com.jgv.workoutplanner.core.designsystem.Dimens

/**
 * The shape every onboarding step shares: a title bar, scrolling content, and one
 * primary action pinned to the bottom (README §4.1–§4.5).
 *
 * Pinned rather than inline because the content of these screens varies from a
 * paragraph to twenty-seven checkboxes, and a Continue button that is sometimes
 * on-screen and sometimes three scrolls away is the kind of inconsistency users read as
 * "the app is broken". Keeping it in the scaffold also means [continueEnabled] is the
 * single place a step says whether it is answered — see each screen's `canContinue`.
 */
@Composable
fun OnboardingScaffold(
    title: String,
    continueLabel: String,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    onCancel: (() -> Unit)? = null,
    continueEnabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    AppScaffold(
        title = title,
        modifier = modifier,
        onBack = onBack,
        actions = {
            if (onCancel != null) {
                TextButton(onClick = onCancel) {
                    Text(text = stringResource(R.string.profile_action_cancel))
                }
            }
        },
        bottomBar = {
            Surface(tonalElevation = Dimens.BottomBarElevation) {
                Button(
                    onClick = onContinue,
                    enabled = continueEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(Dimens.ScreenPadding)
                        // Material3 buttons default to 40.dp; the Phase 7 accessibility
                        // bar is 48.dp (Dimens.MinTouchTarget, README §27 Phase 7).
                        .heightIn(min = Dimens.MinTouchTarget),
                ) {
                    Text(text = continueLabel)
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(
                    start = Dimens.ScreenPadding,
                    end = Dimens.ScreenPadding,
                    top = Dimens.SpacingSmall,
                    bottom = Dimens.SpacingExtraLarge,
                ),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingMedium),
            content = content,
        )
    }
}

/**
 * Introductory copy for a step: what it is asking and, where it matters, why.
 *
 * Rendered as one merged block so a screen reader announces the question and its
 * explanation together rather than as two unrelated stops.
 */
@Composable
fun StepIntroduction(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.fillMaxWidth(),
    )
}

@Preview(showBackground = true)
@Composable
private fun OnboardingScaffoldPreview() {
    AppTheme {
        OnboardingScaffold(
            title = "Training preferences",
            continueLabel = stringResource(R.string.action_continue),
            onContinue = {},
            onBack = {},
        ) {
            StepIntroduction(text = "Tell us how you want to train.")
            Text(text = "Content goes here")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingScaffoldDisabledPreview() {
    AppTheme {
        OnboardingScaffold(
            title = "Training preferences",
            continueLabel = stringResource(R.string.action_continue),
            onContinue = {},
            onBack = {},
            continueEnabled = false,
        ) {
            StepIntroduction(text = "Answer every question to continue.")
        }
    }
}
