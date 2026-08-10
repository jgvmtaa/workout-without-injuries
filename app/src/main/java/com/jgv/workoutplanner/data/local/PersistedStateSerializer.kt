package com.jgv.workoutplanner.data.local

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.jgv.workoutplanner.data.local.model.PersistedState
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

/**
 * Reads and writes [PersistedState] as JSON for a typed DataStore (README §21).
 *
 * README §21 offers Preferences DataStore with JSON blobs or Proto DataStore. This is
 * the middle path: a typed `DataStore<PersistedState>` with a kotlinx-serialization
 * codec — the typed schema and explicit defaults of the Proto option, without adding
 * the protobuf toolchain, and using a dependency the project already has.
 *
 * [json] is deliberately configured, not defaulted:
 * - `ignoreUnknownKeys` — a file written by a newer build must still load in an older
 *   one. Without it, a downgrade is a crash loop.
 * - `encodeDefaults` — otherwise a field left at its default is omitted, and the stored
 *   file stops being a complete record of state.
 * - `prettyPrint` — the file is small and being able to read it during development is
 *   worth the bytes.
 */
object PersistedStateSerializer : Serializer<PersistedState> {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

    override val defaultValue: PersistedState = PersistedState()

    override suspend fun readFrom(input: InputStream): PersistedState =
        try {
            json.decodeFromString(
                PersistedState.serializer(),
                input.readBytes().decodeToString(),
            )
        } catch (e: SerializationException) {
            // Surfaced to the DataStore's corruption handler, which resets the file to
            // [defaultValue]. Losing the profile is bad; refusing to start is worse, and
            // an unparseable file cannot be recovered from here.
            throw CorruptionException("Could not read the stored profile", e)
        }

    override suspend fun writeTo(t: PersistedState, output: OutputStream) {
        output.write(
            json.encodeToString(PersistedState.serializer(), t).encodeToByteArray(),
        )
    }
}
