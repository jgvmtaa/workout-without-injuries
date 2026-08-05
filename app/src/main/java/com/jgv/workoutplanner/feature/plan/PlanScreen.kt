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
 * The current workout plan (README §11–§13). Placeholder until Phase 5.
 *
 * The two ids handed to [onOpenExerciseDetails] and [onReplaceExercise] come from
 * the plan itself once it exists; the caller supplies stand-ins for now.
 */
@Composable
fun PlanScreen(
    onOpenExerciseDetails: () -> Unit,
    onReplaceExercise: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PlaceholderScreen(
        title = stringResource(R.string.screen_plan),
        modifier = modifier,
        onBack = onBack,
        actions = listOf(
            PlaceholderAction(
                label = stringResource(R.string.action_open_exercise_details),
                onClick = onOpenExerciseDetails,
            ),
            PlaceholderAction(
                label = stringResource(R.string.action_replace_exercise),
                onClick = onReplaceExercise,
            ),
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun PlanScreenPreview() {
    AppTheme {
        PlanScreen(
            onOpenExerciseDetails = {},
            onReplaceExercise = {},
            onBack = {},
        )
    }
}
