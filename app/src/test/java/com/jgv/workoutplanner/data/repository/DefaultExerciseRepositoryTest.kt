package com.jgv.workoutplanner.data.repository

import com.jgv.workoutplanner.domain.model.ExerciseId
import com.jgv.workoutplanner.domain.model.MovementPattern
import com.jgv.workoutplanner.domain.model.MuscleGroup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Exercises can be queried by muscle group (spec §5.1, §7).
 */
class DefaultExerciseRepositoryTest {

    private val repository = DefaultExerciseRepository()

    @Test
    fun `returns the whole catalog`() {
        assertEquals(
            ExerciseId.entries.toSet(),
            repository.getAllExercises().map { it.id }.toSet(),
        )
    }

    @Test
    fun `resolves an exercise by id`() {
        val exercise = repository.getExercise(ExerciseId.MACHINE_CHEST_PRESS)

        assertEquals(ExerciseId.MACHINE_CHEST_PRESS, exercise.id)
        assertEquals(MuscleGroup.CHEST, exercise.primaryMuscle)
    }

    @Test
    fun `resolves every id in the enum`() {
        // Guards the non-null contract on getExercise: a plan referencing any id must
        // always be able to look it back up.
        ExerciseId.entries.forEach { id ->
            assertEquals(id, repository.getExercise(id).id)
        }
    }

    @Test
    fun `queries by muscle group return only that primary muscle`() {
        val back = repository.getExercisesByMuscleGroup(MuscleGroup.BACK)

        assertTrue(back.isNotEmpty())
        assertTrue(back.all { it.primaryMuscle == MuscleGroup.BACK })
        assertTrue(ExerciseId.LAT_PULLDOWN in back.map { it.id })
    }

    @Test
    fun `including secondary muscles widens the result`() {
        val primaryOnly = repository.getExercisesByMuscleGroup(MuscleGroup.BICEPS)
        val withSecondary =
            repository.getExercisesByMuscleGroup(MuscleGroup.BICEPS, includeSecondary = true)

        // Rows and pulldowns train biceps as secondary work.
        assertTrue(withSecondary.size > primaryOnly.size)
        assertTrue(withSecondary.map { it.id }.containsAll(primaryOnly.map { it.id }))
        assertTrue(ExerciseId.LAT_PULLDOWN in withSecondary.map { it.id })
        assertTrue(
            withSecondary.all {
                it.primaryMuscle == MuscleGroup.BICEPS || MuscleGroup.BICEPS in it.secondaryMuscles
            },
        )
    }

    @Test
    fun `including secondary muscles never duplicates an exercise`() {
        MuscleGroup.entries.forEach { group ->
            val results = repository.getExercisesByMuscleGroup(group, includeSecondary = true)
            assertEquals(
                "Duplicate results for $group",
                results.map { it.id }.distinct().size,
                results.size,
            )
        }
    }

    @Test
    fun `queries by movement pattern return only that pattern`() {
        val hinges = repository.getExercisesByMovementPattern(MovementPattern.HIP_HINGE)

        assertTrue(hinges.isNotEmpty())
        assertTrue(hinges.all { it.movementPattern == MovementPattern.HIP_HINGE })
        assertTrue(ExerciseId.DUMBBELL_ROMANIAN_DEADLIFT in hinges.map { it.id })
    }

    @Test
    fun `an unused movement pattern returns empty rather than failing`() {
        // CARRY is defined but the MVP catalog has no carries.
        assertEquals(emptyList<Nothing>(), repository.getExercisesByMovementPattern(MovementPattern.CARRY))
    }

    @Test
    fun `every muscle group has at least one exercise`() {
        MuscleGroup.entries.forEach { group ->
            assertTrue(
                "No exercises for $group",
                repository.getExercisesByMuscleGroup(group).isNotEmpty(),
            )
        }
    }
}
