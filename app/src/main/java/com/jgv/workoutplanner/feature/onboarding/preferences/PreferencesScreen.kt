package com.jgv.workoutplanner.feature.onboarding.preferences

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
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
import com.jgv.workoutplanner.core.ui.SingleChoiceRow
import com.jgv.workoutplanner.core.ui.StepIntroduction
import com.jgv.workoutplanner.core.ui.labelRes
import com.jgv.workoutplanner.domain.model.Equipment
import com.jgv.workoutplanner.domain.model.ExperienceLevel
import com.jgv.workoutplanner.domain.model.TrainingGoal
import com.jgv.workoutplanner.domain.model.WorkoutSplit

/** Stateful entry point for the preferences step (README §20). */
@Composable
fun PreferencesRoute(
    onContinue: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PreferencesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    PreferencesScreen(
        state = state,
        onEvent = { event ->
            when (event) {
                PreferencesEvent.Continue -> onContinue()
                PreferencesEvent.Back -> onBack()
                else -> viewModel.onEvent(event)
            }
        },
        modifier = modifier,
    )
}

/**
 * Goal, experience level, weekly schedule, session duration and equipment
 * (README §4.3).
 *
 * The split is shown but not chosen. README §4.3 lists it among the preferences and then
 * permits deriving it from the training frequency instead; deriving it removes a question
 * whose wrong answers (five days of full body) the app would then have to defend, and
 * showing the result keeps it from being a surprise on the plan screen.
 */
@Composable
fun PreferencesScreen(
    state: PreferencesUiState,
    onEvent: (PreferencesEvent) -> Unit,
    modifier: Modifier = Modifier,
    onCancel: (() -> Unit)? = null,
) {
    if (state.isLoading) {
        LoadingContent(modifier = modifier)
        return
    }

    OnboardingScaffold(
        title = stringResource(R.string.screen_preferences),
        continueLabel = stringResource(R.string.action_continue),
        onContinue = { onEvent(PreferencesEvent.Continue) },
        modifier = modifier,
        onBack = { onEvent(PreferencesEvent.Back) },
        onCancel = onCancel,
        continueEnabled = state.canContinue,
    ) {
        StepIntroduction(text = stringResource(R.string.preferences_intro))

        SectionHeader(title = stringResource(R.string.preferences_goal_title))
        TrainingGoal.entries.forEach { goal ->
            SingleChoiceRow(
                label = stringResource(goal.labelRes),
                selected = state.goal == goal,
                value = goal,
                onSelect = { onEvent(PreferencesEvent.SelectGoal(it)) },
            )
        }

        SectionHeader(title = stringResource(R.string.preferences_experience_title))
        ExperienceLevel.entries.forEach { level ->
            SingleChoiceRow(
                label = stringResource(level.labelRes),
                selected = state.experienceLevel == level,
                value = level,
                onSelect = { onEvent(PreferencesEvent.SelectExperience(it)) },
            )
        }

        SectionHeader(title = stringResource(R.string.preferences_days_title))
        SingleChoiceChipRow(
            options = state.dayOptions,
            selected = state.daysPerWeek,
            onSelect = { onEvent(PreferencesEvent.SelectDaysPerWeek(it)) },
            label = { days -> pluralStringResource(R.plurals.preferences_days_value, days, days) },
        )

        if (state.derivedSplit != null) {
            DerivedSplitCard(split = state.derivedSplit)
        }

        SectionHeader(title = stringResource(R.string.preferences_duration_title))
        SingleChoiceChipRow(
            options = state.durationOptions,
            selected = state.sessionDurationMinutes,
            onSelect = { onEvent(PreferencesEvent.SelectSessionDuration(it)) },
            label = { minutes -> stringResource(R.string.preferences_duration_value, minutes) },
        )

        SectionHeader(
            title = stringResource(R.string.preferences_equipment_title),
            description = stringResource(R.string.preferences_equipment_description),
        )
        CheckboxRow(
            label = stringResource(Equipment.BODYWEIGHT.labelRes),
            checked = true,
            onCheckedChange = {},
            enabled = false,
            supportingText = stringResource(R.string.preferences_equipment_always_available),
        )
        state.equipmentOptions.forEach { equipment ->
            CheckboxRow(
                label = stringResource(equipment.labelRes),
                checked = equipment in state.selectedEquipment,
                onCheckedChange = { onEvent(PreferencesEvent.ToggleEquipment(equipment, it)) },
            )
        }
    }
}

@Composable
private fun DerivedSplitCard(
    split: WorkoutSplit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Dimens.SpacingMedium)) {
            Text(
                text = stringResource(R.string.preferences_split_title),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(split.labelRes),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = Dimens.SpacingExtraSmall),
            )
            Text(
                text = stringResource(R.string.preferences_split_derived),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreferencesScreenEmptyPreview() {
    AppTheme {
        PreferencesScreen(
            state = PreferencesUiState(isLoading = false),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreferencesScreenAnsweredPreview() {
    AppTheme {
        PreferencesScreen(
            state = PreferencesUiState(
                isLoading = false,
                goal = TrainingGoal.BUILD_MUSCLE,
                experienceLevel = ExperienceLevel.INTERMEDIATE,
                daysPerWeek = 3,
                sessionDurationMinutes = 60,
                selectedEquipment = setOf(Equipment.DUMBBELLS, Equipment.BENCH),
                derivedSplit = WorkoutSplit.PUSH_PULL_LEGS,
            ),
            onEvent = {},
        )
    }
}
