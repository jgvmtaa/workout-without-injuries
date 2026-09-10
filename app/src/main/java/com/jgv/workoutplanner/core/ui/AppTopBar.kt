package com.jgv.workoutplanner.core.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.jgv.workoutplanner.R
import com.jgv.workoutplanner.core.designsystem.AppTheme

/**
 * Standard top app bar (spec §19 core/ui).
 *
 * Takes a plain [onBack] lambda rather than a `NavController` so screens stay
 * navigation-agnostic and previewable (spec §20).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {},
) {
    TopAppBar(
        title = { Text(text = title) },
        modifier = modifier,
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.action_back),
                    )
                }
            }
        },
        actions = { actions() },
    )
}

@Preview(showBackground = true)
@Composable
private fun AppTopBarPreview() {
    AppTheme {
        AppTopBar(title = "Exercise library", onBack = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun AppTopBarNoBackPreview() {
    AppTheme {
        AppTopBar(title = "Home")
    }
}
