package com.jgv.workoutplanner.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.jgv.workoutplanner.core.ui.LoadingContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileRoute(
    onEditPreferences: () -> Unit,
    onEditInjuries: () -> Unit,
    onEditLimitations: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                ProfileEffect.NavigateToEditPreferences -> onEditPreferences()
                ProfileEffect.NavigateToEditInjuries -> onEditInjuries()
                ProfileEffect.NavigateToEditLimitations -> onEditLimitations()
            }
        }
    }

    ProfileScreen(
        state = state,
        onEvent = { event ->
            when (event) {
                ProfileEvent.Back -> onBack()
                else -> viewModel.onEvent(event)
            }
        },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onEvent: (ProfileEvent) -> Unit,
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
                title = { Text(text = stringResource(R.string.screen_profile)) },
                navigationIcon = {
                    IconButton(onClick = { onEvent(ProfileEvent.Back) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Preferences section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = stringResource(R.string.profile_section_preferences), style = MaterialTheme.typography.titleMedium)
                    state.goalRes?.let { Text(text = "${stringResource(R.string.review_label_goal)}: ${stringResource(it)}", style = MaterialTheme.typography.bodyMedium) }
                    state.experienceRes?.let { Text(text = "${stringResource(R.string.review_label_experience)}: ${stringResource(it)}", style = MaterialTheme.typography.bodyMedium) }
                    state.daysPerWeek?.let {
                        Text(text = "${stringResource(R.string.review_label_days)}: ${pluralStringResource(R.plurals.preferences_days_value, it, it)}", style = MaterialTheme.typography.bodyMedium)
                    }
                    state.sessionDurationMinutes?.let {
                        Text(text = "${stringResource(R.string.review_label_duration)}: ${stringResource(R.string.preferences_duration_value, it)}", style = MaterialTheme.typography.bodyMedium)
                    }
                    state.splitRes?.let { Text(text = "${stringResource(R.string.review_label_split)}: ${stringResource(it)}", style = MaterialTheme.typography.bodyMedium) }
                    // Equipment as bullet list
                    if (state.equipmentRes.isNotEmpty()) {
                        Text(text = stringResource(R.string.review_section_equipment), style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = Dimens.SpacingSmall))
                        state.equipmentRes.forEach { res -> Text(text = "• ${stringResource(res)}", style = MaterialTheme.typography.bodySmall) }
                    }
                    OutlinedButton(onClick = { onEvent(ProfileEvent.EditPreferences) }, modifier = Modifier.fillMaxWidth()) {
                        Text(text = stringResource(R.string.action_edit_preferences))
                    }
                }
            }

            // Injuries section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = stringResource(R.string.profile_section_injuries), style = MaterialTheme.typography.titleMedium)
                    if (state.injuries.isEmpty()) {
                        Text(text = stringResource(R.string.review_no_injuries), style = MaterialTheme.typography.bodyMedium)
                    } else {
                        state.injuries.forEach { injury ->
                            Text(
                                text = stringResource(R.string.review_injury_with_status, stringResource(injury.nameRes), stringResource(injury.statusRes)),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                    OutlinedButton(onClick = { onEvent(ProfileEvent.EditInjuries) }, modifier = Modifier.fillMaxWidth()) {
                        Text(text = stringResource(R.string.action_edit_injuries))
                    }
                }
            }

            // Limitations section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = stringResource(R.string.profile_section_limitations), style = MaterialTheme.typography.titleMedium)
                    if (state.limitationsRes.isEmpty()) {
                        Text(text = stringResource(R.string.review_no_limitations), style = MaterialTheme.typography.bodyMedium)
                    } else {
                        state.limitationsRes.forEach { res ->
                            Text(text = "• ${stringResource(res)}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    OutlinedButton(onClick = { onEvent(ProfileEvent.EditLimitations) }, modifier = Modifier.fillMaxWidth()) {
                        Text(text = stringResource(R.string.action_edit_limitations))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    AppTheme {
        ProfileScreen(
            state = ProfileUiState(
                isLoading = false,
                hasProfile = true,
                goalRes = R.string.goal_build_muscle,
                experienceRes = R.string.experience_intermediate,
                daysPerWeek = 4,
                sessionDurationMinutes = 60,
                splitRes = R.string.split_upper_lower,
                equipmentRes = listOf(R.string.equipment_bodyweight, R.string.equipment_dumbbells),
                injuries = emptyList(),
                limitationsRes = listOf(R.string.limitation_avoid_jumping),
            ),
            onEvent = {},
        )
    }
}
