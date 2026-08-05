package com.jgv.workoutplanner.core.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.jgv.workoutplanner.core.designsystem.AppTheme
import com.jgv.workoutplanner.core.designsystem.Dimens

/**
 * Screen container shared by every destination (README §19 core/ui): a title bar,
 * optional back affordance, and a content slot that receives the scaffold insets.
 */
@Composable
fun AppScaffold(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { AppTopBar(title = title, onBack = onBack) },
        bottomBar = bottomBar,
        content = content,
    )
}

@Preview(showBackground = true)
@Composable
private fun AppScaffoldPreview() {
    AppTheme {
        AppScaffold(title = "Plan", onBack = {}) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(Dimens.ScreenPadding),
            ) {
                Text(text = "Content goes here")
            }
        }
    }
}
