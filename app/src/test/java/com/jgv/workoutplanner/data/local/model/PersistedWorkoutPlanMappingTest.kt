package com.jgv.workoutplanner.data.local.model

import com.jgv.workoutplanner.domain.model.ExerciseId
import com.jgv.workoutplanner.domain.model.PlannedExercise
import com.jgv.workoutplanner.domain.model.WorkoutDay
import com.jgv.workoutplanner.domain.model.WorkoutDayFocus
import com.jgv.workoutplanner.domain.model.WorkoutPlan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class PersistedWorkoutPlanMappingTest {

    @Test
    fun `round-trip preserves plan`() {
        val plan = WorkoutPlan(
            id = "plan-4-UPPER_LOWER",
            name = "UPPER_LOWER - 4 days",
            days = listOf(
                WorkoutDay(
                    id = "plan-4-UPPER_LOWER-day-0-upper_body",
                    name = "Upper 1",
                    focus = WorkoutDayFocus.UPPER_BODY,
                    exercises = listOf(
                        PlannedExercise(ExerciseId.MACHINE_CHEST_PRESS, 3, 8..12, 90, 0),
                        PlannedExercise(ExerciseId.LAT_PULLDOWN, 3, 8..12, 90, 1),
                    ),
                ),
                WorkoutDay(
                    id = "plan-4-UPPER_LOWER-day-1-lower_body",
                    name = "Lower 1",
                    focus = WorkoutDayFocus.LOWER_BODY,
                    exercises = listOf(
                        PlannedExercise(ExerciseId.GOBLET_SQUAT, 3, 8..12, 90, 0),
                    ),
                ),
            ),
        )

        val persisted = plan.toPersisted()
        val restored = StoredWorkoutPlan(persisted).toDomain()

        assertNotNull(restored)
        assertEquals(plan.id, restored!!.id)
        assertEquals(plan.name, restored.name)
        assertEquals(plan.days.size, restored.days.size)
        assertEquals(plan.days[0].id, restored.days[0].id)
        assertEquals(plan.days[0].exercises.map { it.exerciseId }, restored.days[0].exercises.map { it.exerciseId })
    }

    @Test
    fun `unknown exercise id is dropped not crash`() {
        val persisted = PersistedWorkoutPlan(
            id = "plan-3-PUSH_PULL_LEGS",
            name = "test",
            days = listOf(
                PersistedWorkoutDay(
                    id = "day-0",
                    name = "Push",
                    focus = WorkoutDayFocus.PUSH.name,
                    exercises = listOf(
                        PersistedPlannedExercise("MACHINE_CHEST_PRESS", 3, 8, 12, 90, 0),
                        PersistedPlannedExercise("UNKNOWN_EXERCISE", 3, 8, 12, 90, 1),
                    ),
                ),
            ),
        )

        val domain = StoredWorkoutPlan(persisted).toDomain()
        assertNotNull(domain)
        assertEquals(1, domain!!.days.first().exercises.size)
        assertEquals(ExerciseId.MACHINE_CHEST_PRESS, domain.days.first().exercises.first().exerciseId)
    }

    @Test
    fun `deterministic plan id format`() {
        val plan = WorkoutPlan(id = "plan-2-FULL_BODY", name = "FULL_BODY - 2 days", days = emptyList())
        val persisted = plan.toPersisted()
        assertEquals("plan-2-FULL_BODY", persisted.id)
    }
}
