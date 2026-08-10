package com.jgv.workoutplanner.data.repository

import app.cash.turbine.test
import com.jgv.workoutplanner.data.local.ProfileDataStore
import com.jgv.workoutplanner.data.local.model.PersistedState
import com.jgv.workoutplanner.domain.model.Equipment
import com.jgv.workoutplanner.domain.model.InjuryId
import com.jgv.workoutplanner.domain.model.MovementLimitation
import com.jgv.workoutplanner.domain.model.OnboardingDraft
import com.jgv.workoutplanner.domain.model.TrainingGoal
import com.jgv.workoutplanner.testing.FakeDataStore
import com.jgv.workoutplanner.testing.injury
import com.jgv.workoutplanner.testing.userProfile
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * The repository's own behaviour, over an in-memory store (README §21, §27 Phase 3).
 *
 * What is worth testing here is not "does DataStore work" but the decisions this class
 * makes: what a save does to the draft, what clearing removes, and that a write to one
 * half of the state does not wake collectors of the other.
 */
class DefaultProfileRepositoryTest {

    private val dataStore = FakeDataStore(PersistedState())
    private val repository = DefaultProfileRepository(ProfileDataStore(dataStore))

    @Test
    fun `there is no profile before onboarding`() = runTest {
        repository.profile.test {
            assertNull(awaitItem())
        }
    }

    @Test
    fun `an empty draft is emitted before anything is answered`() = runTest {
        repository.onboardingDraft.test {
            assertEquals(OnboardingDraft(), awaitItem())
        }
    }

    @Test
    fun `a draft update is visible on the next emission`() = runTest {
        repository.updateDraft { it.copy(goal = TrainingGoal.BUILD_MUSCLE) }

        repository.onboardingDraft.test {
            assertEquals(TrainingGoal.BUILD_MUSCLE, awaitItem().goal)
        }
    }

    @Test
    fun `draft updates accumulate rather than replace`() = runTest {
        repository.updateDraft { it.copy(goal = TrainingGoal.BUILD_STRENGTH) }
        repository.updateDraft { it.copy(daysPerWeek = 4) }
        repository.updateDraft {
            it.copy(confirmedLimitations = setOf(MovementLimitation.AVOID_JUMPING))
        }

        repository.onboardingDraft.test {
            val draft = awaitItem()
            assertEquals(TrainingGoal.BUILD_STRENGTH, draft.goal)
            assertEquals(4, draft.daysPerWeek)
            assertEquals(setOf(MovementLimitation.AVOID_JUMPING), draft.confirmedLimitations)
        }
    }

    @Test
    fun `a saved profile is readable`() = runTest {
        val profile = userProfile(
            goal = TrainingGoal.IMPROVE_ENDURANCE,
            availableEquipment = setOf(Equipment.BODYWEIGHT, Equipment.RESISTANCE_BAND),
            selectedInjuries = setOf(injury(InjuryId.ANKLE_SPRAIN)),
            movementLimitations = setOf(MovementLimitation.AVOID_HIGH_IMPACT),
        )

        repository.saveProfile(profile)

        repository.profile.test {
            assertEquals(profile, awaitItem())
        }
    }

    /**
     * The profile screen reuses the onboarding screens to edit a saved profile, and those
     * screens read the draft. Clearing it on save would show a returning user empty
     * forms (README §13).
     */
    @Test
    fun `saving a profile re-seeds the draft from it`() = runTest {
        val profile = userProfile(
            goal = TrainingGoal.BUILD_MUSCLE,
            movementLimitations = setOf(MovementLimitation.AVOID_KNEELING),
        )

        repository.saveProfile(profile)

        repository.onboardingDraft.test {
            val draft = awaitItem()
            assertEquals(TrainingGoal.BUILD_MUSCLE, draft.goal)
            assertEquals(setOf(MovementLimitation.AVOID_KNEELING), draft.confirmedLimitations)
        }
    }

    @Test
    fun `clearing removes both the profile and the draft`() = runTest {
        repository.saveProfile(userProfile(goal = TrainingGoal.BUILD_MUSCLE))

        repository.clearProfile()

        repository.profile.test { assertNull(awaitItem()) }
        repository.onboardingDraft.test { assertEquals(OnboardingDraft(), awaitItem()) }
    }

    /**
     * A draft write rewrites the whole stored record. Without `distinctUntilChanged`,
     * ticking a limitation would re-emit an identical profile to every collector — which
     * on the home screen means recomposing a plan summary that has not changed.
     */
    @Test
    fun `a draft write does not re-emit an unchanged profile`() = runTest {
        val profile = userProfile()
        repository.saveProfile(profile)

        repository.profile.test {
            assertEquals(profile, awaitItem())

            repository.updateDraft { it.copy(sessionDurationMinutes = 30) }

            expectNoEvents()
        }
    }

    @Test
    fun `a profile write does not re-emit an unchanged draft`() = runTest {
        val profile = userProfile()
        repository.saveProfile(profile)

        repository.onboardingDraft.test {
            assertEquals(OnboardingDraft.from(profile), awaitItem())

            // Saving the same profile again produces the same draft.
            repository.saveProfile(profile)

            expectNoEvents()
        }
    }
}
