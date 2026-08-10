package com.jgv.workoutplanner.feature.onboarding.limitations

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.jgv.workoutplanner.core.ui.StepIntroduction
import com.jgv.workoutplanner.domain.model.LimitationGroup
import com.jgv.workoutplanner.domain.model.MovementLimitation

/** Stateful entry point for the limitations step (README §20). */
@Composable
fun MovementLimitationsRoute(
    onContinue: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MovementLimitationsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    MovementLimitationsScreen(
        state = state,
        onEvent = { event ->
            when (event) {
                MovementLimitationsEvent.Continue -> onContinue()
                MovementLimitationsEvent.Back -> onBack()
                else -> viewModel.onEvent(event)
            }
        },
        modifier = modifier,
    )
}

/**
 * Suggested and manual movement limitations (README §4.5).
 *
 * The screen this whole flow exists for. Suggestions appear first, unticked, under copy
 * that says outright they have not been applied — because a suggestion the user did not
 * read is indistinguishable, from their side, from the app deciding for them. Everything
 * else is browsable below, so a limitation from a clinician that no selected injury
 * implies can still be added (README §4.5).
 */
@Composable
fun MovementLimitationsScreen(
    state: MovementLimitationsUiState,
    onEvent: (MovementLimitationsEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.isLoading) {
        LoadingContent(modifier = modifier)
        return
    }

    OnboardingScaffold(
        title = stringResource(R.string.screen_movement_limitations),
        continueLabel = stringResource(R.string.action_continue),
        onContinue = { onEvent(MovementLimitationsEvent.Continue) },
        modifier = modifier,
        onBack = { onEvent(MovementLimitationsEvent.Back) },
        continueEnabled = state.canContinue,
    ) {
        StepIntroduction(text = stringResource(R.string.limitations_intro))

        if (state.confirmedCount > 0) {
            Text(
                text = stringResource(R.string.limitations_selected_count, state.confirmedCount),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        if (state.hasSuggestions) {
            SuggestionsCard {
                SectionHeader(
                    title = stringResource(R.string.limitations_suggested_title),
                    description = stringResource(R.string.limitations_suggested_description),
                )
                state.suggested.forEach { limitation ->
                    LimitationRow(
                        limitation = limitation,
                        confirmed = limitation.id in state.confirmed,
                        onEvent = onEvent,
                    )
                }
            }
        } else {
            SuggestionsCard {
                SectionHeader(
                    title = stringResource(R.string.limitations_no_suggestions_title),
                    description = stringResource(R.string.limitations_no_suggestions_description),
                )
            }
        }

        SectionHeader(
            title = stringResource(R.string.limitations_all_title),
            description = stringResource(R.string.limitations_all_description),
            modifier = Modifier.padding(top = Dimens.SpacingSmall),
        )

        state.otherGroups.forEach { group ->
            Text(
                text = stringResource(group.groupNameRes),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Dimens.SpacingSmall),
            )
            group.limitations.forEach { limitation ->
                LimitationRow(
                    limitation = limitation,
                    confirmed = limitation.id in state.confirmed,
                    onEvent = onEvent,
                )
            }
        }
    }
}

@Composable
private fun LimitationRow(
    limitation: LimitationUiModel,
    confirmed: Boolean,
    onEvent: (MovementLimitationsEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    CheckboxRow(
        label = stringResource(limitation.nameRes),
        checked = confirmed,
        onCheckedChange = { onEvent(MovementLimitationsEvent.SetConfirmed(limitation.id, it)) },
        modifier = modifier,
    )
}

/** Sets the suggestions apart from the browsable list without implying they are active. */
@Composable
private fun SuggestionsCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(Dimens.SpacingMedium)) {
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MovementLimitationsScreenWithSuggestionsPreview() {
    AppTheme {
        MovementLimitationsScreen(
            state = MovementLimitationsUiState(
                isLoading = false,
                suggested = listOf(
                    LimitationUiModel(
                        MovementLimitation.AVOID_JUMPING,
                        R.string.limitation_avoid_jumping,
                    ),
                    LimitationUiModel(
                        MovementLimitation.AVOID_RAPID_DIRECTION_CHANGE,
                        R.string.limitation_avoid_rapid_direction_change,
                    ),
                ),
                otherGroups = PREVIEW_GROUPS,
                confirmed = setOf(MovementLimitation.AVOID_JUMPING),
            ),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MovementLimitationsScreenNoSuggestionsPreview() {
    AppTheme {
        MovementLimitationsScreen(
            state = MovementLimitationsUiState(
                isLoading = false,
                otherGroups = PREVIEW_GROUPS,
            ),
            onEvent = {},
        )
    }
}

private val PREVIEW_GROUPS = listOf(
    LimitationGroupUiModel(
        group = LimitationGroup.KNEE,
        groupNameRes = R.string.limitation_group_knee,
        limitations = listOf(
            LimitationUiModel(
                MovementLimitation.AVOID_DEEP_KNEE_FLEXION,
                R.string.limitation_avoid_deep_knee_flexion,
            ),
            LimitationUiModel(
                MovementLimitation.AVOID_KNEELING,
                R.string.limitation_avoid_kneeling,
            ),
        ),
    ),
    LimitationGroupUiModel(
        group = LimitationGroup.SHOULDER,
        groupNameRes = R.string.limitation_group_shoulder,
        limitations = listOf(
            LimitationUiModel(
                MovementLimitation.AVOID_OVERHEAD_PRESSING,
                R.string.limitation_avoid_overhead_pressing,
            ),
        ),
    ),
)
