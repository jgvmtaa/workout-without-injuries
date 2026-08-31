package com.jgv.workoutplanner.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jgv.workoutplanner.R
import com.jgv.workoutplanner.core.designsystem.AppTheme
import com.jgv.workoutplanner.core.ui.LoadingContent
import com.jgv.workoutplanner.domain.model.WorkoutSplit

/**
 * Stateful entry point for the home destination (README §20, §16 task 5.8).
 */
@Composable
fun HomeRoute(
    onOpenPlan: () -> Unit,
    onOpenExerciseLibrary: () -> Unit,
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        state = state,
        onOpenPlan = onOpenPlan,
        onOpenExerciseLibrary = onOpenExerciseLibrary,
        onOpenProfile = onOpenProfile,
        modifier = modifier,
    )
}

/**
 * Post-onboarding home screen (README §16).
 *
 * Shows current plan (if exists), profile summary, active limitations, library entry.
 * Phase 5 real implementation replacing placeholder.
 */
@Composable
fun HomeScreen(
    state: HomeUiState,
    onOpenPlan: () -> Unit,
    onOpenExerciseLibrary: () -> Unit,
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.isLoading) {
        LoadingContent(modifier = modifier)
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Current plan section
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = stringResource(R.string.home_section_current_plan), style = MaterialTheme.typography.titleMedium)
                if (state.hasPlan) {
                    Text(text = state.planName ?: stringResource(R.string.home_plan_ready), style = MaterialTheme.typography.bodyMedium)
                    Button(onClick = onOpenPlan, modifier = Modifier.fillMaxWidth()) {
                        Text(text = stringResource(R.string.action_view_plan))
                    }
                } else {
                    Text(text = stringResource(R.string.home_plan_empty), style = MaterialTheme.typography.bodyMedium)
                    Button(onClick = onOpenPlan, modifier = Modifier.fillMaxWidth()) {
                        Text(text = stringResource(R.string.action_generate_plan))
                    }
                }
            }
        }

        // Profile summary
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = stringResource(R.string.home_section_profile), style = MaterialTheme.typography.titleMedium)
                val summary = buildString {
                    state.daysPerWeek?.let { append("${it} days") }
                    state.split?.let { append(" · ${it.name}") }
                    state.goalDisplay?.let { append(" · $it") }
                    state.experienceDisplay?.let { append(" · $it") }
                }.ifEmpty { stringResource(R.string.review_value_missing) }
                Text(text = summary, style = MaterialTheme.typography.bodyMedium)
            }
        }

        // Active limitations
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = stringResource(R.string.home_section_limitations), style = MaterialTheme.typography.titleMedium)
                Text(
                    text = if (state.limitationsCount == 0) stringResource(R.string.review_no_limitations)
                    else stringResource(R.string.home_limitations_count, state.limitationsCount),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        // Library + Profile actions
        OutlinedButton(onClick = onOpenExerciseLibrary, modifier = Modifier.fillMaxWidth()) {
            Text(text = stringResource(R.string.action_browse_library))
        }
        OutlinedButton(onClick = onOpenProfile, modifier = Modifier.fillMaxWidth()) {
            Text(text = stringResource(R.string.action_open_profile))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    AppTheme {
        HomeScreen(
            state = HomeUiState(
                isLoading = false,
                daysPerWeek = 4,
                goalDisplay = "BUILD_MUSCLE",
                experienceDisplay = "INTERMEDIATE",
                split = WorkoutSplit.UPPER_LOWER,
                limitationsCount = 2,
                hasPlan = true,
                planName = "UPPER_LOWER - 4 days",
            ),
            onOpenPlan = {},
            onOpenExerciseLibrary = {},
            onOpenProfile = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenLoadingPreview() {
    AppTheme {
        HomeScreen(
            state = HomeUiState(isLoading = true),
            onOpenPlan = {},
            onOpenExerciseLibrary = {},
            onOpenProfile = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenNoPlanPreview() {
    AppTheme {
        HomeScreen(
            state = HomeUiState(
                isLoading = false,
                daysPerWeek = 3,
                goalDisplay = "GENERAL_FITNESS",
                experienceDisplay = "BEGINNER",
                split = WorkoutSplit.PUSH_PULL_LEGS,
                limitationsCount = 0,
                hasPlan = false,
                planName = null,
            ),
            onOpenPlan = {},
            onOpenExerciseLibrary = {},
            onOpenProfile = {},
        )
    }
}
