package com.jgv.workoutplanner.data.local

import androidx.datastore.core.CorruptionException
import com.jgv.workoutplanner.data.local.model.PersistedState
import com.jgv.workoutplanner.data.local.model.toPersisted
import com.jgv.workoutplanner.domain.model.MovementLimitation
import com.jgv.workoutplanner.testing.completeDraft
import com.jgv.workoutplanner.testing.userProfile
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

/** The JSON codec behind the typed DataStore (README §21). */
class PersistedStateSerializerTest {

    @Test
    fun `the default is an empty state with the current schema version`() {
        val default = PersistedStateSerializer.defaultValue

        assertEquals(PersistedState.CURRENT_SCHEMA_VERSION, default.schemaVersion)
        assertNull(default.profile)
        assertEquals(false, default.draft.hasAcceptedSafetyNotice)
    }

    @Test
    fun `a state round-trips through the stream`() = runTest {
        val state = PersistedState(
            draft = completeDraft(
                confirmedLimitations = setOf(MovementLimitation.AVOID_JUMPING),
            ).toPersisted(),
            profile = userProfile().toPersisted(),
        )

        assertEquals(state, roundTrip(state))
    }

    @Test
    fun `the default round-trips`() = runTest {
        assertEquals(
            PersistedStateSerializer.defaultValue,
            roundTrip(PersistedStateSerializer.defaultValue),
        )
    }

    /**
     * Fields left at their default must still be written, or the file stops being a
     * complete record and a later build reading it cannot tell "false" from "absent".
     */
    @Test
    fun `defaults are written to the file`() = runTest {
        val json = encode(PersistedStateSerializer.defaultValue)

        assertTrue("schemaVersion missing", json.contains("\"schemaVersion\""))
        assertTrue("safety ack missing", json.contains("\"hasAcceptedSafetyNotice\""))
    }

    /**
     * A downgrade must not be a crash loop: a file written by a build that knows more
     * fields loses the extras and loads.
     */
    @Test
    fun `an unknown field from a newer build is ignored`() = runTest {
        val json = """
            {
              "schemaVersion": 1,
              "draft": { "hasAcceptedSafetyNotice": true, "favouriteColour": "green" },
              "profile": null,
              "somethingFromTheFuture": [1, 2, 3]
            }
        """.trimIndent()

        val decoded = PersistedStateSerializer.readFrom(ByteArrayInputStream(json.toByteArray()))

        assertEquals(true, decoded.draft.hasAcceptedSafetyNotice)
    }

    /**
     * An unreadable file surfaces as [CorruptionException] so DataStore's corruption
     * handler can reset it. Anything else would propagate out of `dataStore.data` and
     * take the app down on every launch.
     */
    @Test(expected = CorruptionException::class)
    fun `unparseable content raises a corruption exception`() = runTest {
        PersistedStateSerializer.readFrom(ByteArrayInputStream("not json".toByteArray()))
    }

    @Test(expected = CorruptionException::class)
    fun `a truncated file raises a corruption exception`() = runTest {
        PersistedStateSerializer.readFrom(ByteArrayInputStream("""{"schemaVer""".toByteArray()))
    }

    private suspend fun roundTrip(state: PersistedState): PersistedState =
        PersistedStateSerializer.readFrom(ByteArrayInputStream(encode(state).toByteArray()))

    private suspend fun encode(state: PersistedState): String {
        val out = ByteArrayOutputStream()
        PersistedStateSerializer.writeTo(state, out)
        return out.toString(Charsets.UTF_8.name())
    }
}
