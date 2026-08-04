package com.jgv.workoutplanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.jgv.workoutplanner.ui.theme.WorkoutPlannerTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single-activity host for the app. Phase 0 renders a blank Compose surface only;
 * the navigation shell and screens arrive in later phases.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WorkoutPlannerTheme {
                PlaceholderSurface()
            }
        }
    }
}

@Composable
private fun PlaceholderSurface() {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "Workout Planner")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlaceholderSurfacePreview() {
    WorkoutPlannerTheme {
        PlaceholderSurface()
    }
}
