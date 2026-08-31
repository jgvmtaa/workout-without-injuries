package com.jgv.workoutplanner.di

import com.jgv.workoutplanner.data.repository.DefaultExerciseRepository
import com.jgv.workoutplanner.data.repository.DefaultInjuryRepository
import com.jgv.workoutplanner.data.repository.DefaultProfileRepository
import com.jgv.workoutplanner.data.repository.DefaultWorkoutPlanRepository
import com.jgv.workoutplanner.domain.repository.ExerciseRepository
import com.jgv.workoutplanner.domain.repository.InjuryRepository
import com.jgv.workoutplanner.domain.repository.ProfileRepository
import com.jgv.workoutplanner.domain.repository.WorkoutPlanRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds repository interfaces to their implementations (README §22).
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

    @Binds
    @Singleton
    abstract fun bindWorkoutPlanRepository(impl: DefaultWorkoutPlanRepository): WorkoutPlanRepository
}
