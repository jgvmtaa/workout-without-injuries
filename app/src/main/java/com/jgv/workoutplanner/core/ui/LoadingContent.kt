package com.jgv.workoutplanner.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.jgv.workoutplanner.R
import com.jgv.workoutplanner.core.designsystem.AppTheme
import com.jgv.workoutplanner.core.designsystem.Dimens

/**
 * Centred progress state (spec §19 core/ui). Screens show this while their state
 * is still resolving; the real loading states arrive with the features in Phases 3–6.
 */
@Composable
fun LoadingContent(
    modifier: Modifier = Modifier,
    message: String = stringResource(R.string.state_loading),
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Dimens.ScreenPadding)
            .semantics(mergeDescendants = true) {},
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Dimens.SpacingMedium),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingContentPreview() {
    AppTheme {
        LoadingContent()
    }
}
