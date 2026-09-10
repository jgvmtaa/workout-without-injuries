package com.jgv.workoutplanner.feature.onboarding.safety

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jgv.workoutplanner.R
import com.jgv.workoutplanner.core.designsystem.AppTheme
import com.jgv.workoutplanner.core.designsystem.Dimens
import com.jgv.workoutplanner.core.ui.CheckboxRow
import com.jgv.workoutplanner.core.ui.LoadingContent
import com.jgv.workoutplanner.core.ui.OnboardingScaffold
import com.jgv.workoutplanner.core.ui.StepIntroduction

/**
 * Stateful entry point for the safety notice (spec §20).
 *
 * Navigation events are mapped here rather than in the ViewModel: the screen keeps one
 * event channel, and the route decides what "continue" means.
 */
@Composable
fun SafetyNoticeRoute(
    onContinue: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SafetyNoticeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    SafetyNoticeScreen(
        state = state,
        onEvent = { event ->
            when (event) {
                SafetyNoticeEvent.Continue -> onContinue()
                SafetyNoticeEvent.Back -> onBack()
                else -> viewModel.onEvent(event)
            }
        },
        modifier = modifier,
    )
}

/**
 * Safety acknowledgement (spec §4.2).
 *
 * All four points are shown as text and covered by one explicit checkbox. Four separate
 * checkboxes would let someone proceed having ticked three of them, which is a state the
 * app has no answer for — the acknowledgement is all-or-nothing, so it is one control.
 */
@Composable
fun SafetyNoticeScreen(
    state: SafetyNoticeUiState,
    onEvent: (SafetyNoticeEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.isLoading) {
        LoadingContent(modifier = modifier)
        return
    }

    OnboardingScaffold(
        title = stringResource(R.string.screen_safety_notice),
        continueLabel = stringResource(R.string.action_accept_safety),
        onContinue = { onEvent(SafetyNoticeEvent.Continue) },
        modifier = modifier,
        onBack = { onEvent(SafetyNoticeEvent.Back) },
        continueEnabled = state.canContinue,
    ) {
        StepIntroduction(text = stringResource(R.string.safety_intro))

        state.acknowledgements.forEach { acknowledgement ->
            AcknowledgementRow(text = stringResource(acknowledgement))
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = Dimens.SpacingSmall))

        CheckboxRow(
            label = stringResource(R.string.safety_confirm_label),
            checked = state.isAcknowledged,
            onCheckedChange = { onEvent(SafetyNoticeEvent.SetAcknowledged(it)) },
        )
    }
}

@Composable
private fun AcknowledgementRow(
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
private fun SafetyNoticeScreenPreview() {
    AppTheme {
        SafetyNoticeScreen(
            state = SafetyNoticeUiState(isLoading = false),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SafetyNoticeScreenAcknowledgedPreview() {
    AppTheme {
        SafetyNoticeScreen(
            state = SafetyNoticeUiState(isLoading = false, isAcknowledged = true),
            onEvent = {},
        )
    }
}
