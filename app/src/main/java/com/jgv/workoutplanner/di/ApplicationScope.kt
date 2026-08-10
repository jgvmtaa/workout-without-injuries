package com.jgv.workoutplanner.di

import javax.inject.Qualifier

/**
 * Marks the process-lifetime [kotlinx.coroutines.CoroutineScope] provided by [AppModule].
 *
 * A qualifier because `CoroutineScope` is too generic a type to inject unqualified —
 * the next scope the app needs would silently collide with this one.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope
