package com.jgv.workoutplanner.feature.exercisepicker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jgv.workoutplanner.R
import com.jgv.workoutplanner.core.designsystem.AppTheme
import com.jgv.workoutplanner.core.designsystem.Dimens
import com.jgv.workoutplanner.core.ui.LoadingContent
import com.jgv.workoutplanner.core.ui.labelRes
import com.jgv.workoutplanner.domain.model.Equipment
import com.jgv.workoutplanner.domain.model.ExerciseDefinition
import com.jgv.workoutplanner.domain.model.ExerciseDifficulty
import com.jgv.workoutplanner.domain.model.ExerciseId
import com.jgv.workoutplanner.domain.model.ExercisePrescription
import com.jgv.workoutplanner.domain.model.ExerciseTag
import com.jgv.workoutplanner.domain.model.MovementPattern
import com.jgv.workoutplanner.domain.model.MuscleGroup
import com.jgv.workoutplanner.domain.model.WorkoutExerciseEditResult
import com.jgv.workoutplanner.feature.exerciselibrary.ExerciseLibraryUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisePickerRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExercisePickerViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val outdatedMsg = stringResource(R.string.plan_edit_failed_plan_outdated)
    val capMsg = stringResource(R.string.plan_edit_failed_capacity)
    val dupMsg = stringResource(R.string.plan_edit_failed_duplicate)
    val ineligibleMsg = stringResource(R.string.plan_edit_failed_ineligible)
    val invalidMsg = stringResource(R.string.plan_edit_failed_invalid)

    LaunchedEffect(state.isStale) {
        if (state.isStale && !state.isLoading) onBack()
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                ExercisePickerEffect.NavigateBack -> onBack()
                is ExercisePickerEffect.EditFailed -> {
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

    ExercisePickerScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onEvent = { event ->
            when (event) {
                ExercisePickerEvent.Back -> onBack()
                else -> viewModel.onEvent(event)
            }
        },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExercisePickerScreen(
    state: ExercisePickerUiState,
    snackbarHostState: SnackbarHostState,
    onEvent: (ExercisePickerEvent) -> Unit,
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
                title = {
                    // Which day the selection lands in is the one thing the picker
                    // cannot infer from its own content.
                    Text(
                        text = state.dayName
                            ?.let { stringResource(R.string.picker_title_for_day, it) }
                            ?: stringResource(R.string.picker_title),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onEvent(ExercisePickerEvent.Back) }) {
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
            Button(
                onClick = { onEvent(ExercisePickerEvent.Confirm) },
                enabled = state.selectedIdsOrdered.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Text(
                    text = if (state.selectedIdsOrdered.isEmpty()) {
                        stringResource(R.string.picker_confirm)
                    } else {
                        "${stringResource(R.string.picker_confirm)} (${state.selectedIdsOrdered.size})"
                    },
                )
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(
                        R.string.picker_capacity_remaining,
                        state.selectedIdsOrdered.size,
                        state.remainingCapacity,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                )
                if (state.hasActiveFilters) {
                    TextButton(onClick = { onEvent(ExercisePickerEvent.ClearFilters) }) {
                        Text(text = stringResource(R.string.library_clear_filters))
                    }
                }
            }

            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { onEvent(ExercisePickerEvent.SearchQueryChanged(it)) },
                label = { Text(text = stringResource(R.string.picker_search_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    if (state.hasSearchQuery) {
                        IconButton(onClick = { onEvent(ExercisePickerEvent.SearchQueryChanged("")) }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = stringResource(R.string.action_clear_search),
                            )
                        }
                    }
                },
            )

            // Muscle-group filters — functional selection controls (spec §13)
            Text(
                text = stringResource(R.string.library_filter_muscle_title),
                style = MaterialTheme.typography.labelLarge,
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = state.selectedMuscleGroup == null,
                    onClick = { onEvent(ExercisePickerEvent.SelectMuscleGroup(null)) },
                    label = { Text(stringResource(R.string.muscle_all)) },
                )
                MuscleGroup.entries.forEach { group ->
                    FilterChip(
                        selected = state.selectedMuscleGroup == group,
                        onClick = { onEvent(ExercisePickerEvent.SelectMuscleGroup(group)) },
                        label = { Text(stringResource(group.labelRes)) },
                    )
                }
            }

            Text(
                text = stringResource(R.string.library_filter_equipment_title),
                style = MaterialTheme.typography.labelLarge,
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = state.selectedEquipment == null,
                    onClick = { onEvent(ExercisePickerEvent.SelectEquipment(null)) },
                    label = { Text(stringResource(R.string.library_filter_equipment_all)) },
                )
                ExerciseLibraryUiState.DefaultEquipmentOptions.forEach { equipment ->
                    FilterChip(
                        selected = state.selectedEquipment == equipment,
                        onClick = { onEvent(ExercisePickerEvent.SelectEquipment(equipment)) },
                        label = { Text(stringResource(equipment.labelRes)) },
                    )
                }
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.filteredCandidates, key = { it.id.name }) { def ->
                    val isSelected = def.id in state.selectedIdsOrdered
                    val selectionEnabled = isSelected || state.selectedIdsOrdered.size < state.remainingCapacity
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = Dimens.MinTouchTarget)
                            .toggleable(
                                value = isSelected,
                                enabled = selectionEnabled,
                                role = Role.Checkbox,
                                onValueChange = { onEvent(ExercisePickerEvent.ToggleSelection(def.id)) },
                            ),
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
                                text = stringResource(def.nameRes),
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.weight(1f),
                            )
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = null,
                                enabled = selectionEnabled,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ExercisePickerPreview() {
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
        ExercisePickerScreen(
            state = ExercisePickerUiState(
                isLoading = false,
                dayName = "Upper 1",
                remainingCapacity = 2,
                allCandidates = listOf(def),
                filteredCandidates = listOf(def),
                selectedIdsOrdered = emptyList(),
            ),
            snackbarHostState = SnackbarHostState(),
            onEvent = {},
        )
    }
}
