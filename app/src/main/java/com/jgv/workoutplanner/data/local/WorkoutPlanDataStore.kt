package com.jgv.workoutplanner.data.local

import androidx.datastore.core.DataStore
import com.jgv.workoutplanner.data.local.model.StoredWorkoutPlan
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Typed local store for the current workout plan (README §21, task 5.7).
 *
 * Thin wrapper over `DataStore<StoredWorkoutPlan>` – file name and type in one place,
 * domain mapping in [com.jgv.workoutplanner.data.repository.DefaultWorkoutPlanRepository].
 * Separate file `workout_plan.json` (not `profile.json`) per Phase 5 decision.
 */
@Singleton
class WorkoutPlanDataStore @Inject constructor(
    private val dataStore: DataStore<StoredWorkoutPlan>,
) {
    val state: Flow<StoredWorkoutPlan> = dataStore.data

    suspend fun update(transform: (StoredWorkoutPlan) -> StoredWorkoutPlan): StoredWorkoutPlan =
        dataStore.updateData(transform)

    companion object {
        const val FILE_NAME: String = "workout_plan.json"
    }
}
