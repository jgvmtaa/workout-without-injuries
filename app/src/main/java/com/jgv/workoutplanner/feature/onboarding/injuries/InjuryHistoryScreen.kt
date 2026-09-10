package com.jgv.workoutplanner.feature.onboarding.injuries

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.jgv.workoutplanner.core.ui.SectionHeader
import com.jgv.workoutplanner.core.ui.SingleChoiceChipRow
import com.jgv.workoutplanner.core.ui.StepIntroduction
import com.jgv.workoutplanner.core.ui.labelRes
import com.jgv.workoutplanner.domain.model.BodyRegion
import com.jgv.workoutplanner.domain.model.InjuryId
import com.jgv.workoutplanner.domain.model.InjuryStatus

/** Stateful entry point for the injury history step (spec §20). */
@Composable
fun InjuryHistoryRoute(
    onContinue: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InjuryHistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    InjuryHistoryScreen(
        state = state,
        onEvent = { event ->
            when (event) {
                InjuryHistoryEvent.Continue -> onContinue()
                InjuryHistoryEvent.Back -> onBack()
                else -> viewModel.onEvent(event)
            }
        },
        modifier = modifier,
    )
}

/**
 * Injury history grouped by body region (spec §4.4).
 *
 * The status chips appear only under an injury that is selected. Asking "how is it now?"
 * about something the user has not claimed would be noise, and it keeps the default list
 * scannable — most of it will be left untouched.
 */
@Composable
fun InjuryHistoryScreen(
    state: InjuryHistoryUiState,
    onEvent: (InjuryHistoryEvent) -> Unit,
    modifier: Modifier = Modifier,
    onCancel: (() -> Unit)? = null,
) {
    if (state.isLoading) {
        LoadingContent(modifier = modifier)
        return
    }

    OnboardingScaffold(
        title = stringResource(R.string.screen_injury_history),
        continueLabel = stringResource(R.string.action_continue),
        onContinue = { onEvent(InjuryHistoryEvent.Continue) },
        modifier = modifier,
        onBack = { onEvent(InjuryHistoryEvent.Back) },
        onCancel = onCancel,
        continueEnabled = state.canContinue,
    ) {
        StepIntroduction(text = stringResource(R.string.injuries_intro))

        if (state.selectedCount > 0) {
            Text(
                text = stringResource(R.string.injuries_selected_count, state.selectedCount),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        state.injuryGroups.forEach { group ->
            SectionHeader(title = stringResource(group.regionNameRes))

            group.injuries.forEach { injury ->
                val status = state.selectedInjuries[injury.id]

                CheckboxRow(
                    label = stringResource(injury.nameRes),
                    checked = status != null,
                    onCheckedChange = { onEvent(InjuryHistoryEvent.ToggleInjury(injury.id)) },
                )

                if (status != null) {
                    InjuryStatusPicker(
                        injuryId = injury.id,
                        status = status,
                        onEvent = onEvent,
                    )
                }
            }
        }
    }
}

@Composable
private fun InjuryStatusPicker(
    injuryId: InjuryId,
    status: InjuryStatus,
    onEvent: (InjuryHistoryEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = Dimens.SpacingExtraLarge, bottom = Dimens.SpacingSmall),
    ) {
        Text(
            text = stringResource(R.string.injuries_status_title),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = Dimens.SpacingExtraSmall),
        )
        SingleChoiceChipRow(
            options = InjuryStatus.entries,
            selected = status,
            onSelect = { onEvent(InjuryHistoryEvent.SetStatus(injuryId, it)) },
            label = { stringResource(it.labelRes) },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InjuryHistoryScreenPreview() {
    AppTheme {
        InjuryHistoryScreen(
            state = InjuryHistoryUiState(
                isLoading = false,
                injuryGroups = PREVIEW_GROUPS,
            ),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InjuryHistoryScreenSelectedPreview() {
    AppTheme {
        InjuryHistoryScreen(
            state = InjuryHistoryUiState(
                isLoading = false,
                injuryGroups = PREVIEW_GROUPS,
                selectedInjuries = mapOf(InjuryId.KNEE_ACL to InjuryStatus.RECOVERING),
            ),
            onEvent = {},
        )
    }
}

private val PREVIEW_GROUPS = listOf(
    InjuryGroupUiModel(
        region = BodyRegion.KNEE,
        regionNameRes = R.string.body_region_knee,
        injuries = listOf(
            InjuryUiModel(InjuryId.KNEE_ACL, R.string.injury_knee_acl),
            InjuryUiModel(InjuryId.KNEE_MENISCUS, R.string.injury_knee_meniscus),
            InjuryUiModel(InjuryId.KNEE_UNDIAGNOSED_PAIN, R.string.injury_knee_undiagnosed_pain),
        ),
    ),
    InjuryGroupUiModel(
        region = BodyRegion.SHOULDER,
        regionNameRes = R.string.body_region_shoulder,
        injuries = listOf(
            InjuryUiModel(InjuryId.SHOULDER_GENERAL, R.string.injury_shoulder_general),
            InjuryUiModel(InjuryId.SHOULDER_IMPINGEMENT, R.string.injury_shoulder_impingement),
        ),
    ),
)
