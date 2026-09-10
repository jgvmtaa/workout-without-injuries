package com.jgv.workoutplanner.data.local

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.jgv.workoutplanner.data.local.model.StoredWorkoutPlan
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

/**
 * Reads and writes [StoredWorkoutPlan] as JSON for typed DataStore (spec §21).
 *
 * Mirrors [PersistedStateSerializer]: tolerant on read (ignoreUnknownKeys), complete on write
 * (encodeDefaults + prettyPrint for debuggability). Corruption resets to empty plan rather than
 * crashing the app on launch — worse to lose a generated plan than to block launch.
 */
object PersistedWorkoutPlanSerializer : Serializer<StoredWorkoutPlan> {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

    override val defaultValue: StoredWorkoutPlan = StoredWorkoutPlan(plan = null)

    override suspend fun readFrom(input: InputStream): StoredWorkoutPlan =
        try {
            json.decodeFromString(
                StoredWorkoutPlan.serializer(),
                input.readBytes().decodeToString(),
            )
        } catch (e: SerializationException) {
            throw CorruptionException("Could not read stored workout plan", e)
        }

    override suspend fun writeTo(t: StoredWorkoutPlan, output: OutputStream) {
        output.write(json.encodeToString(StoredWorkoutPlan.serializer(), t).encodeToByteArray())
    }
}
