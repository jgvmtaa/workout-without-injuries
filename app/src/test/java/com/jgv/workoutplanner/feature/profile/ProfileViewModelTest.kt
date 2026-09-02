package com.jgv.workoutplanner.feature.profile

import app.cash.turbine.test
import com.jgv.workoutplanner.domain.model.TrainingGoal
import com.jgv.workoutplanner.testing.FakeProfileRepository
import com.jgv.workoutplanner.testing.MainDispatcherRule
import com.jgv.workoutplanner.testing.userProfile
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `profile summaries derived from saved profile only`() = runTest {
        val profile = userProfile(goal = TrainingGoal.BUILD_MUSCLE)
        val repo = FakeProfileRepository(initialProfile = profile)

        val vm = ProfileViewModel(repo, com.jgv.workoutplanner.data.repository.DefaultInjuryRepository(), com.jgv.workoutplanner.domain.usecase.DetermineWorkoutSplitUseCase())

        vm.uiState.test {
            var state = awaitItem()
            if (state.isLoading) state = awaitItem()
            assertTrue(state.hasProfile)
            assertFalse(state.goalRes == null)
        }
    }

    @Test
    fun `edit preferences resets draft before navigation`() = runTest {
        val profile = userProfile(goal = TrainingGoal.GENERAL_FITNESS)
        val repo = FakeProfileRepository(initialProfile = profile, initialDraft = com.jgv.workoutplanner.domain.model.OnboardingDraft(goal = TrainingGoal.BUILD_STRENGTH))

        val vm = ProfileViewModel(repo, com.jgv.workoutplanner.data.repository.DefaultInjuryRepository(), com.jgv.workoutplanner.domain.usecase.DetermineWorkoutSplitUseCase())

        vm.onEvent(ProfileEvent.EditPreferences)

        vm.effects.test {
            val eff = awaitItem()
            assertTrue(eff is ProfileEffect.NavigateToEditPreferences)
            assertEquals(TrainingGoal.GENERAL_FITNESS, repo.currentDraft.goal)
        }
    }

    @Test
    fun `edit injuries resets draft before navigation`() = runTest {
        val profile = userProfile(goal = TrainingGoal.GENERAL_FITNESS)
        val repo = FakeProfileRepository(initialProfile = profile, initialDraft = com.jgv.workoutplanner.domain.model.OnboardingDraft(goal = TrainingGoal.BUILD_MUSCLE))

        val vm = ProfileViewModel(repo, com.jgv.workoutplanner.data.repository.DefaultInjuryRepository(), com.jgv.workoutplanner.domain.usecase.DetermineWorkoutSplitUseCase())

        vm.onEvent(ProfileEvent.EditInjuries)

        vm.effects.test {
            val eff = awaitItem()
            assertTrue(eff is ProfileEffect.NavigateToEditInjuries)
            assertEquals(TrainingGoal.GENERAL_FITNESS, repo.currentDraft.goal)
        }
    }

}
