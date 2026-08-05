package com.jgv.workoutplanner.feature.onboarding.review

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.jgv.workoutplanner.R
import com.jgv.workoutplanner.core.designsystem.AppTheme
import com.jgv.workoutplanner.core.ui.PlaceholderAction
import com.jgv.workoutplanner.core.ui.PlaceholderScreen

/**
 * Final onboarding step: review the collected profile before the first plan is
 * generated (README §3, §4). Placeholder until Phase 3.
 */
@Composable
fun ProfileReviewScreen(
    onFinish: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PlaceholderScreen(
        title = stringResource(R.string.screen_profile_review),
        modifier = modifier,
        onBack = onBack,
        actions = listOf(
            PlaceholderAction(
                label = stringResource(R.string.action_finish_setup),
                onClick = onFinish,
            ),
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun ProfileReviewScreenPreview() {
    AppTheme {
        ProfileReviewScreen(onFinish = {}, onBack = {})
    }
}
