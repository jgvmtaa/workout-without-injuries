package com.jgv.workoutplanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jgv.workoutplanner.core.designsystem.AppTheme
import com.jgv.workoutplanner.core.ui.LoadingContent
import com.jgv.workoutplanner.navigation.AppNavigation
import com.jgv.workoutplanner.navigation.AppRoute
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single-activity host (spec §18, §19). It applies the theme, waits for the start
 * destination to be known, and hands off to the navigation shell; everything else lives
 * in the feature packages.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val startDestination by viewModel.startDestination.collectAsStateWithLifecycle()

            AppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    when (startDestination) {
                        // One frame or two while the profile is read from disk. Showing a
                        // loading state beats starting at Welcome and snapping to Home.
                        StartDestination.Undecided -> LoadingContent()
                        StartDestination.Onboarding -> AppNavigation(startDestination = AppRoute.Welcome)
                        StartDestination.Home -> AppNavigation(startDestination = AppRoute.Home)
                    }
                }
            }
        }
    }
}
