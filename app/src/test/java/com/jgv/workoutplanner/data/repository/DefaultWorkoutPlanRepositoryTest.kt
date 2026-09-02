package com.jgv.workoutplanner.data.repository

import app.cash.turbine.test
import com.jgv.workoutplanner.data.local.WorkoutPlanDataStore
import com.jgv.workoutplanner.data.local.model.StoredWorkoutPlan
import com.jgv.workoutplanner.domain.model.ExerciseId
import com.jgv.workoutplanner.domain.model.PlanWarning
import com.jgv.workoutplanner.domain.model.PlannedExercise
import com.jgv.workoutplanner.domain.model.WorkoutDay
import com.jgv.workoutplanner.domain.model.WorkoutDayFocus
import com.jgv.workoutplanner.domain.model.WorkoutPlan
import com.jgv.workoutplanner.domain.repository.WorkoutPlanRepository
import com.jgv.workoutplanner.testing.FakeDataStore
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultWorkoutPlanRepositoryTest {

    private fun samplePlan(id: String = "plan"): WorkoutPlan {
        val day = WorkoutDay(
            id = "day-1",
            name = "Day 1",
            focus = WorkoutDayFocus.FULL_BODY,
            exercises = listOf(PlannedExercise(ExerciseId.PUSH_UP, 3, 8..12, 90, 0)),
        )
        return WorkoutPlan(id = id, name = "Name $id", days = listOf(day))
    }

    @Test
    fun `saveGeneratedPlan clears outdated flag`() = runTest {
        val dataStore = FakeDataStore(StoredWorkoutPlan(plan = null, warnings = emptyList(), requiresRegeneration = true))
        val repo = DefaultWorkoutPlanRepository(WorkoutPlanDataStore(dataStore))

        val plan = samplePlan()
        repo.saveGeneratedPlan(plan, emptyList())

        repo.requiresRegeneration.test {
            assertFalse(awaitItem())
        }
        repo.currentPlan.test {
            assertEquals(plan.id, awaitItem()?.id)
        }
    }

    @Test
    fun `markRequiresRegeneration sets flag only when plan exists`() = runTest {
        val dataStore = FakeDataStore(StoredWorkoutPlan(plan = null))
        val repo = DefaultWorkoutPlanRepository(WorkoutPlanDataStore(dataStore))

        val hadPlanWhenEmpty = repo.markRequiresRegeneration()
        assertFalse(hadPlanWhenEmpty)

        repo.saveGeneratedPlan(samplePlan(), emptyList())
        val hadPlanAfterSave = repo.markRequiresRegeneration()
        assertTrue(hadPlanAfterSave)

        repo.requiresRegeneration.test {
            assertTrue(awaitItem())
        }
    }

    @Test
    fun `updatePlanAtomically preserves warnings and requiresRegeneration`() = runTest {
        val warning = PlanWarning(0, WorkoutDayFocus.FULL_BODY, "slot", "reason")
        val initial = StoredWorkoutPlan(
            plan = samplePlan().let { plan ->
                com.jgv.workoutplanner.data.local.model.PersistedWorkoutPlan(
                    id = plan.id,
                    name = plan.name,
                    days = plan.days.map { day ->
                        com.jgv.workoutplanner.data.local.model.PersistedWorkoutDay(
                            id = day.id,
                            name = day.name,
                            focus = day.focus.name,
                            exercises = day.exercises.map { pe ->
                                com.jgv.workoutplanner.data.local.model.PersistedPlannedExercise(
                                    exerciseId = pe.exerciseId.name,
                                    sets = pe.sets,
                                    repStart = pe.repRange.first,
                                    repEnd = pe.repRange.last,
                                    restSeconds = pe.restSeconds,
                                    order = pe.order,
                                )
                            },
                        )
                    },
                )
            },
            warnings = listOf(
                com.jgv.workoutplanner.data.local.model.PersistedPlanWarning(
                    dayIndex = warning.dayIndex,
                    dayFocus = warning.dayFocus.name,
                    slotId = warning.slotId,
                    reason = warning.reason,
                ),
            ),
            requiresRegeneration = true,
        )
        val dataStore = FakeDataStore(initial)
        val repo = DefaultWorkoutPlanRepository(WorkoutPlanDataStore(dataStore))

        // Commit new plan via atomic mutation
        val newPlan = samplePlan("new-plan")
        val result = repo.updatePlanAtomically { snapshot ->
            // snapshot should contain requiresRegeneration true
            assertTrue(snapshot.requiresRegeneration)
            WorkoutPlanRepository.AtomicPlanMutation.Commit(newPlan, "ok")
        }

        assertEquals("ok", result)

        // warnings preserved
        repo.currentWarnings.test {
            val w = awaitItem()
            assertEquals(1, w.size)
            assertEquals("reason", w.first().reason)
        }

        // flag preserved (manual edit cannot clear)
        repo.requiresRegeneration.test {
            assertTrue(awaitItem())
        }

        // plan replaced
        repo.currentPlan.test {
            assertEquals("new-plan", awaitItem()?.id)
        }
    }

    @Test
    fun `updatePlanAtomically Reject writes nothing`() = runTest {
        val dataStore = FakeDataStore(
            StoredWorkoutPlan(
                plan = samplePlan().let { plan ->
                    com.jgv.workoutplanner.data.local.model.PersistedWorkoutPlan(
                        id = plan.id,
                        name = plan.name,
                        days = plan.days.map { day ->
                            com.jgv.workoutplanner.data.local.model.PersistedWorkoutDay(
                                id = day.id,
                                name = day.name,
                                focus = day.focus.name,
                                exercises = day.exercises.map { pe ->
                                    com.jgv.workoutplanner.data.local.model.PersistedPlannedExercise(
                                        exerciseId = pe.exerciseId.name,
                                        sets = pe.sets,
                                        repStart = pe.repRange.first,
                                        repEnd = pe.repRange.last,
                                        restSeconds = pe.restSeconds,
                                        order = pe.order,
                                    )
                                },
                            )
                        },
                    )
                },
                warnings = emptyList(),
                requiresRegeneration = false,
            ),
        )
        val repo = DefaultWorkoutPlanRepository(WorkoutPlanDataStore(dataStore))

        val result = repo.updatePlanAtomically { _ ->
            WorkoutPlanRepository.AtomicPlanMutation.Reject("rejected")
        }

        assertEquals("rejected", result)

        repo.currentPlan.test {
            assertEquals("plan", awaitItem()?.id)
        }
    }

    @Test
    fun `clearPlan removes plan warnings and flag`() = runTest {
        val dataStore = FakeDataStore(
            StoredWorkoutPlan(
                plan = samplePlan().let { plan ->
                    com.jgv.workoutplanner.data.local.model.PersistedWorkoutPlan(
                        id = plan.id,
                        name = plan.name,
                        days = listOf(),
                    )
                },
                warnings = listOf(
                    com.jgv.workoutplanner.data.local.model.PersistedPlanWarning(0, "FULL_BODY", "s", "r"),
                ),
                requiresRegeneration = true,
            ),
        )
        val repo = DefaultWorkoutPlanRepository(WorkoutPlanDataStore(dataStore))

        repo.clearPlan()

        repo.currentPlan.test { assertEquals(null, awaitItem()) }
        repo.currentWarnings.test { assertTrue(awaitItem().isEmpty()) }
        repo.requiresRegeneration.test { assertFalse(awaitItem()) }
    }
}
