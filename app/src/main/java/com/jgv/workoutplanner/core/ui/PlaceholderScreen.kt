package com.jgv.workoutplanner.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.jgv.workoutplanner.R
import com.jgv.workoutplanner.core.designsystem.AppTheme
import com.jgv.workoutplanner.core.designsystem.Dimens

/** A button rendered by [PlaceholderScreen], used to reach a neighbouring destination. */
data class PlaceholderAction(
    val label: String,
    val onClick: () -> Unit,
)

/**
 * Temporary body shared by every Phase 1 destination: the screen's name, a note that
 * it is a placeholder, and the navigation actions that will eventually be real UI.
 *
 * Each feature screen keeps its own composable and preview, so replacing this body
 * with the real content in Phases 3–6 is a local change. Screens receive plain
 * lambdas and never a `NavController` (README §20).
 */
@Composable
fun PlaceholderScreen(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    supportingText: String? = null,
    actions: List<PlaceholderAction> = emptyList(),
) {
    AppScaffold(title = title, modifier = modifier, onBack = onBack) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(Dimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingMedium),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = stringResource(R.string.placeholder_screen_note),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (supportingText != null) {
                Text(
                    text = supportingText,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            actions.forEach { action ->
                Button(
                    onClick = action.onClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Dimens.SpacingExtraSmall),
                ) {
                    Text(text = action.label)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlaceholderScreenPreview() {
    AppTheme {
        PlaceholderScreen(
            title = "Home",
            onBack = {},
            supportingText = "Supporting detail for this destination.",
            actions = listOf(
                PlaceholderAction(label = "View plan", onClick = {}),
                PlaceholderAction(label = "Exercise library", onClick = {}),
            ),
        )
    }
}
