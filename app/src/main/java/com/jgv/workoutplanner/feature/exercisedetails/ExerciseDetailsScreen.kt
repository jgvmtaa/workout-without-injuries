package com.jgv.workoutplanner.feature.exercisedetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
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
import com.jgv.workoutplanner.core.designsystem.Dimens
import com.jgv.workoutplanner.core.ui.AppScaffold
import com.jgv.workoutplanner.core.ui.LoadingContent
import com.jgv.workoutplanner.core.ui.labelRes
import com.jgv.workoutplanner.data.catalog.ExerciseCatalog
import com.jgv.workoutplanner.data.catalog.MovementLimitationCatalog
import com.jgv.workoutplanner.domain.model.ExclusionReason
import com.jgv.workoutplanner.domain.model.ExerciseAvailability
import com.jgv.workoutplanner.domain.model.ExerciseDefinition
import com.jgv.workoutplanner.domain.model.ExerciseEligibility
import com.jgv.workoutplanner.domain.model.ExerciseId

/**
 * Stateful entry point for exercise details (README §15, §20).
 */
@Composable
fun ExerciseDetailsRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExerciseDetailsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ExerciseDetailsScreen(
        state = state,
        onEvent = { event ->
            when (event) {
                ExerciseDetailsEvent.Back -> onBack()
            }
        },
        modifier = modifier,
    )
}

/**
 * Detail view for one exercise, including why it is or is not available for the
 * current profile (README §15).
 *
 * No NavController inside — previewable.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExerciseDetailsScreen(
    state: ExerciseDetailsUiState,
    onEvent: (ExerciseDetailsEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.isLoading || state.definition == null) {
        LoadingContent(modifier = modifier)
        return
    }

    val def = state.definition

    // Pre-resolve strings that need Context, so we can use them outside composable lambdas
    val secondaryMuscleLabels = def.secondaryMuscles.sortedBy { it.name }.map { muscle ->
        muscle to stringResource(muscle.labelRes)
    }
    val secondaryMusclesCombined = secondaryMuscleLabels.joinToString(", ") { it.second }

    AppScaffold(
        title = stringResource(def.nameRes),
        onBack = { onEvent(ExerciseDetailsEvent.Back) },
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(Dimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Overview
            Section(title = stringResource(R.string.detail_section_overview)) {
                Text(
                    text = stringResource(def.descriptionRes),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            // Muscles
            Section(title = stringResource(R.string.detail_section_muscles)) {
                Text(
                    text = stringResource(R.string.detail_primary_muscle, stringResource(def.primaryMuscle.labelRes)),
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (secondaryMuscleLabels.isEmpty()) {
                    Text(
                        text = stringResource(R.string.detail_no_secondary),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        secondaryMuscleLabels.forEach { (_, label) ->
                            FilterChip(
                                selected = false,
                                onClick = {},
                                label = { Text(label) },
                                enabled = false,
                            )
                        }
                    }
                    Text(
                        text = stringResource(R.string.detail_secondary_muscles, secondaryMusclesCombined),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Movement pattern
            Section(title = stringResource(R.string.detail_section_movement)) {
                Text(
                    text = stringResource(def.movementPattern.labelRes),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = stringResource(def.difficulty.labelRes),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Equipment
            Section(title = stringResource(R.string.detail_section_equipment)) {
                EquipmentChips(definition = def)
            }

            // Prescription
            Section(title = stringResource(R.string.detail_section_prescription)) {
                PrescriptionText(definition = def)
            }

            // Availability
            Section(title = stringResource(R.string.detail_section_availability)) {
                AvailabilityCard(
                    eligibility = state.eligibility,
                    availability = state.availability,
                    currentExperienceLevel = state.currentExperienceLevel,
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EquipmentChips(
    definition: ExerciseDefinition,
    modifier: Modifier = Modifier,
) {
    // Pre-resolve equipment labels
    val labels = definition.requiredEquipment.sortedBy { it.name }.map { eq ->
        eq to stringResource(eq.labelRes)
    }
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        labels.forEach { (_, label) ->
            FilterChip(
                selected = false,
                onClick = {},
                label = { Text(label) },
                enabled = false,
            )
        }
    }
}

@Composable
private fun PrescriptionText(
    definition: ExerciseDefinition,
    modifier: Modifier = Modifier,
) {
    val setsRange = stringResource(
        R.string.detail_prescription_range,
        definition.defaultPrescription.sets.first,
        definition.defaultPrescription.sets.last,
    )
    val repsRange = stringResource(
        R.string.detail_prescription_range,
        definition.defaultPrescription.reps.first,
        definition.defaultPrescription.reps.last,
    )
    Text(
        text = stringResource(
            R.string.detail_prescription_format,
            setsRange,
            repsRange,
            definition.defaultPrescription.restSeconds,
        ),
        style = MaterialTheme.typography.bodyMedium,
        modifier = modifier,
    )
}

@Composable
private fun Section(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        content()
    }
}

@Composable
private fun AvailabilityCard(
    eligibility: ExerciseEligibility?,
    availability: ExerciseAvailability?,
    currentExperienceLevel: com.jgv.workoutplanner.domain.model.ExperienceLevel?,
    modifier: Modifier = Modifier,
) {
    if (eligibility == null || availability == null) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Column(Modifier.padding(12.dp)) {
                Text(
                    text = stringResource(R.string.detail_availability_onboarding_required),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        return
    }

    // Current level may be null only in inconsistent states (profile missing but
    // eligibility present should not happen — ViewModel sets both together). Do not
    // silently default to Beginner; handle explicitly.
    val currentLevelLabel = currentExperienceLevel?.let { stringResource(it.labelRes) }

    // Pre-resolve all exclusion reason display strings (no try/catch around composable)
    val reasonTexts = eligibility.exclusionReasons.map { reason ->
        when (reason) {
            is ExclusionReason.ConflictingLimitation -> {
                // Catalog has entry for every limitation, so lookup is safe
                val limitationDef = MovementLimitationCatalog.definition(reason.limitation)
                limitationDef to stringResource(limitationDef.nameRes)
            }
            is ExclusionReason.MissingEquipment -> {
                null to stringResource(R.string.detail_missing_equipment_reason, stringResource(reason.equipment.labelRes))
            }
            is ExclusionReason.AboveExperienceLevel -> {
                val requiredLabel = stringResource(reason.requiredLevel.labelRes)
                if (currentLevelLabel != null) {
                    null to stringResource(
                        R.string.detail_experience_reason,
                        requiredLabel,
                        currentLevelLabel,
                    )
                } else {
                    // Do not silently claim Beginner — show required level only.
                    null to stringResource(
                        R.string.detail_experience_reason_requires_only,
                        requiredLabel,
                    )
                }
            }
        }
    }

    when (availability) {
        ExerciseAvailability.AVAILABLE -> {
            Card(
                modifier = modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(
                        text = stringResource(availability.labelRes),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.detail_available_message),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
        ExerciseAvailability.EXCLUDED -> {
            Card(
                modifier = modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(R.string.detail_excluded_title),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = stringResource(R.string.detail_excluded_message),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    reasonTexts.forEach { (def, text) ->
                        if (def != null) {
                            // ConflictingLimitation: wrap with reason format
                            Text(
                                text = stringResource(R.string.detail_conflicting_limitation_reason, text),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        } else {
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }
        }
        ExerciseAvailability.UNAVAILABLE -> {
            Card(
                modifier = modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(R.string.detail_unavailable_title),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    reasonTexts.forEach { (def, text) ->
                        if (def != null) {
                            Text(
                                text = stringResource(R.string.detail_conflicting_limitation_reason, text),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        } else {
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodyMedium,
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
private fun ExerciseDetailsScreenPreviewAvailable() {
    val def = ExerciseCatalog.exercises.first { it.id == ExerciseId.MACHINE_CHEST_PRESS }
    val eligibility = ExerciseEligibility(def, true, emptySet())
    AppTheme {
        ExerciseDetailsScreen(
            state = ExerciseDetailsUiState(
                isLoading = false,
                definition = def,
                eligibility = eligibility,
                availability = ExerciseAvailability.AVAILABLE,
            ),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExerciseDetailsScreenPreviewExcluded() {
    val def = ExerciseCatalog.exercises.first { it.id == ExerciseId.DUMBBELL_ROMANIAN_DEADLIFT }
    val reason = ExclusionReason.ConflictingLimitation(
        com.jgv.workoutplanner.domain.model.MovementLimitation.AVOID_UNSUPPORTED_HIP_HINGE,
    )
    val eligibility = ExerciseEligibility(def, false, setOf(reason))
    AppTheme {
        ExerciseDetailsScreen(
            state = ExerciseDetailsUiState(
                isLoading = false,
                definition = def,
                eligibility = eligibility,
                availability = ExerciseAvailability.EXCLUDED,
            ),
            onEvent = {},
        )
    }
}
