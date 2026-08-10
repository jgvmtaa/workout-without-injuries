package com.jgv.workoutplanner.di

import com.jgv.workoutplanner.data.repository.DefaultExerciseRepository
import com.jgv.workoutplanner.data.repository.DefaultInjuryRepository
import com.jgv.workoutplanner.domain.repository.ExerciseRepository
import com.jgv.workoutplanner.domain.repository.InjuryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds repository interfaces to their implementations (README §22).
 *
 * `@Binds` rather than `@Provides`: the implementations are constructor-injectable, so
 * Hilt only needs to be told which interface they satisfy.
 *
 * Catalog-backed repositories only. `ProfileRepository` and `WorkoutPlanRepository` have
 * interfaces but no implementations until Phase 3 adds persistence; their bindings go
 * here then.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindExerciseRepository(impl: DefaultExerciseRepository): ExerciseRepository

    @Binds
    @Singleton
    abstract fun bindInjuryRepository(impl: DefaultInjuryRepository): InjuryRepository
}
