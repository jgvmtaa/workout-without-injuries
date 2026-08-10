package com.jgv.workoutplanner.di

import com.jgv.workoutplanner.data.repository.DefaultExerciseRepository
import com.jgv.workoutplanner.data.repository.DefaultInjuryRepository
import com.jgv.workoutplanner.data.repository.DefaultProfileRepository
import com.jgv.workoutplanner.domain.repository.ExerciseRepository
import com.jgv.workoutplanner.domain.repository.InjuryRepository
import com.jgv.workoutplanner.domain.repository.ProfileRepository
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
 * `WorkoutPlanRepository` still has an interface and no implementation. Nothing produces
 * a plan until Phase 5 generates one, and README §27 lists plan persistence as Phase 5
 * work — binding an empty store now would only add a dependency nobody can use.
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

    @Binds
    @Singleton
    abstract fun bindProfileRepository(impl: DefaultProfileRepository): ProfileRepository
}
