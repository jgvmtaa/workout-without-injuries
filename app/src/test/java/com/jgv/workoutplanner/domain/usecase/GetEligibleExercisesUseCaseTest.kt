package com.jgv.workoutplanner.domain.usecase

import com.jgv.workoutplanner.data.repository.DefaultExerciseRepository
import com.jgv.workoutplanner.domain.model.Equipment
import com.jgv.workoutplanner.domain.model.ExerciseAvailability
import com.jgv.workoutplanner.domain.model.ExerciseId
import com.jgv.workoutplanner.domain.model.ExclusionReason
import com.jgv.workoutplanner.domain.model.ExperienceLevel
import com.jgv.workoutplanner.domain.model.MovementLimitation
import com.jgv.workoutplanner.domain.model.availability
import com.jgv.workoutplanner.testing.userProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Three-category filtering tests (spec §9, §24.1).
 */
class GetEligibleExercisesUseCaseTest {

    private val exerciseRepository = DefaultExerciseRepository()
    private val evaluate = EvaluateExerciseEligibilityUseCase()
    private val useCase = GetEligibleExercisesUseCase(exerciseRepository, evaluate)

    @Test
    fun `filtering is deterministic for a fixed profile`() {
        val profile = userProfile(
            experienceLevel = ExperienceLevel.INTERMEDIATE,
            availableEquipment = setOf(
                Equipment.BODYWEIGHT,
                Equipment.DUMBBELLS,
                Equipment.BENCH,
            ),
            movementLimitations = setOf(MovementLimitation.AVOID_OVERHEAD_PRESSING),
        )

        val first = useCase(profile)
        val second = useCase(profile)

        assertEquals(first.available.map { it.exercise.id }, second.available.map { it.exercise.id })
        assertEquals(first.excluded.map { it.exercise.id }, second.excluded.map { it.exercise.id })
        assertEquals(first.unavailable.map { it.exercise.id }, second.unavailable.map { it.exercise.id })
    }

    @Test
    fun `total count equals catalog size`() {
        val profile = userProfile(
            experienceLevel = ExperienceLevel.ADVANCED,
            availableEquipment = exerciseRepository.getAllExercises().flatMap { it.requiredEquipment }.toSet(),
        )
        val result = useCase(profile)

        assertEquals(exerciseRepository.getAllExercises().size, result.totalCount)
    }

    @Test
    fun `excluded exercises have conflicting limitation reason`() {
        val profile = userProfile(
            movementLimitations = setOf(MovementLimitation.AVOID_WIDE_GRIP_PRESSING),
            availableEquipment = exerciseRepository.getAllExercises().flatMap { it.requiredEquipment }.toSet(),
            experienceLevel = ExperienceLevel.ADVANCED,
        )

        val result = useCase(profile)

        assertTrue(result.excluded.isNotEmpty())
        result.excluded.forEach { eligibility ->
            assertTrue(
                eligibility.exclusionReasons.any { it is ExclusionReason.ConflictingLimitation },
            )
            assertEquals(ExerciseAvailability.EXCLUDED, eligibility.availability())
        }
    }

    @Test
    fun `unavailable includes missing equipment`() {
        val profile = userProfile(
            availableEquipment = setOf(Equipment.BODYWEIGHT),
            experienceLevel = ExperienceLevel.ADVANCED,
        )

        val result = useCase(profile)

        // Cable exercise should be in unavailable, not excluded
        val cableExercise = result.all.firstOrNull { it.exercise.id == ExerciseId.CABLE_CHEST_FLY }
        assertTrue("CABLE_CHEST_FLY should be categorized", cableExercise != null)
        if (cableExercise != null) {
            // No limitation conflict in this profile, so missing equipment -> UNAVAILABLE
            assertEquals(ExerciseAvailability.UNAVAILABLE, cableExercise.availability())
        }
    }

    @Test
    fun `priority - limitation conflict outranks missing equipment as EXCLUDED`() {
        // Profile missing equipment AND having conflicting limitation for same exercise
        // Should be EXCLUDED, not UNAVAILABLE, because safety signal wins.
        val target = exerciseRepository.getAllExercises().first { exercise ->
            exercise.requiredEquipment.contains(Equipment.CABLE_MACHINE) &&
                exercise.conflictingLimitations.isNotEmpty()
        }

        val profile = userProfile(
            availableEquipment = setOf(Equipment.BODYWEIGHT), // missing cable
            movementLimitations = target.conflictingLimitations, // conflict
            experienceLevel = ExperienceLevel.ADVANCED,
        )

        val result = useCase(profile)
        val eligibility = result.all.first { it.exercise.id == target.id }

        assertEquals(ExerciseAvailability.EXCLUDED, eligibility.availability())
        assertTrue(eligibility.exclusionReasons.any { it is ExclusionReason.ConflictingLimitation })
        assertTrue(eligibility.exclusionReasons.any { it is ExclusionReason.MissingEquipment })
    }

    @Test
    fun `available contains only eligible exercises`() {
        val profile = userProfile(
            availableEquipment = setOf(Equipment.BODYWEIGHT),
            experienceLevel = ExperienceLevel.BEGINNER,
        )

        val result = useCase(profile)

        result.available.forEach { assertTrue(it.isEligible) }
        result.excluded.forEach { assertTrue(!it.isEligible) }
        result.unavailable.forEach { assertTrue(!it.isEligible) }
    }

    @Test
    fun `result buckets are sorted by id name for determinism`() {
        val profile = userProfile(
            experienceLevel = ExperienceLevel.ADVANCED,
            availableEquipment = exerciseRepository.getAllExercises().flatMap { it.requiredEquipment }.toSet(),
        )
        val result = useCase(profile)

        fun List<com.jgv.workoutplanner.domain.model.ExerciseEligibility>.isSorted(): Boolean {
            val names = map { it.exercise.id.name }
            return names == names.sorted()
        }

        assertTrue(result.available.isSorted())
        assertTrue(result.excluded.isSorted())
        assertTrue(result.unavailable.isSorted())
    }
}
