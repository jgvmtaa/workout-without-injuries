package com.jgv.workoutplanner.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStoreFile
import com.jgv.workoutplanner.data.local.PersistedStateSerializer
import com.jgv.workoutplanner.data.local.PersistedWorkoutPlanSerializer
import com.jgv.workoutplanner.data.local.ProfileDataStore
import com.jgv.workoutplanner.data.local.WorkoutPlanDataStore
import com.jgv.workoutplanner.data.local.model.PersistedState
import com.jgv.workoutplanner.data.local.model.StoredWorkoutPlan
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

/**
 * Application-scoped bindings that need constructing rather than binding (README §22).
 *
 * Repositories are constructor-injectable and bound with `@Binds` in [RepositoryModule];
 * use cases are constructor-injected and need no entry anywhere. What lands here is the
 * DataStore, because building one needs the application `Context`, a coroutine scope,
 * and a serializer — none of which a constructor can supply on its own.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * A scope that outlives every screen, for work that must not be cancelled when the
     * caller goes away.
     *
     * [SupervisorJob] so one failed write cannot take the store down with it, and
     * [Dispatchers.IO] because everything it does is file access.
     */
    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * The typed profile store (README §21).
     *
     * [ReplaceFileCorruptionHandler] resets the file to the serializer's default if it
     * cannot be read. That loses the stored profile, which is bad — but the alternative
     * is an app that throws on every launch and cannot be recovered without clearing app
     * data, which is worse. The draft goes with it, so the user restarts onboarding
     * rather than resuming a flow backed by a file nothing can read.
     */
    @Provides
    @Singleton
    fun provideProfileDataStore(
        @ApplicationContext context: Context,
        @ApplicationScope scope: CoroutineScope,
    ): DataStore<PersistedState> = DataStoreFactory.create(
        serializer = PersistedStateSerializer,
        corruptionHandler = ReplaceFileCorruptionHandler {
            PersistedStateSerializer.defaultValue
        },
        scope = scope,
        produceFile = { context.dataStoreFile(ProfileDataStore.FILE_NAME) },
    )

    /**
     * The typed workout-plan store (README §21, task 5.7).
     *
     * Separate file `workout_plan.json` from profile. Same corruption policy: losing a
     * generated plan is recoverable by re-generating, crashing on launch is not.
     */
    @Provides
    @Singleton
    fun provideWorkoutPlanDataStore(
        @ApplicationContext context: Context,
        @ApplicationScope scope: CoroutineScope,
    ): DataStore<StoredWorkoutPlan> = DataStoreFactory.create(
        serializer = PersistedWorkoutPlanSerializer,
        corruptionHandler = ReplaceFileCorruptionHandler {
            PersistedWorkoutPlanSerializer.defaultValue
        },
        scope = scope,
        produceFile = { context.dataStoreFile(WorkoutPlanDataStore.FILE_NAME) },
    )
}
