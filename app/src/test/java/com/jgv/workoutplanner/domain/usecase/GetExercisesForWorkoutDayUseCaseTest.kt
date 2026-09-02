package com.jgv.workoutplanner.domain.usecase

import com.jgv.workoutplanner.data.repository.DefaultExerciseRepository
import com.jgv.workoutplanner.domain.model.Equipment
import com.jgv.workoutplanner.domain.model.ExerciseId
import com.jgv.workoutplanner.domain.model.ExperienceLevel
import com.jgv.workoutplanner.domain.model.MovementLimitation
import com.jgv.workoutplanner.domain.model.WorkoutDayFocus
import com.jgv.workoutplanner.domain.model.WorkoutPlanTemplate
import com.jgv.workoutplanner.testing.userProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GetExercisesForWorkoutDayUseCaseTest {

    private val exerciseRepo = DefaultExerciseRepository()
    private val evaluate = EvaluateExerciseEligibilityUseCase()
    private val eligible = GetEligibleExercisesUseCase(exerciseRepo, evaluate)
    private val useCase = GetExercisesForWorkoutDayUseCase(eligible)

    private val generousProfile = userProfile(
        experienceLevel = ExperienceLevel.ADVANCED,
        availableEquipment = exerciseRepo.getAllExercises().flatMap { it.requiredEquipment }.toSet() + Equipment.BODYWEIGHT,
    )

    @Test
    fun `filters by profile eligible`() {
        val profile = userProfile(
            experienceLevel = ExperienceLevel.BEGINNER,
            availableEquipment = setOf(Equipment.BODYWEIGHT),
        )
        val result = useCase(profile, WorkoutDayFocus.FULL_BODY, emptySet())
        result.forEach { def ->
            assertEquals(com.jgv.workoutplanner.domain.model.ExerciseDifficulty.BEGINNER, def.difficulty)
            assertTrue(def.requiredEquipment.all { it == Equipment.BODYWEIGHT })
        }
    }

    @Test
    fun `keeps only patterns allowed for focus`() {
        val result = useCase(generousProfile, WorkoutDayFocus.PUSH, emptySet())
        val allowed = WorkoutPlanTemplate.slotsFor(WorkoutDayFocus.PUSH).flatMap { it.allowedPatterns }.toSet()
        result.forEach { def ->
            assertTrue("${def.id} pattern ${def.movementPattern} not allowed for PUSH", def.movementPattern in allowed)
        }
    }

    @Test
    fun `excludes exercises already in day`() {
        val excluded = setOf(ExerciseId.MACHINE_CHEST_PRESS, ExerciseId.PUSH_UP)
        val result = useCase(generousProfile, WorkoutDayFocus.FULL_BODY, excluded)
        assertFalse(result.any { it.id in excluded })
    }

    @Test
    fun `deterministic ordering by ExerciseId name`() {
        val result = useCase(generousProfile, WorkoutDayFocus.FULL_BODY, emptySet())
        val names = result.map { it.id.name }
        assertEquals(names.sorted(), names)
    }

    @Test
    fun `different focuses produce different allowed patterns`() {
        val upper = useCase(generousProfile, WorkoutDayFocus.UPPER_BODY, emptySet()).map { it.movementPattern }.toSet()
        val lower = useCase(generousProfile, WorkoutDayFocus.LOWER_BODY, emptySet()).map { it.movementPattern }.toSet()
        // Upper and lower should differ
        assertFalse(upper == lower)
    }
}
