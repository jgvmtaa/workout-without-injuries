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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
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
import com.jgv.workoutplanner.domain.model.WorkoutDayFocus
import com.jgv.workoutplanner.domain.model.WorkoutExerciseEditResult

/**
 * Stateful entry point for the plan destination (README §20, Phase 5/6).
 *
 * - Auto-generates if no plan.
 * - Collects typed one-shot effects (PlanEffect.EditFailed) and maps to localized text.
 * - Handles navigation for details / replace / picker.
 */
@Composable
fun PlanRoute(
    onOpenExerciseDetails: (ExerciseId) -> Unit,
    onReplaceExercise: (workoutDayId: String, exerciseId: ExerciseId) -> Unit,
    onOpenExercisePicker: (workoutDayId: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlanViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val showConfirm by viewModel.showRegenerateConfirm.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val outdatedMessage = stringResource(R.string.plan_outdated_banner)
    val capMsg = stringResource(R.string.plan_edit_failed_capacity)
    val dupMsg = stringResource(R.string.plan_edit_failed_duplicate)
    val ineligibleMsg = stringResource(R.string.plan_edit_failed_ineligible)
    val invalidMsg = stringResource(R.string.plan_edit_failed_invalid)
    val genericMsg = stringResource(R.string.plan_edit_failed_generic)
    val outdatedEditMsg = stringResource(R.string.plan_edit_failed_plan_outdated)

    LaunchedEffect(state.hasNoPlan, state.isLoading) {
        if (!state.isLoading && state.hasNoPlan) {
            viewModel.autoGenerateIfNeeded()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            val msg = when (effect) {
                is PlanEffect.EditFailed -> when (effect.result) {
                    WorkoutExerciseEditResult.PlanOutdated -> outdatedEditMsg
                    WorkoutExerciseEditResult.CapacityExceeded -> capMsg
                    WorkoutExerciseEditResult.DuplicateExercise -> dupMsg
                    WorkoutExerciseEditResult.IneligibleExercise -> ineligibleMsg
                    WorkoutExerciseEditResult.InvalidTargetIndex,
                    WorkoutExerciseEditResult.DayNotFound,
                    WorkoutExerciseEditResult.ExerciseNotFound,
                    WorkoutExerciseEditResult.PlanNotFound -> invalidMsg
                    WorkoutExerciseEditResult.Unchanged -> ""
                    WorkoutExerciseEditResult.Updated -> ""
                }
            }
            if (msg.isNotBlank()) {
                snackbarHostState.showSnackbar(msg)
            }
        }
    }

    PlanScreen(
        state = state,
        showRegenerateConfirm = showConfirm,
        snackbarHostState = snackbarHostState,
        onEvent = { event ->
            when (event) {
                is PlanEvent.OpenExerciseDetails -> onOpenExerciseDetails(event.exerciseId)
                is PlanEvent.ReplaceExercise -> onReplaceExercise(event.dayId, event.exerciseId)
                is PlanEvent.OpenExercisePicker -> onOpenExercisePicker(event.dayId)
                PlanEvent.Back -> onBack()
                else -> viewModel.onEvent(event)
            }
        },
        modifier = modifier,
    )
}

/**
 * The current workout plan (README §11–§13, Phase 5/6).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanScreen(
    state: PlanUiState,
    showRegenerateConfirm: Boolean,
    snackbarHostState: SnackbarHostState,
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                if (state.requiresRegeneration) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                            ),
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = stringResource(R.string.plan_outdated_banner),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                )
                            }
                        }
                    }
                }

                if (!state.requiresRegeneration && state.warnings.isNotEmpty()) {
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
                        canEdit = !state.requiresRegeneration,
                        onOpenDetails = { id -> onEvent(PlanEvent.OpenExerciseDetails(id)) },
                        onReplace = { dayId, exId -> onEvent(PlanEvent.ReplaceExercise(dayId, exId)) },
                        onRemove = { dayId, exId -> onEvent(PlanEvent.RemoveExercise(dayId, exId)) },
                        onMoveUp = { dayId, exId -> onEvent(PlanEvent.MoveUp(dayId, exId)) },
                        onMoveDown = { dayId, exId -> onEvent(PlanEvent.MoveDown(dayId, exId)) },
                        onAddExercise = { dayId -> onEvent(PlanEvent.OpenExercisePicker(dayId)) },
                    )
                }
            }
        }

        if (showRegenerateConfirm) {
            AlertDialog(
                onDismissRequest = { onEvent(PlanEvent.DismissRegenerationConfirm) },
                title = { Text(text = stringResource(R.string.plan_regenerate_confirm_title)) },
                text = { Text(text = stringResource(R.string.plan_regenerate_confirm_body)) },
                confirmButton = {
                    TextButton(onClick = { onEvent(PlanEvent.ConfirmRegeneration) }) {
                        Text(text = stringResource(R.string.plan_regenerate_confirm_ok))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onEvent(PlanEvent.DismissRegenerationConfirm) }) {
                        Text(text = stringResource(R.string.plan_regenerate_confirm_cancel))
                    }
                },
            )
        }
    }
}

@Composable
private fun WorkoutDayCard(
    day: WorkoutDayUiModel,
    canEdit: Boolean,
    onOpenDetails: (ExerciseId) -> Unit,
    onReplace: (String, ExerciseId) -> Unit,
    onRemove: (String, ExerciseId) -> Unit,
    onMoveUp: (String, ExerciseId) -> Unit,
    onMoveDown: (String, ExerciseId) -> Unit,
    onAddExercise: (String) -> Unit,
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
                    text = stringResource(R.string.plan_empty_day),
                    style = MaterialTheme.typography.bodySmall,
                )
            } else {
                day.exercises.forEachIndexed { index, ex ->
                    if (index > 0) {
                        HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
                    }
                    PlannedExerciseRow(
                        exercise = ex,
                        canEdit = canEdit,
                        isFirst = index == 0,
                        isLast = index == day.exercises.lastIndex,
                        onOpenDetails = onOpenDetails,
                        onReplace = { onReplace(day.id, ex.exerciseId) },
                        onRemove = { onRemove(day.id, ex.exerciseId) },
                        onMoveUp = { onMoveUp(day.id, ex.exerciseId) },
                        onMoveDown = { onMoveDown(day.id, ex.exerciseId) },
                    )
                }
            }

            if (canEdit && day.remainingCapacity > 0) {
                HorizontalDivider()
                val addLabel = pluralStringResource(
                    R.plurals.plan_add_row,
                    day.remainingCapacity,
                    day.remainingCapacity,
                )
                OutlinedButton(
                    onClick = { onAddExercise(day.id) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = addLabel)
                }
            }
        }
    }
}

@Composable
private fun PlannedExerciseRow(
    exercise: PlannedExerciseUiModel,
    canEdit: Boolean,
    isFirst: Boolean,
    isLast: Boolean,
    onOpenDetails: (ExerciseId) -> Unit,
    onReplace: () -> Unit,
    onRemove: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val nameText = exercise.definition?.let { def ->
                runCatching { stringResource(def.nameRes) }.getOrNull()
            } ?: exercise.exerciseId.name

            Text(
                text = "${exercise.order + 1}. $nameText",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f),
            )

            if (canEdit) {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "More",
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(text = stringResource(R.string.plan_action_details)) },
                        onClick = {
                            showMenu = false
                            onOpenDetails(exercise.exerciseId)
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(text = stringResource(R.string.action_replace_exercise_short)) },
                        onClick = {
                            showMenu = false
                            onReplace()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(text = stringResource(R.string.plan_action_move_up)) },
                        onClick = {
                            showMenu = false
                            onMoveUp()
                        },
                        enabled = !isFirst,
                    )
                    DropdownMenuItem(
                        text = { Text(text = stringResource(R.string.plan_action_move_down)) },
                        onClick = {
                            showMenu = false
                            onMoveDown()
                        },
                        enabled = !isLast,
                    )
                    DropdownMenuItem(
                        text = { Text(text = stringResource(R.string.plan_action_remove)) },
                        onClick = {
                            showMenu = false
                            onRemove()
                        },
                    )
                }
            }
        }

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
            showRegenerateConfirm = false,
            snackbarHostState = SnackbarHostState(),
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
                        remainingCapacity = 1,
                    ),
                ),
                warnings = emptyList(),
                hasNoPlan = false,
                requiresRegeneration = false,
            ),
            showRegenerateConfirm = false,
            snackbarHostState = SnackbarHostState(),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PlanScreenOutdatedPreview() {
    AppTheme {
        PlanScreen(
            state = PlanUiState(
                isLoading = false,
                planName = "UPPER_LOWER - 4 days",
                days = listOf(
                    WorkoutDayUiModel(
                        id = "day-0",
                        name = "Upper 1",
                        focus = WorkoutDayFocus.UPPER_BODY,
                        exercises = emptyList(),
                        remainingCapacity = 0,
                    ),
                ),
                warnings = emptyList(),
                hasNoPlan = false,
                requiresRegeneration = true,
            ),
            showRegenerateConfirm = false,
            snackbarHostState = SnackbarHostState(),
            onEvent = {},
        )
    }
}
