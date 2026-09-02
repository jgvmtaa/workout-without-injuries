package com.jgv.workoutplanner.feature.profile.edit

import com.jgv.workoutplanner.data.repository.DefaultInjuryRepository
import com.jgv.workoutplanner.domain.model.ExerciseId
import com.jgv.workoutplanner.domain.model.OnboardingDraft
import com.jgv.workoutplanner.domain.model.PlannedExercise
import com.jgv.workoutplanner.domain.model.TrainingGoal
import com.jgv.workoutplanner.domain.model.WorkoutDay
import com.jgv.workoutplanner.domain.model.WorkoutDayFocus
import com.jgv.workoutplanner.domain.model.WorkoutPlan
import com.jgv.workoutplanner.domain.usecase.DetermineWorkoutSplitUseCase
import com.jgv.workoutplanner.domain.usecase.SaveProfileEditsUseCase
import com.jgv.workoutplanner.feature.onboarding.review.ProfileReviewEvent
import com.jgv.workoutplanner.testing.FakeProfileRepository
import com.jgv.workoutplanner.testing.FakeWorkoutPlanRepository
import com.jgv.workoutplanner.testing.MainDispatcherRule
import com.jgv.workoutplanner.testing.userProfile
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ProfileEditReviewViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val determineSplit = DetermineWorkoutSplitUseCase()

    @Test
    fun `identical complete draft finishes without saving or invalidating`() = runTest {
        val profile = userProfile()
        val profileRepository = FakeProfileRepository(
            initialProfile = profile,
            initialDraft = OnboardingDraft.from(profile),
        )
        val workoutRepository = FakeWorkoutPlanRepository()
        val viewModel = viewModel(profileRepository, workoutRepository)

        viewModel.onEvent(ProfileReviewEvent.Confirm)

        assertTrue(viewModel.isFinished.value)
        assertTrue(profileRepository.savedProfiles.isEmpty())
        assertFalse(workoutRepository.currentStored.requiresRegeneration)
    }

    @Test
    fun `incomplete draft does not finish`() = runTest {
        val profileRepository = FakeProfileRepository(
            initialProfile = userProfile(),
            initialDraft = OnboardingDraft(),
        )
        val workoutRepository = FakeWorkoutPlanRepository()
        val viewModel = viewModel(profileRepository, workoutRepository)

        viewModel.onEvent(ProfileReviewEvent.Confirm)

        assertFalse(viewModel.isFinished.value)
        assertTrue(profileRepository.savedProfiles.isEmpty())
    }

    @Test
    fun `material edit saves profile invalidates plan and finishes`() = runTest {
        val profile = userProfile(goal = TrainingGoal.GENERAL_FITNESS)
        val profileRepository = FakeProfileRepository(
            initialProfile = profile,
            initialDraft = OnboardingDraft.from(profile).copy(goal = TrainingGoal.BUILD_MUSCLE),
        )
        val workoutRepository = FakeWorkoutPlanRepository(initialPlan = samplePlan())
        val viewModel = viewModel(profileRepository, workoutRepository)

        viewModel.onEvent(ProfileReviewEvent.Confirm)

        assertTrue(viewModel.isFinished.value)
        assertEquals(TrainingGoal.BUILD_MUSCLE, profileRepository.savedProfiles.single().goal)
        assertTrue(workoutRepository.currentStored.requiresRegeneration)
    }

    private fun viewModel(
        profileRepository: FakeProfileRepository,
        workoutRepository: FakeWorkoutPlanRepository,
    ) = ProfileEditReviewViewModel(
        profileRepository = profileRepository,
        injuryRepository = DefaultInjuryRepository(),
        determineSplit = determineSplit,
        saveProfileEditsUseCase = SaveProfileEditsUseCase(
            profileRepository,
            workoutRepository,
            determineSplit,
        ),
    )

    private fun samplePlan() = WorkoutPlan(
        id = "plan",
        name = "Plan",
        days = listOf(
            WorkoutDay(
                id = "day-1",
                name = "Day 1",
                focus = WorkoutDayFocus.FULL_BODY,
                exercises = listOf(PlannedExercise(ExerciseId.PUSH_UP, 3, 8..12, 90, 0)),
            ),
        ),
    )
}
