package com.jgv.workoutplanner.feature.profile.edit

import app.cash.turbine.test
import com.jgv.workoutplanner.domain.model.OnboardingDraft
import com.jgv.workoutplanner.domain.model.TrainingGoal
import com.jgv.workoutplanner.navigation.AppRoute
import com.jgv.workoutplanner.testing.FakeProfileRepository
import com.jgv.workoutplanner.testing.MainDispatcherRule
import com.jgv.workoutplanner.testing.userProfile
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ProfileEditSessionViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `cancel resets draft before returning to profile`() = runTest {
        val profile = userProfile(goal = TrainingGoal.BUILD_MUSCLE)
        val repository = FakeProfileRepository(
            initialProfile = profile,
            initialDraft = OnboardingDraft.from(profile).copy(goal = TrainingGoal.BUILD_STRENGTH),
        )
        val viewModel = ProfileEditSessionViewModel(repository)

        viewModel.effects.test {
            viewModel.cancel()

            assertEquals(ProfileEditNavigationEffect.ReturnToProfile, awaitItem())
            assertEquals(profile.goal, repository.currentDraft.goal)
        }
    }

    @Test
    fun `back from root limitations resets draft and returns to profile`() = runTest {
        val profile = userProfile(goal = TrainingGoal.BUILD_MUSCLE)
        val repository = FakeProfileRepository(
            initialProfile = profile,
            initialDraft = OnboardingDraft.from(profile).copy(goal = TrainingGoal.BUILD_STRENGTH),
        )
        val viewModel = ProfileEditSessionViewModel(repository)

        viewModel.effects.test {
            viewModel.backFromMovementLimitations(AppRoute.ProfileEditOrigin.Profile)

            assertEquals(ProfileEditNavigationEffect.ReturnToProfile, awaitItem())
            assertEquals(profile.goal, repository.currentDraft.goal)
        }
    }

    @Test
    fun `intermediate back preserves draft and returns to injuries`() = runTest {
        val profile = userProfile(goal = TrainingGoal.BUILD_MUSCLE)
        val editedGoal = TrainingGoal.BUILD_STRENGTH
        val repository = FakeProfileRepository(
            initialProfile = profile,
            initialDraft = OnboardingDraft.from(profile).copy(goal = editedGoal),
        )
        val viewModel = ProfileEditSessionViewModel(repository)

        viewModel.effects.test {
            viewModel.backFromMovementLimitations(AppRoute.ProfileEditOrigin.InjuryHistory)

            assertEquals(ProfileEditNavigationEffect.NavigateBack, awaitItem())
            assertEquals(editedGoal, repository.currentDraft.goal)
        }
    }
}
