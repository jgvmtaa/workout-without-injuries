package com.jgv.workoutplanner.data.local

import com.jgv.workoutplanner.data.local.model.PersistedPlanWarning
import com.jgv.workoutplanner.data.local.model.PersistedPlannedExercise
import com.jgv.workoutplanner.data.local.model.PersistedWorkoutDay
import com.jgv.workoutplanner.data.local.model.PersistedWorkoutPlan
import com.jgv.workoutplanner.data.local.model.StoredWorkoutPlan
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class PersistedWorkoutPlanSerializerTest {
    @Test
    fun `edited plan warnings and invalidation survive serialization`() = runTest {
        val stored = StoredWorkoutPlan(
            plan = PersistedWorkoutPlan(
                id = "edited-plan",
                name = "Edited plan",
                days = listOf(
                    PersistedWorkoutDay(
                        id = "day-1",
                        name = "Day 1",
                        focus = "FULL_BODY",
                        exercises = listOf(
                            PersistedPlannedExercise("MACHINE_CHEST_PRESS", 4, 6, 10, 120, 0),
                            PersistedPlannedExercise("DEAD_BUG", 3, 10, 15, 45, 1),
                        ),
                    ),
                ),
            ),
            warnings = listOf(PersistedPlanWarning(0, "FULL_BODY", "core", "No match")),
            requiresRegeneration = true,
        )
        val output = ByteArrayOutputStream()

        PersistedWorkoutPlanSerializer.writeTo(stored, output)
        val restored = PersistedWorkoutPlanSerializer.readFrom(ByteArrayInputStream(output.toByteArray()))

        assertEquals(stored, restored)
    }

    @Test
    fun `legacy payload without invalidation flag defaults to current`() = runTest {
        val legacyJson = """
            {
              "plan": null,
              "warnings": []
            }
        """.trimIndent()

        val restored = PersistedWorkoutPlanSerializer.readFrom(
            ByteArrayInputStream(legacyJson.encodeToByteArray()),
        )

        assertFalse(restored.requiresRegeneration)
    }
}
