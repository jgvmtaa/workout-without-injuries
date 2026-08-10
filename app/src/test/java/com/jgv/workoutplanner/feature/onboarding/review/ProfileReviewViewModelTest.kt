package com.jgv.workoutplanner.feature.onboarding.review

import app.cash.turbine.test
import com.jgv.workoutplanner.R
import com.jgv.workoutplanner.data.repository.DefaultInjuryRepository
import com.jgv.workoutplanner.domain.model.Equipment
import com.jgv.workoutplanner.domain.model.InjuryId
import com.jgv.workoutplanner.domain.model.MovementLimitation
import com.jgv.workoutplanner.domain.model.OnboardingDraft
import com.jgv.workoutplanner.domain.model.TrainingGoal
import com.jgv.workoutplanner.domain.model.WorkoutSplit
import com.jgv.workoutplanner.domain.usecase.DetermineWorkoutSplitUseCase
import com.jgv.workoutplanner.testing.FakeProfileRepository
import com.jgv.workoutplanner.testing.MainDispatcherRule
import com.jgv.workoutplanner.testing.completeDraft
import com.jgv.workoutplanner.testing.injury
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Summary, completion gating and the profile write (README §24.5, §27 Phase 3). */
class ProfileReviewViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(repository: FakeProfileRepository) = ProfileReviewViewModel(
        profileRepository = repository,
        determineWorkoutSplit = DetermineWorkoutSplitUseCase(),
        injuryRepository = DefaultInjuryRepository(),
    )

    @Test
    fun `a complete draft can be confirmed`() = runTest {
        viewModel(FakeProfileRepository(initialDraft = completeDraft())).uiState.test {
            val state = awaitItem()

            assertTrue(state.isComplete)
            assertTrue(state.canContinue)
        }
    }

    @Test
    fun `an incomplete draft cannot be confirmed`() = runTest {
        val repository = FakeProfileRepository(initialDraft = OnboardingDraft())

        viewModel(repository).uiState.test {
            assertFalse(awaitItem().canContinue)
        }
    }

    @Test
    fun `the summary reflects the draft`() = runTest {
        val repository = FakeProfileRepository(
            initialDraft = completeDraft(
                goal = TrainingGoal.BUILD_STRENGTH,
                daysPerWeek = 4,
                sessionDurationMinutes = 75,
                availableEquipment = setOf(Equipment.BODYWEIGHT, Equipment.BARBELL),
                selectedInjuries = setOf(injury(InjuryId.KNEE_ACL)),
                confirmedLimitations = setOf(MovementLimitation.AVOID_JUMPING),
            ),
        )

        viewModel(repository).uiState.test {
            val state = awaitItem()

            assertEquals(R.string.goal_build_strength, state.goalRes)
            assertEquals(4, state.daysPerWeek)
            assertEquals(75, state.sessionDurationMinutes)
            assertEquals(R.string.split_upper_lower, state.splitRes)
            assertEquals(
                listOf(R.string.equipment_bodyweight, R.string.equipment_barbell),
                state.equipment,
            )
            assertEquals(listOf(R.string.injury_knee_acl), state.injuries.map { it.nameRes })
            assertEquals(listOf(R.string.limitation_avoid_jumping), state.limitations)
        }
    }

    /**
     * The review screen is the last place the app can imply an injury did something on
     * its own, so it must show only what the user confirmed (README §2).
     */
    @Test
    fun `an injury with no confirmed limitation shows an empty limitations list`() = runTest {
        val repository = FakeProfileRepository(
            initialDraft = completeDraft(selectedInjuries = setOf(injury(InjuryId.KNEE_SURGERY))),
        )

        viewModel(repository).uiState.test {
            val state = awaitItem()

            assertEquals(1, state.injuries.size)
            assertEquals(emptyList<Int>(), state.limitations)
        }
    }

    @Test
    fun `confirming saves the profile`() = runTest {
        val repository = FakeProfileRepository(
            initialDraft = completeDraft(
                goal = TrainingGoal.BUILD_MUSCLE,
                daysPerWeek = 3,
                confirmedLimitations = setOf(MovementLimitation.AVOID_KNEELING),
            ),
        )
        val viewModel = viewModel(repository)

        viewModel.onEvent(ProfileReviewEvent.Confirm)

        val saved = repository.savedProfiles.single()
        assertEquals(TrainingGoal.BUILD_MUSCLE, saved.goal)
        assertEquals(WorkoutSplit.PUSH_PULL_LEGS, saved.preferredSplit)
        assertEquals(setOf(MovementLimitation.AVOID_KNEELING), saved.movementLimitations)
        assertTrue(saved.hasAcceptedSafetyNotice)
    }

    /** The route navigates on this, so it must not flip before the write lands. */
    @Test
    fun `the finished signal follows the save`() = runTest {
        val repository = FakeProfileRepository(initialDraft = completeDraft())
        val viewModel = viewModel(repository)

        viewModel.isFinished.test {
            assertFalse(awaitItem())

            viewModel.onEvent(ProfileReviewEvent.Confirm)

            assertTrue(awaitItem())
        }
        assertEquals(1, repository.savedProfiles.size)
    }

    /**
     * Guards a race, not an expected path: the finish button is disabled while the draft
     * is incomplete. Doing nothing beats saving a profile with a guessed goal.
     */
    @Test
    fun `confirming an incomplete draft saves nothing`() = runTest {
        val repository = FakeProfileRepository(initialDraft = OnboardingDraft())
        val viewModel = viewModel(repository)

        viewModel.onEvent(ProfileReviewEvent.Confirm)

        assertTrue(repository.savedProfiles.isEmpty())
        assertFalse(viewModel.isFinished.value)
    }

    @Test
    fun `back saves nothing`() = runTest {
        val repository = FakeProfileRepository(initialDraft = completeDraft())

        viewModel(repository).onEvent(ProfileReviewEvent.Back)

        assertTrue(repository.savedProfiles.isEmpty())
    }
}
