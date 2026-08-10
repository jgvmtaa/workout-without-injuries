package com.jgv.workoutplanner.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Application-scoped bindings that need constructing rather than binding (README §22).
 *
 * Still empty. The catalog-backed repositories added in Phase 2 are constructor-
 * injectable, so they are bound in [RepositoryModule] with `@Binds` instead — nothing
 * about them needs a `@Provides` function. Phase 3's DataStore instances will need one,
 * and this is where they go.
 *
 * Use cases are constructor-injected and need no entry here. ViewModels are provided
 * by `@HiltViewModel` and obtained with `hiltViewModel()`.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule
