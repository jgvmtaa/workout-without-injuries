package com.jgv.workoutplanner.testing

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * In-memory [DataStore] for repository tests.
 *
 * The [Mutex] is not decoration: `DefaultProfileRepository` relies on `updateData`
 * serialising concurrent read-modify-write, so a double without that guarantee would
 * quietly make a concurrency test meaningless.
 */
class FakeDataStore<T>(initialValue: T) : DataStore<T> {

    private val state = MutableStateFlow(initialValue)
    private val writeLock = Mutex()

    override val data: Flow<T> = state.asStateFlow()

    /** The stored value right now, for assertions that do not need to collect. */
    val current: T get() = state.value

    override suspend fun updateData(transform: suspend (t: T) -> T): T =
        writeLock.withLock {
            val updated = transform(state.value)
            state.value = updated
            updated
        }
}
