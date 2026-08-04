package com.jgv.workoutplanner

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point. [HiltAndroidApp] triggers Hilt's code generation and
 * creates the app-level dependency container that all other components hang off.
 */
@HiltAndroidApp
class WorkoutPlannerApp : Application()
