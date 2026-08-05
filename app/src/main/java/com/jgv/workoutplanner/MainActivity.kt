package com.jgv.workoutplanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.jgv.workoutplanner.core.designsystem.AppTheme
import com.jgv.workoutplanner.navigation.AppNavigation
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single-activity host (README §18, §19). It applies the theme and hands off to the
 * navigation shell; everything else lives in the feature packages.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation()
                }
            }
        }
    }
}
