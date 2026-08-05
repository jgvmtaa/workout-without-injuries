package com.jgv.workoutplanner.feature.exerciselibrary

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.jgv.workoutplanner.R
import com.jgv.workoutplanner.core.designsystem.AppTheme
import com.jgv.workoutplanner.core.ui.PlaceholderAction
import com.jgv.workoutplanner.core.ui.PlaceholderScreen

/**
 * Browsable exercise catalog with muscle-group and availability filters
 * (README §14). Placeholder until Phase 4.
 */
@Composable
fun ExerciseLibraryScreen(
    onOpenExerciseDetails: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PlaceholderScreen(
        title = stringResource(R.string.screen_exercise_library),
        modifier = modifier,
        onBack = onBack,
        actions = listOf(
            PlaceholderAction(
                label = stringResource(R.string.action_open_exercise_details),
                onClick = onOpenExerciseDetails,
            ),
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun ExerciseLibraryScreenPreview() {
    AppTheme {
        ExerciseLibraryScreen(onOpenExerciseDetails = {}, onBack = {})
    }
}
