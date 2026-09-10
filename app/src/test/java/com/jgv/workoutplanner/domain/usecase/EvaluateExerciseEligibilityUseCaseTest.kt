package com.jgv.workoutplanner.domain.usecase

import com.jgv.workoutplanner.data.catalog.ExerciseCatalog
import com.jgv.workoutplanner.domain.model.Equipment
import com.jgv.workoutplanner.domain.model.ExerciseDifficulty
import com.jgv.workoutplanner.domain.model.ExerciseId
import com.jgv.workoutplanner.domain.model.ExclusionReason
import com.jgv.workoutplanner.domain.model.ExperienceLevel
import com.jgv.workoutplanner.domain.model.MovementLimitation
import com.jgv.workoutplanner.testing.userProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Eligibility engine tests (spec §8, §24.1, §24.2).
 *
 * Runs against the real [ExerciseCatalog] rather than stubs — the interesting behaviour
 * is "a catalog entry with a given limitation is excluded when that limitation is confirmed".
 */
class EvaluateExerciseEligibilityUseCaseTest {

    private val useCase = EvaluateExerciseEligibilityUseCase()

    @Test
    fun `exercise is excluded when a conflicting limitation is selected`() {
        // Dumbbell Romanian deadlift conflicts with AVOID_UNSUPPORTED_HIP_HINGE
        val exercise = ExerciseCatalog.exercises.first { it.id == ExerciseId.DUMBBELL_ROMANIAN_DEADLIFT }
        val profile = userProfile(
            movementLimitations = setOf(MovementLimitation.AVOID_UNSUPPORTED_HIP_HINGE),
            availableEquipment = setOf(Equipment.BODYWEIGHT, Equipment.DUMBBELLS),
        )

        val result = useCase(exercise, profile)

        assertFalse(result.isEligible)
        assertTrue(
            result.exclusionReasons.any {
                it is ExclusionReason.ConflictingLimitation &&
                    it.limitation == MovementLimitation.AVOID_UNSUPPORTED_HIP_HINGE
            },
        )
    }

    @Test
    fun `cable exercise is unavailable without cable machine`() {
        // Cable chest fly requires CABLE_MACHINE
        val exercise = ExerciseCatalog.exercises.first { it.id == ExerciseId.CABLE_CHEST_FLY }
        val profile = userProfile(
            availableEquipment = setOf(Equipment.BODYWEIGHT), // no cable
        )

        val result = useCase(exercise, profile)

        assertFalse(result.isEligible)
        assertTrue(
            result.exclusionReasons.any {
                it is ExclusionReason.MissingEquipment && it.equipment == Equipment.CABLE_MACHINE
            },
        )
    }

    @Test
    fun `beginner cannot do advanced exercise`() {
        val advanced = ExerciseCatalog.exercises.firstOrNull { it.difficulty == ExerciseDifficulty.ADVANCED }
        assertTrue(
            "Catalog should contain at least one ADVANCED exercise",
            advanced != null,
        )
        // Safe cast after check — the assertion above fails the test if null.
        val advancedExercise = requireNotNull(advanced)

        // Profile with no movement limitations and all equipment, so the only
        // possible exclusion is experience level.
        val profile = userProfile(
            experienceLevel = ExperienceLevel.BEGINNER,
            availableEquipment = ExerciseCatalog.exercises.flatMap { it.requiredEquipment }.toSet(),
            movementLimitations = emptySet(),
        )

        val result = useCase(advancedExercise, profile)

        assertFalse(result.isEligible)
        assertTrue(
            "Beginner should get AboveExperienceLevel for ADVANCED exercise ${advancedExercise.id}",
            result.exclusionReasons.any { it is ExclusionReason.AboveExperienceLevel },
        )
    }

    @Test
    fun `exercise is eligible when profile satisfies all requirements`() {
        val exercise = ExerciseCatalog.exercises.first { it.id == ExerciseId.PUSH_UP }
        val profile = userProfile(
            experienceLevel = ExperienceLevel.BEGINNER,
            availableEquipment = setOf(Equipment.BODYWEIGHT),
            movementLimitations = emptySet(),
        )

        val result = useCase(exercise, profile)

        assertTrue(result.isEligible)
        assertTrue(result.exclusionReasons.isEmpty())
    }

    @Test
    fun `multiple exclusion reasons are all reported`() {
        // Pick an exercise that needs cable + has a limitation, and create profile missing both
        val exercise = ExerciseCatalog.exercises.first { it.id == ExerciseId.DUMBBELL_ROMANIAN_DEADLIFT }
        val profile = userProfile(
            availableEquipment = setOf(Equipment.BODYWEIGHT), // missing dumbbells
            movementLimitations = exercise.conflictingLimitations, // conflict
            experienceLevel = ExperienceLevel.BEGINNER, // may be intermediate difficulty
        )

        val result = useCase(exercise, profile)

        assertFalse(result.isEligible)
        // Should have at least conflicting limitation + missing equipment
        assertTrue(result.exclusionReasons.count { it is ExclusionReason.ConflictingLimitation } >= 1)
        assertTrue(result.exclusionReasons.any { it is ExclusionReason.MissingEquipment })
    }

    // ---- Per-limitation-category tests ----

    @Test
    fun `knee category - deep knee flexion excludes exercise`() {
        assertLimitationExcludes(MovementLimitation.AVOID_DEEP_KNEE_FLEXION)
    }

    @Test
    fun `spine category - loaded spinal flexion excludes exercise`() {
        assertLimitationExcludes(MovementLimitation.AVOID_LOADED_SPINAL_FLEXION)
    }

    @Test
    fun `spine category - unsupported hip hinge excludes exercise`() {
        assertLimitationExcludes(MovementLimitation.AVOID_UNSUPPORTED_HIP_HINGE)
    }

    @Test
    fun `shoulder category - overhead pressing excludes exercise`() {
        assertLimitationExcludes(MovementLimitation.AVOID_OVERHEAD_PRESSING)
    }

    @Test
    fun `shoulder category - wide grip pressing excludes exercise`() {
        assertLimitationExcludes(MovementLimitation.AVOID_WIDE_GRIP_PRESSING)
    }

    @Test
    fun `shoulder category - shoulder abduction excludes exercise`() {
        assertLimitationExcludes(MovementLimitation.AVOID_SHOULDER_ABDUCTION)
    }

    @Test
    fun `elbow category - heavy elbow flexion excludes exercise`() {
        assertLimitationExcludes(MovementLimitation.AVOID_HEAVY_ELBOW_FLEXION)
    }

    @Test
    fun `elbow category - heavy elbow extension excludes exercise`() {
        assertLimitationExcludes(MovementLimitation.AVOID_HEAVY_ELBOW_EXTENSION)
    }

    @Test
    fun `wrist and grip category - loaded wrist extension excludes exercise`() {
        // Push-up has loaded wrist extension
        assertLimitationExcludes(MovementLimitation.AVOID_LOADED_WRIST_EXTENSION)
    }

    @Test
    fun `wrist and grip category - pronated grip excludes exercise`() {
        assertLimitationExcludes(MovementLimitation.AVOID_PRONATED_GRIP)
    }

    @Test
    fun `hip category - wide hip abduction excludes exercise`() {
        assertLimitationExcludes(MovementLimitation.AVOID_WIDE_HIP_ABDUCTION)
    }

    @Test
    fun `hip category - unilateral balance demand excludes exercise`() {
        assertLimitationExcludes(MovementLimitation.AVOID_UNILATERAL_BALANCE_DEMAND)
    }

    @Test
    fun `impact category limitations currently exclude nothing - documented`() {
        // These limitations have no applicable exercises in the strength-only catalog.
        // Pin that fact so product copy does not claim they change current results.
        val nonExcluding = listOf(
            MovementLimitation.AVOID_HIGH_IMPACT,
            MovementLimitation.AVOID_JUMPING,
            MovementLimitation.AVOID_RUNNING,
            MovementLimitation.AVOID_RAPID_DIRECTION_CHANGE,
            MovementLimitation.AVOID_SPINAL_ROTATION,
            MovementLimitation.AVOID_LOADED_WRIST_FLEXION,
        )

        for (limitation in nonExcluding) {
            val matching = ExerciseCatalog.exercises.count { limitation in it.conflictingLimitations }
            assertEquals("Expected 0 exercises to conflict with $limitation", 0, matching)
        }
    }

    private fun assertLimitationExcludes(limitation: MovementLimitation) {
        val exercise = ExerciseCatalog.exercises.firstOrNull { limitation in it.conflictingLimitations }
        assertNotNull(
            "Expected at least one exercise to conflict with $limitation — coverage loss: catalog no longer contains this limitation. " +
                "If this limitation is intentionally non-excluding, move it to the documented non-excluding list.",
            exercise,
        )
        // Safe after assertNotNull — but guard compiler with requireNotNull.
        val nonNullExercise = requireNotNull(exercise)
        val profile = userProfile(
            movementLimitations = setOf(limitation),
            availableEquipment = ExerciseCatalog.exercises.flatMap { it.requiredEquipment }.toSet() + Equipment.BODYWEIGHT,
            experienceLevel = ExperienceLevel.ADVANCED, // avoid level interference
        )
        val result = useCase(nonNullExercise, profile)
        assertFalse("Exercise ${nonNullExercise.id} should be excluded by $limitation", result.isEligible)
        assertTrue(
            result.exclusionReasons.any { it is ExclusionReason.ConflictingLimitation && it.limitation == limitation },
        )
    }
}
