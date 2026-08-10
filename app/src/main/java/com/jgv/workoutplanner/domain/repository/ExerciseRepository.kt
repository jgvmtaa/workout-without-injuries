package com.jgv.workoutplanner.domain.repository

import com.jgv.workoutplanner.domain.model.ExerciseDefinition
import com.jgv.workoutplanner.domain.model.ExerciseId
import com.jgv.workoutplanner.domain.model.MovementPattern
import com.jgv.workoutplanner.domain.model.MuscleGroup

/**
 * Read access to the exercise catalog (README §19).
 *
 * Synchronous and non-suspending: the MVP catalog is a compiled-in list, and pretending
 * otherwise would push coroutine machinery into every caller for no benefit. Moving the
 * catalog to a file or a server (README §6) will change these to `suspend` — a small,
 * contained break precisely because callers depend on this interface and not on
 * [com.jgv.workoutplanner.data.catalog.ExerciseCatalog].
 */
interface ExerciseRepository {

    /** Every exercise in the catalog, in catalog order. */
    fun getAllExercises(): List<ExerciseDefinition>

    /**
     * The definition behind [id].
     *
     * Non-null by construction: the catalog covers every [ExerciseId] and a test
     * enforces it, so a saved plan can always resolve what it references.
     */
    fun getExercise(id: ExerciseId): ExerciseDefinition

    /**
     * Exercises training [muscleGroup].
     *
     * @param includeSecondary when true, also returns exercises that train the group as
     *   a secondary muscle. The library browses primary only (README §14); plan
     *   generation counts secondary work when balancing a day.
     */
    fun getExercisesByMuscleGroup(
        muscleGroup: MuscleGroup,
        includeSecondary: Boolean = false,
    ): List<ExerciseDefinition>

    /**
     * Exercises built on [movementPattern] — the basis for substitution, since sharing a
     * pattern is what makes one exercise a stand-in for another (README §13).
     */
    fun getExercisesByMovementPattern(movementPattern: MovementPattern): List<ExerciseDefinition>
}
