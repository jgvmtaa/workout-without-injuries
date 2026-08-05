package com.jgv.workoutplanner.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Application-scoped bindings (README §22).
 *
 * Empty on purpose: Phase 1 has nothing to provide yet. It exists so the later
 * phases have one obvious place to add app-singleton dependencies —
 *
 *  - Phase 2: the static exercise and injury catalogs, plus repository bindings.
 *  - Phase 3: the DataStore instances behind profile and plan persistence.
 *
 * Use cases are constructor-injected and need no entry here. ViewModels are provided
 * by `@HiltViewModel` and obtained with `hiltViewModel()`.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule
