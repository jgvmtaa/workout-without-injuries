package com.jgv.workoutplanner.feature.exercisedetails

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.jgv.workoutplanner.R
import com.jgv.workoutplanner.core.designsystem.AppTheme
import com.jgv.workoutplanner.core.ui.PlaceholderScreen

/**
 * Detail view for one exercise, including why it is or is not available for the
 * current profile (README §15). Placeholder until Phase 4.
 */
@Composable
fun ExerciseDetailsScreen(
    exerciseId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PlaceholderScreen(
        title = stringResource(R.string.screen_exercise_details),
        modifier = modifier,
        onBack = onBack,
        supportingText = stringResource(R.string.placeholder_exercise_id, exerciseId),
    )
}

@Preview(showBackground = true)
@Composable
private fun ExerciseDetailsScreenPreview() {
    AppTheme {
        ExerciseDetailsScreen(exerciseId = "MACHINE_CHEST_PRESS", onBack = {})
    }
}
