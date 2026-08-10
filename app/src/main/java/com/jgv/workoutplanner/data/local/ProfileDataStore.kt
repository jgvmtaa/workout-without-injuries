package com.jgv.workoutplanner.data.local

import androidx.datastore.core.DataStore
import com.jgv.workoutplanner.data.local.model.PersistedState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The app's typed local store for onboarding and profile state (README §19, §21).
 *
 * A thin wrapper over `DataStore<PersistedState>` rather than a layer with opinions:
 * it exists so that the file name and the storage type are declared in one place, and
 * so callers depend on something nameable instead of a generic. Domain mapping belongs
 * one level up in
 * [com.jgv.workoutplanner.data.repository.DefaultProfileRepository]; the DataStore
 * instance itself is built in the DI module, which is where the `Context` is.
 */
@Singleton
class ProfileDataStore @Inject constructor(
    private val dataStore: DataStore<PersistedState>,
) {

    /**
     * The stored state, re-emitting on every write.
     *
     * DataStore guarantees the first emission reflects what is on disk, so a collector
     * never sees a default that is about to be replaced by real data.
     */
    val state: Flow<PersistedState> = dataStore.data

    /**
     * Applies [transform] to the stored state and returns the result.
     *
     * `updateData` serialises concurrent updates, so read-modify-write from two screens
     * cannot interleave and lose one of them.
     */
    suspend fun update(transform: (PersistedState) -> PersistedState): PersistedState =
        dataStore.updateData(transform)

    companion object {
        /** Name of the file under the app's `datastore` directory. */
        const val FILE_NAME: String = "profile.json"
    }
}
