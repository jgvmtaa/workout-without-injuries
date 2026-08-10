package com.jgv.workoutplanner.data.repository

import com.jgv.workoutplanner.data.catalog.ExerciseCatalog
import com.jgv.workoutplanner.domain.model.ExerciseDefinition
import com.jgv.workoutplanner.domain.model.ExerciseId
import com.jgv.workoutplanner.domain.model.MovementPattern
import com.jgv.workoutplanner.domain.model.MuscleGroup
import com.jgv.workoutplanner.domain.repository.ExerciseRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [ExerciseRepository] backed by the static [ExerciseCatalog] (README §19, §27 Phase 2).
 *
 * Indexes the catalog once at construction. The lists are small enough that scanning
 * would be fine, but the library and the plan generator query by muscle group and
 * pattern repeatedly, and an index keeps that off the main thread's critical path
 * without any caller having to think about it.
 *
 * A `@Singleton` so the indexes are built once per process.
 */
@Singleton
class DefaultExerciseRepository @Inject constructor() : ExerciseRepository {

    private val exercises: List<ExerciseDefinition> = ExerciseCatalog.exercises

    private val byId: Map<ExerciseId, ExerciseDefinition> = exercises.associateBy { it.id }

    private val byPrimaryMuscle: Map<MuscleGroup, List<ExerciseDefinition>> =
        exercises.groupBy { it.primaryMuscle }

    private val byAnyMuscle: Map<MuscleGroup, List<ExerciseDefinition>> =
        exercises
            .flatMap { exercise ->
                (exercise.secondaryMuscles + exercise.primaryMuscle).map { it to exercise }
            }
            .groupBy(keySelector = { it.first }, valueTransform = { it.second })

    private val byMovementPattern: Map<MovementPattern, List<ExerciseDefinition>> =
        exercises.groupBy { it.movementPattern }

    override fun getAllExercises(): List<ExerciseDefinition> = exercises

    override fun getExercise(id: ExerciseId): ExerciseDefinition =
        // Cannot happen: ExerciseCatalogTest asserts one entry per id. If it ever does,
        // the catalog is broken and failing loudly beats returning something plausible.
        requireNotNull(byId[id]) { "No catalog entry for exercise id $id" }

    override fun getExercisesByMuscleGroup(
        muscleGroup: MuscleGroup,
        includeSecondary: Boolean,
    ): List<ExerciseDefinition> {
        val index = if (includeSecondary) byAnyMuscle else byPrimaryMuscle
        return index[muscleGroup].orEmpty()
    }

    override fun getExercisesByMovementPattern(
        movementPattern: MovementPattern,
    ): List<ExerciseDefinition> = byMovementPattern[movementPattern].orEmpty()
}
