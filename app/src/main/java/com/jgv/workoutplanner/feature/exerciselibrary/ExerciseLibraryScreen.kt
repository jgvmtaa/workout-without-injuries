package com.jgv.workoutplanner.feature.exerciselibrary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.jgv.workoutplanner.core.designsystem.Dimens
import com.jgv.workoutplanner.core.ui.AppScaffold
import com.jgv.workoutplanner.core.ui.EmptyContent
import com.jgv.workoutplanner.core.ui.LoadingContent
import com.jgv.workoutplanner.core.ui.labelRes
import com.jgv.workoutplanner.data.catalog.ExerciseCatalog
import com.jgv.workoutplanner.domain.model.Equipment
import com.jgv.workoutplanner.domain.model.ExerciseAvailability
import com.jgv.workoutplanner.domain.model.ExerciseDefinition
import com.jgv.workoutplanner.domain.model.ExerciseDifficulty
import com.jgv.workoutplanner.domain.model.ExerciseEligibility
import com.jgv.workoutplanner.domain.model.ExerciseId
import com.jgv.workoutplanner.domain.model.ExercisePrescription
import com.jgv.workoutplanner.domain.model.ExerciseTag
import com.jgv.workoutplanner.domain.model.MovementLimitation
import com.jgv.workoutplanner.domain.model.MovementPattern
import com.jgv.workoutplanner.domain.model.MuscleGroup

/**
 * Stateful entry point for the library (README §20): collects ViewModel state,
 * forwards navigation events.
 */
@Composable
fun ExerciseLibraryRoute(
    onOpenExerciseDetails: (ExerciseId) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExerciseLibraryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ExerciseLibraryScreen(
        state = state,
        onEvent = { event ->
            when (event) {
                is ExerciseLibraryEvent.OpenExercise -> onOpenExerciseDetails(event.exerciseId)
                ExerciseLibraryEvent.Back -> onBack()
                else -> viewModel.onEvent(event)
            }
        },
        modifier = modifier,
    )
}

/**
 * Browsable exercise catalog with muscle-group chips, equipment filter, and search
 * (README §14, §20). Shows available and excluded rows with subtitle and exclusion count.
 *
 * No NavController inside — previewable.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExerciseLibraryScreen(
    state: ExerciseLibraryUiState,
    onEvent: (ExerciseLibraryEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.isLoading) {
        LoadingContent(modifier = modifier)
        return
    }

    AppScaffold(
        title = stringResource(R.string.screen_exercise_library),
        onBack = { onEvent(ExerciseLibraryEvent.Back) },
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Dimens.ScreenPadding),
        ) {
            // Search
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { onEvent(ExerciseLibraryEvent.SearchQueryChanged(it)) },
                label = { Text(stringResource(R.string.library_search_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onEvent(ExerciseLibraryEvent.SearchQueryChanged("")) }) {
                            Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.action_clear_search))
                        }
                    }
                },
            )

            Spacer(Modifier.height(12.dp))

            // Availability summary + clear
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${stringResource(R.string.library_available_count, state.availableCount)} · " +
                        "${stringResource(R.string.library_excluded_count, state.excludedCount)} · " +
                        "${stringResource(R.string.library_unavailable_count, state.unavailableCount)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (state.selectedMuscleGroup != null || state.selectedEquipment != null || state.searchQuery.isNotBlank()) {
                    TextButton(onClick = { onEvent(ExerciseLibraryEvent.ClearFilters) }) {
                        Text(stringResource(R.string.library_clear_filters))
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Muscle-group tabs/chips: All + 10 groups
            Text(
                text = stringResource(R.string.library_filter_muscle_title),
                style = MaterialTheme.typography.labelLarge,
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = state.selectedMuscleGroup == null,
                    onClick = { onEvent(ExerciseLibraryEvent.SelectMuscleGroup(null)) },
                    label = { Text(stringResource(R.string.muscle_all)) },
                )
                state.muscleGroupOptions.forEach { group ->
                    FilterChip(
                        selected = state.selectedMuscleGroup == group,
                        onClick = { onEvent(ExerciseLibraryEvent.SelectMuscleGroup(group)) },
                        label = { Text(stringResource(group.labelRes)) },
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.library_filter_equipment_title),
                style = MaterialTheme.typography.labelLarge,
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = state.selectedEquipment == null,
                    onClick = { onEvent(ExerciseLibraryEvent.SelectEquipment(null)) },
                    label = { Text(stringResource(R.string.library_filter_equipment_all)) },
                )
                state.equipmentOptions.forEach { equipment ->
                    FilterChip(
                        selected = state.selectedEquipment == equipment,
                        onClick = { onEvent(ExerciseLibraryEvent.SelectEquipment(equipment)) },
                        label = { Text(stringResource(equipment.labelRes)) },
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            if (state.isEmpty) {
                EmptyContent(
                    title = stringResource(R.string.library_empty),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.filteredRows, key = { it.definition.id.name }) { row ->
                        ExerciseRow(
                            row = row,
                            onClick = { onEvent(ExerciseLibraryEvent.OpenExercise(row.definition.id)) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseRow(
    row: ExerciseRowUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val def = row.definition
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = when (row.availability) {
            ExerciseAvailability.AVAILABLE -> CardDefaults.cardColors()
            ExerciseAvailability.EXCLUDED -> CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
            )
            ExerciseAvailability.UNAVAILABLE -> CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        },
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = stringResource(def.nameRes),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(2.dp))
            // Subtitle: muscle · equipment · tag (README §14) — show all, sorted deterministically
            val muscleLabel = stringResource(def.primaryMuscle.labelRes)
            val equipLabels = def.requiredEquipment.sortedBy { it.name }.map { eq ->
                stringResource(eq.labelRes)
            }
            val equipCombined = equipLabels.joinToString(", ")
            val tagLabels = def.tags.sortedBy { it.name }.map { t ->
                stringResource(t.labelRes)
            }
            val tagCombined = tagLabels.joinToString(", ")
            val subtitle = stringResource(
                R.string.library_subtitle_format,
                muscleLabel,
                equipCombined,
                tagCombined,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            when (row.availability) {
                ExerciseAvailability.AVAILABLE -> Text(
                    text = stringResource(R.string.availability_available),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                ExerciseAvailability.EXCLUDED -> Text(
                    text = pluralStringResource(
                        R.plurals.library_excluded_by_count_plural,
                        row.conflictingLimitationCount,
                        row.conflictingLimitationCount,
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                )
                ExerciseAvailability.UNAVAILABLE -> Text(
                    text = stringResource(R.string.availability_unavailable),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// ---- Previews ----

@Preview(showBackground = true)
@Composable
private fun ExerciseLibraryScreenPreview() {
    val sampleExercise = ExerciseCatalog.exercises.first()
    val row = ExerciseRowUiModel(
        definition = sampleExercise,
        eligibility = ExerciseEligibility(sampleExercise, true, emptySet()),
        availability = ExerciseAvailability.AVAILABLE,
    )
    AppTheme {
        ExerciseLibraryScreen(
            state = ExerciseLibraryUiState(
                isLoading = false,
                allRows = listOf(row),
                filteredRows = listOf(row),
                availableCount = 1,
            ),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExerciseLibraryScreenExcludedPreview() {
    AppTheme {
        ExerciseLibraryScreen(
            state = ExerciseLibraryUiState(
                isLoading = false,
                allRows = listOf(fakeRow(ExerciseAvailability.EXCLUDED)),
                filteredRows = listOf(fakeRow(ExerciseAvailability.EXCLUDED)),
                availableCount = 0,
                excludedCount = 1,
            ),
            onEvent = {},
        )
    }
}

private fun fakeRow(availability: ExerciseAvailability): ExerciseRowUiModel {
    val def = ExerciseDefinition(
        id = ExerciseId.DUMBBELL_ROMANIAN_DEADLIFT,
        nameRes = R.string.exercise_dumbbell_romanian_deadlift,
        descriptionRes = R.string.exercise_dumbbell_romanian_deadlift_description,
        primaryMuscle = MuscleGroup.HAMSTRINGS,
        secondaryMuscles = emptySet(),
        movementPattern = MovementPattern.HIP_HINGE,
        requiredEquipment = setOf(Equipment.DUMBBELLS),
        difficulty = ExerciseDifficulty.INTERMEDIATE,
        conflictingLimitations = setOf(MovementLimitation.AVOID_UNSUPPORTED_HIP_HINGE),
        tags = setOf(ExerciseTag.COMPOUND),
        defaultPrescription = ExercisePrescription(3..4, 6..12, 120),
    )
    val reasons = when (availability) {
        ExerciseAvailability.AVAILABLE -> emptySet()
        ExerciseAvailability.EXCLUDED -> setOf(
            com.jgv.workoutplanner.domain.model.ExclusionReason.ConflictingLimitation(
                MovementLimitation.AVOID_UNSUPPORTED_HIP_HINGE,
            ),
        )
        ExerciseAvailability.UNAVAILABLE -> setOf(
            com.jgv.workoutplanner.domain.model.ExclusionReason.MissingEquipment(Equipment.CABLE_MACHINE),
        )
    }
    return ExerciseRowUiModel(
        definition = def,
        eligibility = ExerciseEligibility(def, reasons.isEmpty(), reasons),
        availability = availability,
    )
}
