package com.jgv.workoutplanner.feature.plan

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.jgv.workoutplanner.domain.model.ExerciseDefinition
import com.jgv.workoutplanner.domain.model.ExerciseDifficulty
import com.jgv.workoutplanner.domain.model.ExerciseId
import com.jgv.workoutplanner.domain.model.ExercisePrescription
import com.jgv.workoutplanner.domain.model.ExerciseTag
import com.jgv.workoutplanner.domain.model.MovementPattern
import com.jgv.workoutplanner.domain.model.MuscleGroup
import com.jgv.workoutplanner.domain.model.WorkoutExerciseEditResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseReplacementRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExerciseReplacementViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val outdatedMsg = stringResource(R.string.plan_edit_failed_plan_outdated)
    val capMsg = stringResource(R.string.plan_edit_failed_capacity)
    val dupMsg = stringResource(R.string.plan_edit_failed_duplicate)
    val ineligibleMsg = stringResource(R.string.plan_edit_failed_ineligible)
    val invalidMsg = stringResource(R.string.plan_edit_failed_invalid)

    LaunchedEffect(state.isStale) {
        if (state.isStale && !state.isLoading) {
            onBack()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                ExerciseReplacementEffect.NavigateBack -> onBack()
                is ExerciseReplacementEffect.EditFailed -> {
                    val msg = when (effect.result) {
                        WorkoutExerciseEditResult.PlanOutdated -> outdatedMsg
                        WorkoutExerciseEditResult.CapacityExceeded -> capMsg
                        WorkoutExerciseEditResult.DuplicateExercise -> dupMsg
                        WorkoutExerciseEditResult.IneligibleExercise -> ineligibleMsg
                        WorkoutExerciseEditResult.InvalidTargetIndex,
                        WorkoutExerciseEditResult.DayNotFound,
                        WorkoutExerciseEditResult.ExerciseNotFound,
                        WorkoutExerciseEditResult.PlanNotFound,
                        WorkoutExerciseEditResult.Unchanged -> invalidMsg
                        WorkoutExerciseEditResult.Updated -> ""
                    }
                    if (msg.isNotBlank()) snackbarHostState.showSnackbar(msg)
                }
            }
        }
    }

    ExerciseReplacementScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onEvent = { event ->
            when (event) {
                ExerciseReplacementEvent.Back -> onBack()
                else -> viewModel.onEvent(event)
            }
        },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseReplacementScreen(
    state: ExerciseReplacementUiState,
    snackbarHostState: SnackbarHostState,
    onEvent: (ExerciseReplacementEvent) -> Unit,
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
                title = { Text(text = stringResource(R.string.replacement_title)) },
                navigationIcon = {
                    IconButton(onClick = { onEvent(ExerciseReplacementEvent.Back) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (!state.noCandidates) {
                Button(
                    onClick = { onEvent(ExerciseReplacementEvent.ConfirmReplacement) },
                    enabled = state.selectedExerciseId != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Text(text = stringResource(R.string.replacement_confirm))
                }
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.replacement_select_prompt),
                style = MaterialTheme.typography.bodyMedium,
            )

            if (state.noCandidates) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.replacement_empty),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.candidates) { def ->
                        val isSelected = state.selectedExerciseId == def.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onEvent(ExerciseReplacementEvent.SelectCandidate(def.id)) },
                            colors = if (isSelected) CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                            ) else CardDefaults.cardColors(),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = runCatching { stringResource(def.nameRes) }.getOrNull() ?: def.id.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.weight(1f),
                                )
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { onEvent(ExerciseReplacementEvent.SelectCandidate(def.id)) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ExerciseReplacementScreenPreview() {
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
        ExerciseReplacementScreen(
            state = ExerciseReplacementUiState(
                isLoading = false,
                currentExerciseName = "Machine chest press",
                candidates = listOf(def),
                selectedExerciseId = null,
                noCandidates = false,
                isStale = false,
            ),
            snackbarHostState = SnackbarHostState(),
            onEvent = {},
        )
    }
}
