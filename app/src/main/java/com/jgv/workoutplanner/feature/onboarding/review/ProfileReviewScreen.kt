package com.jgv.workoutplanner.feature.onboarding.review

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jgv.workoutplanner.R
import com.jgv.workoutplanner.core.designsystem.AppTheme
import com.jgv.workoutplanner.core.designsystem.Dimens
import com.jgv.workoutplanner.core.ui.LoadingContent
import com.jgv.workoutplanner.core.ui.OnboardingScaffold
import com.jgv.workoutplanner.core.ui.SectionHeader
import com.jgv.workoutplanner.core.ui.StepIntroduction

/**
 * Stateful entry point for the review step (spec §20).
 *
 * Unlike the other routes, this one waits: [onFinish] fires from a `LaunchedEffect` on
 * the ViewModel's finished signal, not from the button, so navigation cannot outrun the
 * profile write.
 */
@Composable
fun ProfileReviewRoute(
    onFinish: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileReviewViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isFinished by viewModel.isFinished.collectAsStateWithLifecycle()

    LaunchedEffect(isFinished) {
        if (isFinished) onFinish()
    }

    ProfileReviewScreen(
        state = state,
        onEvent = { event ->
            when (event) {
                ProfileReviewEvent.Back -> onBack()
                else -> viewModel.onEvent(event)
            }
        },
        modifier = modifier,
    )
}

/**
 * Final onboarding step: everything collected, in one place, before the first plan
 * (spec §3, §4).
 *
 * The limitations section carries a note that these are the only thing filtering
 * exercises. It is the last chance to correct the impression that selecting an injury
 * did something on its own (spec §2).
 */
@Composable
fun ProfileReviewScreen(
    state: ProfileReviewUiState,
    onEvent: (ProfileReviewEvent) -> Unit,
    modifier: Modifier = Modifier,
    onCancel: (() -> Unit)? = null,
    continueLabel: String = stringResource(R.string.action_finish_setup),
) {
    if (state.isLoading) {
        LoadingContent(modifier = modifier)
        return
    }

    OnboardingScaffold(
        title = stringResource(R.string.screen_profile_review),
        continueLabel = continueLabel,
        onContinue = { onEvent(ProfileReviewEvent.Confirm) },
        modifier = modifier,
        onBack = { onEvent(ProfileReviewEvent.Back) },
        onCancel = onCancel,
        continueEnabled = state.canContinue,
    ) {
        StepIntroduction(text = stringResource(R.string.review_intro))

        if (!state.isComplete) {
            Text(
                text = stringResource(R.string.review_incomplete),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }

        SectionHeader(title = stringResource(R.string.review_section_training))
        SummaryRow(
            label = stringResource(R.string.review_label_goal),
            value = state.goalRes?.let { stringResource(it) },
        )
        SummaryRow(
            label = stringResource(R.string.review_label_experience),
            value = state.experienceRes?.let { stringResource(it) },
        )
        SummaryRow(
            label = stringResource(R.string.review_label_days),
            value = state.daysPerWeek?.let {
                pluralStringResource(R.plurals.preferences_days_value, it, it)
            },
        )
        SummaryRow(
            label = stringResource(R.string.review_label_duration),
            value = state.sessionDurationMinutes?.let {
                stringResource(R.string.preferences_duration_value, it)
            },
        )
        SummaryRow(
            label = stringResource(R.string.review_label_split),
            value = state.splitRes?.let { stringResource(it) },
        )

        SectionHeader(
            title = stringResource(R.string.review_section_equipment),
            modifier = Modifier.padding(top = Dimens.SpacingSmall),
        )
        state.equipment.forEach { BulletLine(text = stringResource(it)) }

        SectionHeader(
            title = stringResource(R.string.review_section_injuries),
            modifier = Modifier.padding(top = Dimens.SpacingSmall),
        )
        if (state.injuries.isEmpty()) {
            EmptyLine(text = stringResource(R.string.review_no_injuries))
        } else {
            state.injuries.forEach { injury ->
                BulletLine(
                    text = stringResource(
                        R.string.review_injury_with_status,
                        stringResource(injury.nameRes),
                        stringResource(injury.statusRes),
                    ),
                )
            }
        }

        SectionHeader(
            title = stringResource(R.string.review_section_limitations),
            description = stringResource(R.string.review_limitations_note),
            modifier = Modifier.padding(top = Dimens.SpacingSmall),
        )
        if (state.limitations.isEmpty()) {
            EmptyLine(text = stringResource(R.string.review_no_limitations))
        } else {
            state.limitations.forEach { BulletLine(text = stringResource(it)) }
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String?,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value ?: stringResource(R.string.review_value_missing),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun BulletLine(
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

@Composable
private fun EmptyLine(
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
private fun ProfileReviewScreenPreview() {
    AppTheme {
        ProfileReviewScreen(
            state = ProfileReviewUiState(
                isLoading = false,
                isComplete = true,
                goalRes = R.string.goal_build_muscle,
                experienceRes = R.string.experience_intermediate,
                daysPerWeek = 3,
                sessionDurationMinutes = 60,
                splitRes = R.string.split_push_pull_legs,
                equipment = listOf(R.string.equipment_bodyweight, R.string.equipment_dumbbells),
                injuries = listOf(
                    ReviewInjuryUiModel(
                        nameRes = R.string.injury_knee_acl,
                        statusRes = R.string.injury_status_historical,
                    ),
                ),
                limitations = listOf(R.string.limitation_avoid_jumping),
            ),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileReviewScreenNothingSelectedPreview() {
    AppTheme {
        ProfileReviewScreen(
            state = ProfileReviewUiState(
                isLoading = false,
                isComplete = true,
                goalRes = R.string.goal_general_fitness,
                experienceRes = R.string.experience_beginner,
                daysPerWeek = 2,
                sessionDurationMinutes = 30,
                splitRes = R.string.split_full_body,
                equipment = listOf(R.string.equipment_bodyweight),
            ),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileReviewScreenIncompletePreview() {
    AppTheme {
        ProfileReviewScreen(
            state = ProfileReviewUiState(isLoading = false, isComplete = false),
            onEvent = {},
        )
    }
}
