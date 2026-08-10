package com.jgv.workoutplanner.data.catalog

import com.jgv.workoutplanner.domain.model.Equipment
import com.jgv.workoutplanner.domain.model.ExerciseId
import com.jgv.workoutplanner.domain.model.ExerciseTag
import com.jgv.workoutplanner.domain.model.MovementPattern
import com.jgv.workoutplanner.domain.model.MuscleGroup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Structural guarantees for the exercise catalog (README §26, Phase 2 task 2.7).
 *
 * The catalog is hand-written data, and the failure modes are the ones humans have when
 * editing sixty near-identical blocks: a duplicated id, a copy-pasted name that was
 * never updated, a muscle group that quietly drifted out of range, a new movement
 * pattern with nothing to substitute for it. None of those break the build and none are
 * visible by reading the diff. These tests are what makes them visible.
 */
class ExerciseCatalogTest {

    private val exercises = ExerciseCatalog.exercises

    /** README §26: aim for roughly 45–60 exercises. */
    @Test
    fun `catalog size is within the target range`() {
        assertTrue(
            "Expected 45–60 exercises, found ${exercises.size}",
            exercises.size in 45..60,
        )
    }

    @Test
    fun `every exercise id has exactly one catalog entry`() {
        val idsInCatalog = exercises.map { it.id }

        val duplicates = idsInCatalog.groupBy { it }.filterValues { it.size > 1 }.keys
        assertEquals("Duplicate catalog entries", emptySet<ExerciseId>(), duplicates)

        val missing = ExerciseId.entries.toSet() - idsInCatalog.toSet()
        assertEquals("Exercise ids with no catalog entry", emptySet<ExerciseId>(), missing)
    }

    /**
     * A missing string resource is a compile error, so what is left to check is that no
     * two exercises point at the *same* copy — the symptom of a copy-pasted entry.
     */
    @Test
    fun `every exercise has its own name and description`() {
        val sharedNames = exercises
            .groupBy { it.nameRes }
            .filterValues { it.size > 1 }
            .values
            .map { group -> group.map { it.id } }
        assertEquals("Exercises sharing a name resource", emptyList<List<ExerciseId>>(), sharedNames)

        val sharedDescriptions = exercises
            .groupBy { it.descriptionRes }
            .filterValues { it.size > 1 }
            .values
            .map { group -> group.map { it.id } }
        assertEquals(
            "Exercises sharing a description resource",
            emptyList<List<ExerciseId>>(),
            sharedDescriptions,
        )

        val nameIsAlsoDescription = exercises.filter { it.nameRes == it.descriptionRes }.map { it.id }
        assertEquals(
            "Exercises using one resource as both name and description",
            emptyList<ExerciseId>(),
            nameIsAlsoDescription,
        )
    }

    /** README §26 sets a per-muscle distribution so no muscle group is left thin. */
    @Test
    fun `primary muscle distribution matches the target ranges`() {
        val targets = mapOf(
            MuscleGroup.CHEST to 5..7,
            MuscleGroup.BACK to 7..9,
            MuscleGroup.SHOULDERS to 6..8,
            MuscleGroup.BICEPS to 3..5,
            MuscleGroup.TRICEPS to 4..6,
            MuscleGroup.QUADRICEPS to 6..8,
            MuscleGroup.HAMSTRINGS to 4..6,
            MuscleGroup.GLUTES to 5..7,
            MuscleGroup.CALVES to 2..3,
            MuscleGroup.CORE to 5..7,
        )

        // Every muscle group needs a target, or a new one could be added untested.
        assertEquals(
            "Muscle groups with no distribution target",
            emptySet<MuscleGroup>(),
            MuscleGroup.entries.toSet() - targets.keys,
        )

        val counts = exercises.groupingBy { it.primaryMuscle }.eachCount()
        val outOfRange = targets.filter { (group, range) -> counts[group] !in range }
        assertEquals(
            "Muscle groups outside their target range " +
                "(actual: ${outOfRange.keys.associateWith { counts[it] ?: 0 }})",
            emptyMap<MuscleGroup, IntRange>(),
            outOfRange,
        )
    }

    /**
     * README §26: every movement needs substitutes across equipment types, "because
     * filtering only works well when each movement has several possible substitutes".
     *
     * Bodyweight-only patterns are exempt from the equipment requirement — they are
     * available to everyone by definition, so there is nothing to substitute *for*. They
     * still need a second exercise, so a limitation exclusion leaves an alternative.
     */
    @Test
    fun `every movement pattern has alternatives`() {
        val byPattern = exercises.groupBy { it.movementPattern }

        val tooFewExercises = byPattern.filterValues { it.size < 2 }.keys
        assertEquals(
            "Movement patterns with fewer than 2 exercises",
            emptySet<MovementPattern>(),
            tooFewExercises,
        )

        val lackingEquipmentAlternatives = byPattern
            .filterValues { group ->
                val bodyweightOnly = group.all { it.requiredEquipment == setOf(Equipment.BODYWEIGHT) }
                val distinctEquipment = group.map { it.requiredEquipment }.toSet()
                !bodyweightOnly && distinctEquipment.size < 2
            }
            .keys
        assertEquals(
            "Movement patterns with fewer than 2 equipment alternatives",
            emptySet<MovementPattern>(),
            lackingEquipmentAlternatives,
        )
    }

    @Test
    fun `every exercise requires at least one piece of equipment`() {
        // Bodyweight is modelled as equipment, so an empty set means the entry is
        // incomplete rather than that the exercise needs nothing.
        val withoutEquipment = exercises.filter { it.requiredEquipment.isEmpty() }.map { it.id }
        assertEquals(emptyList<ExerciseId>(), withoutEquipment)
    }

    @Test
    fun `secondary muscles never repeat the primary muscle`() {
        val overlapping = exercises
            .filter { it.primaryMuscle in it.secondaryMuscles }
            .map { it.id }
        assertEquals(emptyList<ExerciseId>(), overlapping)
    }

    @Test
    fun `every exercise is tagged either compound or isolation, and never both`() {
        val misTagged = exercises
            .filterNot { exercise ->
                val compound = ExerciseTag.COMPOUND in exercise.tags
                val isolation = ExerciseTag.ISOLATION in exercise.tags
                compound xor isolation
            }
            .map { it.id }
        assertEquals(emptyList<ExerciseId>(), misTagged)
    }

    @Test
    fun `every exercise is tagged either unilateral or bilateral, and never both`() {
        val misTagged = exercises
            .filterNot { exercise ->
                val unilateral = ExerciseTag.UNILATERAL in exercise.tags
                val bilateral = ExerciseTag.BILATERAL in exercise.tags
                unilateral xor bilateral
            }
            .map { it.id }
        assertEquals(emptyList<ExerciseId>(), misTagged)
    }

    @Test
    fun `bodyweight tag matches bodyweight equipment`() {
        val inconsistent = exercises
            .filterNot { exercise ->
                val tagged = ExerciseTag.BODYWEIGHT in exercise.tags
                val requiresBodyweightOnly =
                    exercise.requiredEquipment == setOf(Equipment.BODYWEIGHT)
                // A bench or bar can carry the tag (a step-up is still bodyweight work),
                // but bodyweight-only equipment must carry it.
                !requiresBodyweightOnly || tagged
            }
            .map { it.id }
        assertEquals(
            "Bodyweight-only exercises missing the BODYWEIGHT tag",
            emptyList<ExerciseId>(),
            inconsistent,
        )
    }

    @Test
    fun `every prescription is usable`() {
        val invalid = exercises.filterNot { exercise ->
            val prescription = exercise.defaultPrescription
            prescription.sets.first >= 1 &&
                prescription.sets.last >= prescription.sets.first &&
                prescription.reps.first >= 1 &&
                prescription.reps.last >= prescription.reps.first &&
                prescription.restSeconds > 0
        }.map { it.id }
        assertEquals(emptyList<ExerciseId>(), invalid)
    }

    /**
     * The filter is only as good as this field, and an entry with no conflicts is a
     * claim that the movement is safe under every limitation. That is true of a few
     * exercises — it is what makes them the fallbacks a heavily-limited user is left
     * with — so this asserts the claim is deliberate rather than forgotten.
     */
    @Test
    fun `exercises with no conflicting limitations are the expected ones`() {
        val unconflicted = exercises
            .filter { it.conflictingLimitations.isEmpty() }
            .map { it.id }
            .toSet()

        val expected = setOf(
            ExerciseId.CHEST_SUPPORTED_DUMBBELL_ROW,
            ExerciseId.ISO_LATERAL_ROW,
            ExerciseId.BAND_ROW,
            ExerciseId.CABLE_REAR_DELT_FLY,
            ExerciseId.FACE_PULL,
            ExerciseId.BAND_PULL_APART,
            ExerciseId.STANDING_CALF_RAISE,
            ExerciseId.SEATED_CALF_RAISE,
            ExerciseId.DEAD_BUG,
            ExerciseId.FRONT_PLANK,
            ExerciseId.SIDE_PLANK,
            ExerciseId.PALLOF_PRESS,
        )

        assertEquals(expected, unconflicted)
    }
}
