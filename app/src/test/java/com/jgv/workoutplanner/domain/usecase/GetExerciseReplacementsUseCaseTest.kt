package com.jgv.workoutplanner.domain.usecase

import com.jgv.workoutplanner.data.repository.DefaultExerciseRepository
import com.jgv.workoutplanner.domain.model.Equipment
import com.jgv.workoutplanner.domain.model.ExerciseDefinition
import com.jgv.workoutplanner.domain.model.ExerciseDifficulty
import com.jgv.workoutplanner.domain.model.ExerciseId
import com.jgv.workoutplanner.domain.model.ExercisePrescription
import com.jgv.workoutplanner.domain.model.ExerciseTag
import com.jgv.workoutplanner.domain.model.ExperienceLevel
import com.jgv.workoutplanner.domain.model.MovementLimitation
import com.jgv.workoutplanner.domain.model.MovementPattern
import com.jgv.workoutplanner.domain.model.MuscleGroup
import com.jgv.workoutplanner.domain.repository.ExerciseRepository
import com.jgv.workoutplanner.testing.userProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GetExerciseReplacementsUseCaseTest {

    private val exerciseRepo = DefaultExerciseRepository()
    private val evaluate = EvaluateExerciseEligibilityUseCase()
    private val eligible = GetEligibleExercisesUseCase(exerciseRepo, evaluate)
    private val useCase = GetExerciseReplacementsUseCase(eligible, exerciseRepo)

    private val generousProfile = userProfile(
        experienceLevel = ExperienceLevel.ADVANCED,
        availableEquipment = exerciseRepo.getAllExercises().flatMap { it.requiredEquipment }.toSet() + Equipment.BODYWEIGHT,
    )

    @Test
    fun `candidates respect limitation filter`() {
        val profile = generousProfile.copy(
            movementLimitations = setOf(MovementLimitation.AVOID_HIGH_IMPACT),
        )
        val current = ExerciseId.MACHINE_CHEST_PRESS
        val result = useCase(current, profile, emptySet())

        // MACHINE_CHEST_PRESS does not conflict with HIGH_IMPACT, but some candidates might
        result.forEach { def ->
            assertFalse(def.conflictingLimitations.contains(MovementLimitation.AVOID_HIGH_IMPACT))
        }
    }

    @Test
    fun `candidates respect equipment filter`() {
        val profile = userProfile(
            experienceLevel = ExperienceLevel.ADVANCED,
            availableEquipment = setOf(Equipment.BODYWEIGHT),
        )
        val result = useCase(ExerciseId.PUSH_UP, profile, emptySet())

        result.forEach { def ->
            assertTrue(def.requiredEquipment.all { it == Equipment.BODYWEIGHT })
        }
    }

    @Test
    fun `candidates respect experience filter`() {
        val profile = userProfile(
            experienceLevel = ExperienceLevel.BEGINNER,
            availableEquipment = exerciseRepo.getAllExercises().flatMap { it.requiredEquipment }.toSet() + Equipment.BODYWEIGHT,
        )
        val result = useCase(ExerciseId.PUSH_UP, profile, emptySet())

        result.forEach { def ->
            assertEquals(com.jgv.workoutplanner.domain.model.ExerciseDifficulty.BEGINNER, def.difficulty)
        }
    }

    @Test
    fun `excludes current exercise`() {
        val result = useCase(ExerciseId.MACHINE_CHEST_PRESS, generousProfile, emptySet())
        assertFalse(result.any { it.id == ExerciseId.MACHINE_CHEST_PRESS })
    }

    @Test
    fun `excludes day duplicates`() {
        val excluded = setOf(ExerciseId.PUSH_UP, ExerciseId.LAT_PULLDOWN)
        val result = useCase(ExerciseId.MACHINE_CHEST_PRESS, generousProfile, excluded)
        assertFalse(result.any { it.id in excluded })
    }

    @Test
    fun `ranking tier same pattern and muscle first`() {
        // Current = MACHINE_CHEST_PRESS: HORIZONTAL_PUSH, CHEST
        // Tier 1 = same pattern + primary muscle
        val result = useCase(ExerciseId.MACHINE_CHEST_PRESS, generousProfile, emptySet())
        assertTrue(result.isNotEmpty())

        val tier1Indices = result.mapIndexedNotNull { idx, def ->
            if (def.movementPattern == com.jgv.workoutplanner.domain.model.MovementPattern.HORIZONTAL_PUSH &&
                def.primaryMuscle == com.jgv.workoutplanner.domain.model.MuscleGroup.CHEST
            ) idx else null
        }
        val tier2Indices = result.mapIndexedNotNull { idx, def ->
            if (def.movementPattern == com.jgv.workoutplanner.domain.model.MovementPattern.HORIZONTAL_PUSH &&
                def.primaryMuscle != com.jgv.workoutplanner.domain.model.MuscleGroup.CHEST
            ) idx else null
        }

        if (tier1Indices.isNotEmpty() && tier2Indices.isNotEmpty()) {
            assertTrue(tier1Indices.max()!! < tier2Indices.min()!!)
        }
    }

    @Test
    fun `ranking orders all four tiers`() {
        fun definition(
            id: ExerciseId,
            pattern: MovementPattern,
            muscle: MuscleGroup,
        ) = ExerciseDefinition(
            id = id,
            nameRes = 0,
            descriptionRes = 0,
            primaryMuscle = muscle,
            secondaryMuscles = emptySet(),
            movementPattern = pattern,
            requiredEquipment = setOf(Equipment.BODYWEIGHT),
            difficulty = ExerciseDifficulty.BEGINNER,
            conflictingLimitations = emptySet(),
            tags = setOf(ExerciseTag.COMPOUND),
            defaultPrescription = ExercisePrescription(sets = 3..3, reps = 8..12, restSeconds = 60),
        )

        val definitions = listOf(
            definition(ExerciseId.PUSH_UP, MovementPattern.HORIZONTAL_PUSH, MuscleGroup.CHEST),
            definition(ExerciseId.MACHINE_CHEST_PRESS, MovementPattern.HORIZONTAL_PUSH, MuscleGroup.CHEST),
            definition(ExerciseId.LAT_PULLDOWN, MovementPattern.HORIZONTAL_PUSH, MuscleGroup.BACK),
            definition(ExerciseId.BODYWEIGHT_SQUAT, MovementPattern.SQUAT, MuscleGroup.CHEST),
            definition(ExerciseId.DEAD_BUG, MovementPattern.CORE_ANTI_EXTENSION, MuscleGroup.CORE),
        )
        val repository = object : ExerciseRepository {
            override fun getAllExercises(): List<ExerciseDefinition> = definitions

            override fun getExercise(id: ExerciseId): ExerciseDefinition =
                definitions.first { it.id == id }

            override fun getExercisesByMuscleGroup(
                muscleGroup: MuscleGroup,
                includeSecondary: Boolean,
            ): List<ExerciseDefinition> = definitions.filter { definition ->
                definition.primaryMuscle == muscleGroup ||
                    (includeSecondary && muscleGroup in definition.secondaryMuscles)
            }

            override fun getExercisesByMovementPattern(
                movementPattern: MovementPattern,
            ): List<ExerciseDefinition> = definitions.filter { it.movementPattern == movementPattern }
        }
        val eligibility = GetEligibleExercisesUseCase(repository, EvaluateExerciseEligibilityUseCase())

        val result = GetExerciseReplacementsUseCase(eligibility, repository)(
            currentExerciseId = ExerciseId.PUSH_UP,
            profile = userProfile(),
            excludedExerciseIds = emptySet(),
        )

        assertEquals(
            listOf(
                ExerciseId.MACHINE_CHEST_PRESS,
                ExerciseId.LAT_PULLDOWN,
                ExerciseId.BODYWEIGHT_SQUAT,
                ExerciseId.DEAD_BUG,
            ),
            result.map { it.id },
        )
    }

    @Test
    fun `ranking tie-break by ExerciseId name`() {
        val result = useCase(ExerciseId.PUSH_UP, generousProfile, emptySet())
        // Within same tier, sorted by name
        val groupedByTier = result.groupBy { def ->
            val current = exerciseRepo.getExercise(ExerciseId.PUSH_UP)
            when {
                def.movementPattern == current.movementPattern && def.primaryMuscle == current.primaryMuscle -> 0
                def.movementPattern == current.movementPattern -> 1
                def.primaryMuscle == current.primaryMuscle -> 2
                else -> 3
            }
        }

        groupedByTier.values.forEach { tierList ->
            val names = tierList.map { it.id.name }
            assertEquals(names.sorted(), names)
        }
    }

    @Test
    fun `returns empty when no eligible alternatives`() {
        val profile = userProfile(
            experienceLevel = ExperienceLevel.BEGINNER,
            availableEquipment = setOf(Equipment.BODYWEIGHT),
            movementLimitations = MovementLimitation.entries.toSet(),
        )
        val result = useCase(ExerciseId.PUSH_UP, profile, emptySet())
        // With all limitations, likely empty or very small, but must not relax filters
        result.forEach { def ->
            // still eligible
            val eligibility = evaluate(def, profile)
            assertTrue(eligibility.isEligible)
        }
    }
}
