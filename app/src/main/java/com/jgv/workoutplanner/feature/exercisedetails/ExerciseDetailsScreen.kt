package com.jgv.workoutplanner.feature.exercisedetails

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.jgv.workoutplanner.R
import com.jgv.workoutplanner.core.designsystem.AppTheme
import com.jgv.workoutplanner.core.ui.PlaceholderScreen
import com.jgv.workoutplanner.domain.model.ExerciseId

/**
 * Detail view for one exercise, including why it is or is not available for the
 * current profile (README §15). Placeholder until Phase 4.
 */
@Composable
fun ExerciseDetailsScreen(
    exerciseId: ExerciseId,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PlaceholderScreen(
        title = stringResource(R.string.screen_exercise_details),
        modifier = modifier,
        onBack = onBack,
        // The enum name until Phase 4 resolves the catalog entry and shows its
        // `nameRes` — an id is not user-facing copy.
        supportingText = stringResource(R.string.placeholder_exercise_id, exerciseId.name),
    )
}

@Preview(showBackground = true)
@Composable
private fun ExerciseDetailsScreenPreview() {
    AppTheme {
        ExerciseDetailsScreen(exerciseId = ExerciseId.MACHINE_CHEST_PRESS, onBack = {})
    }
}
