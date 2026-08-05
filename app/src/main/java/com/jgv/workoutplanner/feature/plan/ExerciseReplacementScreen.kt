package com.jgv.workoutplanner.feature.plan

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.jgv.workoutplanner.R
import com.jgv.workoutplanner.core.designsystem.AppTheme
import com.jgv.workoutplanner.core.ui.PlaceholderAction
import com.jgv.workoutplanner.core.ui.PlaceholderScreen

/**
 * Eligible alternatives for one exercise in one day of the plan (README §13).
 * Placeholder until Phase 6.
 */
@Composable
fun ExerciseReplacementScreen(
    workoutDayId: String,
    exerciseId: String,
    onReplacementChosen: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PlaceholderScreen(
        title = stringResource(R.string.screen_exercise_replacement),
        modifier = modifier,
        onBack = onBack,
        supportingText = stringResource(
            R.string.placeholder_replacement_context,
            exerciseId,
            workoutDayId,
        ),
        actions = listOf(
            PlaceholderAction(
                label = stringResource(R.string.action_choose_replacement),
                onClick = onReplacementChosen,
            ),
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun ExerciseReplacementScreenPreview() {
    AppTheme {
        ExerciseReplacementScreen(
            workoutDayId = "day-1",
            exerciseId = "MACHINE_CHEST_PRESS",
            onReplacementChosen = {},
            onBack = {},
        )
    }
}
