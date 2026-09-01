package com.jgv.workoutplanner.feature.plan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jgv.workoutplanner.R
import com.jgv.workoutplanner.core.designsystem.AppTheme
import com.jgv.workoutplanner.core.ui.LoadingContent
import com.jgv.workoutplanner.domain.model.ExerciseDifficulty
import com.jgv.workoutplanner.domain.model.ExerciseId
import com.jgv.workoutplanner.domain.model.ExercisePrescription
import com.jgv.workoutplanner.domain.model.ExerciseDefinition
import com.jgv.workoutplanner.domain.model.ExerciseTag
import com.jgv.workoutplanner.domain.model.MovementPattern
import com.jgv.workoutplanner.domain.model.MuscleGroup
import com.jgv.workoutplanner.domain.model.WorkoutDayFocus
import com.jgv.workoutplanner.domain.model.WorkoutPlan
import com.jgv.workoutplanner.domain.model.PlannedExercise
import com.jgv.workoutplanner.domain.model.WorkoutDay

/**
 * Stateful entry point for the plan destination (README §20, task 5.8).
 *
 * Route resolves ViewModel and collects state; screen below is stateless and previewable.
 * Auto-generates on entry if no plan exists (Home button + auto-generate decision).
 */
@Composable
fun PlanRoute(
    onOpenExerciseDetails: (ExerciseId) -> Unit,
    onReplaceExercise: (workoutDayId: String, exerciseId: ExerciseId) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlanViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Auto-generate once when screen appears and no plan exists.
    LaunchedEffect(state.hasNoPlan, state.isLoading) {
        if (!state.isLoading && state.hasNoPlan) {
            viewModel.autoGenerateIfNeeded()
        }
    }

    PlanScreen(
        state = state,
        onEvent = { event ->
            when (event) {
                is PlanEvent.OpenExerciseDetails -> onOpenExerciseDetails(event.exerciseId)
                is PlanEvent.ReplaceExercise -> onReplaceExercise(event.dayId, event.exerciseId)
                PlanEvent.Back -> onBack()
                else -> viewModel.onEvent(event)
            }
        },
        modifier = modifier,
    )
}

/**
 * The current workout plan (README §11–§13, task 5.8).
 *
 * Renders generated plan by day with sets/reps/rest. Follows §20: immutable UiState,
 * sealed PlanEvent, no NavController passed in.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanScreen(
    state: PlanUiState,
    onEvent: (PlanEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.isLoading) {
        LoadingContent(modifier = modifier)
        return
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = state.planName ?: stringResource(R.string.screen_plan)) },
                navigationIcon = {
                    IconButton(onClick = { onEvent(PlanEvent.Back) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
                actions = {
                    if (!state.hasNoPlan) {
                        TextButton(onClick = { onEvent(PlanEvent.RegeneratePlan) }) {
                            Text(text = stringResource(R.string.action_regenerate_plan))
                        }
                    }
                },
            )
        },
    ) { paddingValues ->
        if (state.hasNoPlan) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = stringResource(R.string.plan_empty_title),
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.plan_empty_body),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onEvent(PlanEvent.GeneratePlan) },
                    enabled = !state.isGenerating,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = stringResource(R.string.action_generate_plan))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (state.warnings.isNotEmpty()) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = stringResource(R.string.plan_warnings_title),
                                    style = MaterialTheme.typography.titleSmall,
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                state.warnings.forEach { warning ->
                                    Text(
                                        text = "Day ${warning.dayIndex + 1} (${warning.dayFocus.name}): ${warning.reason}",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(vertical = 2.dp),
                                    )
                                }
                            }
                        }
                    }
                }

                items(state.days) { day ->
                    WorkoutDayCard(
                        day = day,
                        onOpenDetails = { id -> onEvent(PlanEvent.OpenExerciseDetails(id)) },
                        onReplace = { dayId, exId -> onEvent(PlanEvent.ReplaceExercise(dayId, exId)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkoutDayCard(
    day: WorkoutDayUiModel,
    onOpenDetails: (ExerciseId) -> Unit,
    onReplace: (String, ExerciseId) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = day.name, style = MaterialTheme.typography.titleMedium)
                Text(text = day.focus.name, style = MaterialTheme.typography.labelMedium)
            }
            if (day.exercises.isEmpty()) {
                Text(
                    text = stringResource(R.string.plan_slot_no_match),
                    style = MaterialTheme.typography.bodySmall,
                )
            } else {
                day.exercises.forEachIndexed { index, ex ->
                    if (index > 0) {
                        HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
                    }
                    PlannedExerciseRow(
                        exercise = ex,
                        onOpenDetails = onOpenDetails,
                        onReplace = { onReplace(day.id, ex.exerciseId) },
                    )
                }
            }
        }
    }
}

@Composable
private fun PlannedExerciseRow(
    exercise: PlannedExerciseUiModel,
    onOpenDetails: (ExerciseId) -> Unit,
    onReplace: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        val nameText = exercise.definition?.let { def ->
            runCatching { stringResource(def.nameRes) }.getOrNull()
        } ?: exercise.exerciseId.name

        // Line 1: Exercise name - full width, up to 2 lines, no competing button
        Text(
            text = "${exercise.order + 1}. $nameText",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.fillMaxWidth(),
        )

        // Line 2: Sets/reps + actions aligned in one row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${exercise.sets} x ${exercise.repRange.first}..${exercise.repRange.last} • ${exercise.restSeconds}s rest",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = { onOpenDetails(exercise.exerciseId) },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                ) {
                    Text(
                        text = stringResource(R.string.action_open_exercise_details_short),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
                TextButton(
                    onClick = onReplace,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                ) {
                    Text(
                        text = stringResource(R.string.action_replace_exercise_short),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
}

// ---------------- Previews

@Preview(showBackground = true)
@Composable
private fun PlanScreenEmptyPreview() {
    AppTheme {
        PlanScreen(
            state = PlanUiState(isLoading = false, hasNoPlan = true),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PlanScreenWithDataPreview() {
    AppTheme {
        val def = ExerciseDefinition(
            id = ExerciseId.MACHINE_CHEST_PRESS,
            nameRes = R.string.exercise_machine_chest_press,
            descriptionRes = R.string.exercise_machine_chest_press_description,
            primaryMuscle = MuscleGroup.CHEST,
            secondaryMuscles = emptySet(),
            movementPattern = MovementPattern.HORIZONTAL_PUSH,
            requiredEquipment = emptySet(),
            difficulty = ExerciseDifficulty.BEGINNER,
            conflictingLimitations = emptySet(),
            tags = setOf(ExerciseTag.COMPOUND),
            defaultPrescription = ExercisePrescription(3..4, 8..12, 90),
        )
        PlanScreen(
            state = PlanUiState(
                isLoading = false,
                planName = "UPPER_LOWER - 4 days",
                days = listOf(
                    WorkoutDayUiModel(
                        id = "plan-4-UPPER_LOWER-day-0-upper_body",
                        name = "Upper 1",
                        focus = WorkoutDayFocus.UPPER_BODY,
                        exercises = listOf(
                            PlannedExerciseUiModel(ExerciseId.MACHINE_CHEST_PRESS, def, 3, 8..12, 90, 0),
                            PlannedExerciseUiModel(ExerciseId.LAT_PULLDOWN, def, 3, 8..12, 90, 1),
                        ),
                    ),
                ),
                warnings = emptyList(),
                hasNoPlan = false,
            ),
            onEvent = {},
        )
    }
}
