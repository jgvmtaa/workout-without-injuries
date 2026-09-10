package com.jgv.workoutplanner.data.repository

import app.cash.turbine.test
import com.jgv.workoutplanner.data.local.ProfileDataStore
import com.jgv.workoutplanner.data.local.model.PersistedState
import com.jgv.workoutplanner.domain.model.OnboardingDraft
import com.jgv.workoutplanner.domain.model.TrainingGoal
import com.jgv.workoutplanner.testing.FakeDataStore
import com.jgv.workoutplanner.testing.userProfile
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultProfileRepositoryDraftResetTest {

    @Test
    fun `resetDraftFromProfile seeds draft from profile`() = runTest {
        val dataStore = FakeDataStore(PersistedState())
        val repo = DefaultProfileRepository(ProfileDataStore(dataStore))

        val profile = userProfile(goal = TrainingGoal.BUILD_MUSCLE)
        repo.saveProfile(profile)

        // Mutate draft away from profile
        repo.updateDraft { it.copy(goal = TrainingGoal.GENERAL_FITNESS) }

        repo.onboardingDraft.test {
            assertEquals(TrainingGoal.GENERAL_FITNESS, awaitItem().goal)
        }

        repo.resetDraftFromProfile()

        repo.onboardingDraft.test {
            assertEquals(TrainingGoal.BUILD_MUSCLE, awaitItem().goal)
        }
    }

    @Test
    fun `resetDraftFromProfile after cancel prevents leak`() = runTest {
        val dataStore = FakeDataStore(PersistedState())
        val repo = DefaultProfileRepository(ProfileDataStore(dataStore))

        val profile = userProfile(goal = TrainingGoal.BUILD_MUSCLE)
        repo.saveProfile(profile)

        // Simulate edit session changing goal
        repo.updateDraft { it.copy(goal = TrainingGoal.BUILD_STRENGTH) }

        // Cancel -> reset
        repo.resetDraftFromProfile()

        repo.onboardingDraft.test {
            val draft = awaitItem()
            assertEquals(TrainingGoal.BUILD_MUSCLE, draft.goal)
        }
    }
}
