package com.jgv.workoutplanner.domain.usecase

import com.jgv.workoutplanner.domain.model.Equipment
import com.jgv.workoutplanner.domain.model.ExperienceLevel
import com.jgv.workoutplanner.domain.model.MovementLimitation
import com.jgv.workoutplanner.domain.model.PlannedExercise
import com.jgv.workoutplanner.domain.model.WorkoutDay
import com.jgv.workoutplanner.domain.model.WorkoutDayFocus
import com.jgv.workoutplanner.domain.model.WorkoutPlan
import com.jgv.workoutplanner.domain.model.ExerciseId
import com.jgv.workoutplanner.domain.model.TrainingGoal
import com.jgv.workoutplanner.testing.FakeProfileRepository
import com.jgv.workoutplanner.testing.FakeWorkoutPlanRepository
import com.jgv.workoutplanner.testing.completeDraft
import com.jgv.workoutplanner.testing.userProfile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SaveProfileEditsUseCaseTest {

    private val determineSplit = DetermineWorkoutSplitUseCase()

    private fun samplePlan(): WorkoutPlan {
        val day = WorkoutDay(
            id = "day-1",
            name = "Full",
            focus = WorkoutDayFocus.FULL_BODY,
            exercises = listOf(PlannedExercise(ExerciseId.PUSH_UP, 3, 8..12, 90, 0)),
        )
        return WorkoutPlan(id = "p", name = "p", days = listOf(day))
    }

    @Test
    fun `identical save is Unchanged and does not invalidate`() = runTest {
        val profile = userProfile()
        val profileRepo = FakeProfileRepository(initialProfile = profile, initialDraft = com.jgv.workoutplanner.domain.model.OnboardingDraft.from(profile))
        val workoutRepo = FakeWorkoutPlanRepository(initialPlan = samplePlan())
        val useCase = SaveProfileEditsUseCase(profileRepo, workoutRepo, determineSplit)

        val draft = com.jgv.workoutplanner.domain.model.OnboardingDraft.from(profile)
        val result = useCase(draft)

        assertEquals(SaveProfileEditsUseCase.Result.Unchanged, result)
        assertFalse(workoutRepo.currentStored.requiresRegeneration)
    }

    @Test
    fun `material change marks requiresRegeneration before save`() = runTest {
        val profile = userProfile(goal = TrainingGoal.GENERAL_FITNESS)
        val operations = mutableListOf<String>()
        val profileRepo = FakeProfileRepository(
            initialProfile = profile,
            initialDraft = com.jgv.workoutplanner.domain.model.OnboardingDraft.from(profile),
            operationLog = operations,
        )
        val workoutRepo = FakeWorkoutPlanRepository(
            initialPlan = samplePlan(),
            operationLog = operations,
        )
        val useCase = SaveProfileEditsUseCase(profileRepo, workoutRepo, determineSplit)

        val editedDraft = com.jgv.workoutplanner.domain.model.OnboardingDraft.from(profile).copy(goal = TrainingGoal.BUILD_MUSCLE)
        val result = useCase(editedDraft)

        assertEquals(SaveProfileEditsUseCase.Result.Updated, result)
        assertTrue(workoutRepo.currentStored.requiresRegeneration)
        assertEquals(listOf("markRequiresRegeneration", "saveProfile"), operations)
    }

    @Test
    fun `hasAcceptedSafetyNotice change alone saves without invalidating`() = runTest {
        val profile = userProfile(hasAcceptedSafetyNotice = false)
        val profileRepo = FakeProfileRepository(initialProfile = profile, initialDraft = com.jgv.workoutplanner.domain.model.OnboardingDraft.from(profile))
        val workoutRepo = FakeWorkoutPlanRepository(initialPlan = samplePlan())
        val useCase = SaveProfileEditsUseCase(profileRepo, workoutRepo, determineSplit)

        val editedDraft = com.jgv.workoutplanner.domain.model.OnboardingDraft.from(profile).copy(hasAcceptedSafetyNotice = true)
        val result = useCase(editedDraft)

        assertEquals(SaveProfileEditsUseCase.Result.Updated, result)
        assertFalse(workoutRepo.currentStored.requiresRegeneration)
        assertEquals(true, profileRepo.profile.first()?.hasAcceptedSafetyNotice)
    }

    @Test
    fun `no plan exists does not create outdated state`() = runTest {
        val profile = userProfile()
        val profileRepo = FakeProfileRepository(initialProfile = profile, initialDraft = com.jgv.workoutplanner.domain.model.OnboardingDraft.from(profile))
        val workoutRepo = FakeWorkoutPlanRepository(initialPlan = null)
        val useCase = SaveProfileEditsUseCase(profileRepo, workoutRepo, determineSplit)

        val editedDraft = com.jgv.workoutplanner.domain.model.OnboardingDraft.from(profile).copy(goal = TrainingGoal.BUILD_STRENGTH)
        val result = useCase(editedDraft)

        assertEquals(SaveProfileEditsUseCase.Result.Updated, result)
        assertFalse(workoutRepo.currentStored.requiresRegeneration)
        // markRequiresRegeneration returned false because no plan
    }

    @Test
    fun `change with existing plan persists invalidation`() = runTest {
        val profile = userProfile(experienceLevel = ExperienceLevel.BEGINNER)
        val profileRepo = FakeProfileRepository(initialProfile = profile, initialDraft = com.jgv.workoutplanner.domain.model.OnboardingDraft.from(profile))
        val workoutRepo = FakeWorkoutPlanRepository(initialPlan = samplePlan())
        val useCase = SaveProfileEditsUseCase(profileRepo, workoutRepo, determineSplit)

        val editedDraft = com.jgv.workoutplanner.domain.model.OnboardingDraft.from(profile).copy(experienceLevel = ExperienceLevel.ADVANCED)
        useCase(editedDraft)

        assertTrue(workoutRepo.currentStored.requiresRegeneration)
        assertEquals(ExperienceLevel.ADVANCED, profileRepo.profile.first()?.experienceLevel)
    }
}
