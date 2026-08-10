package com.jgv.workoutplanner.feature.onboarding.preferences

import app.cash.turbine.test
import com.jgv.workoutplanner.domain.model.Equipment
import com.jgv.workoutplanner.domain.model.ExperienceLevel
import com.jgv.workoutplanner.domain.model.TrainingGoal
import com.jgv.workoutplanner.domain.model.WorkoutSplit
import com.jgv.workoutplanner.domain.usecase.DetermineWorkoutSplitUseCase
import com.jgv.workoutplanner.testing.FakeProfileRepository
import com.jgv.workoutplanner.testing.MainDispatcherRule
import com.jgv.workoutplanner.testing.completeDraft
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Selection, continue-state and persistence for the preferences step (README §24.5). */
class PreferencesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeProfileRepository()
    private val viewModel by lazy { PreferencesViewModel(repository, DetermineWorkoutSplitUseCase()) }

    @Test
    fun `nothing is selected to begin with`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()

            assertNull(state.goal)
            assertNull(state.experienceLevel)
            assertNull(state.daysPerWeek)
            assertNull(state.sessionDurationMinutes)
            assertFalse(state.canContinue)
        }
    }

    @Test
    fun `continue unlocks once every question is answered`() = runTest {
        viewModel.uiState.test {
            assertFalse(awaitItem().canContinue)

            viewModel.onEvent(PreferencesEvent.SelectGoal(TrainingGoal.BUILD_MUSCLE))
            assertFalse(awaitItem().canContinue)

            viewModel.onEvent(PreferencesEvent.SelectExperience(ExperienceLevel.BEGINNER))
            assertFalse(awaitItem().canContinue)

            viewModel.onEvent(PreferencesEvent.SelectDaysPerWeek(3))
            assertFalse(awaitItem().canContinue)

            viewModel.onEvent(PreferencesEvent.SelectSessionDuration(45))
            assertTrue(awaitItem().canContinue)
        }
    }

    /** Equipment is optional: selecting none means bodyweight only (README §25). */
    @Test
    fun `no equipment selected does not block continuing`() = runTest {
        viewModel.onEvent(PreferencesEvent.SelectGoal(TrainingGoal.GENERAL_FITNESS))
        viewModel.onEvent(PreferencesEvent.SelectExperience(ExperienceLevel.BEGINNER))
        viewModel.onEvent(PreferencesEvent.SelectDaysPerWeek(2))
        viewModel.onEvent(PreferencesEvent.SelectSessionDuration(30))

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.selectedEquipment.isEmpty())
            assertTrue(state.canContinue)
        }
    }

    @Test
    fun `each selection is persisted as it is made`() = runTest {
        viewModel.onEvent(PreferencesEvent.SelectGoal(TrainingGoal.BUILD_STRENGTH))
        viewModel.onEvent(PreferencesEvent.SelectExperience(ExperienceLevel.ADVANCED))
        viewModel.onEvent(PreferencesEvent.SelectDaysPerWeek(4))
        viewModel.onEvent(PreferencesEvent.SelectSessionDuration(75))

        val draft = repository.currentDraft
        assertEquals(TrainingGoal.BUILD_STRENGTH, draft.goal)
        assertEquals(ExperienceLevel.ADVANCED, draft.experienceLevel)
        assertEquals(4, draft.daysPerWeek)
        assertEquals(75, draft.sessionDurationMinutes)
    }

    @Test
    fun `equipment toggles on and off`() = runTest {
        viewModel.onEvent(PreferencesEvent.ToggleEquipment(Equipment.DUMBBELLS, true))
        assertTrue(Equipment.DUMBBELLS in repository.currentDraft.availableEquipment)

        viewModel.onEvent(PreferencesEvent.ToggleEquipment(Equipment.DUMBBELLS, false))
        assertFalse(Equipment.DUMBBELLS in repository.currentDraft.availableEquipment)
    }

    /**
     * README §25 offers "treat bodyweight as always available" or "require at least one
     * selection"; `Equipment`'s KDoc picks the first. Enforced in the ViewModel rather
     * than by hiding the control, because the draft is what plan generation reads.
     */
    @Test
    fun `bodyweight survives every equipment change`() = runTest {
        viewModel.onEvent(PreferencesEvent.ToggleEquipment(Equipment.BARBELL, true))
        assertTrue(Equipment.BODYWEIGHT in repository.currentDraft.availableEquipment)

        viewModel.onEvent(PreferencesEvent.ToggleEquipment(Equipment.BARBELL, false))
        assertTrue(Equipment.BODYWEIGHT in repository.currentDraft.availableEquipment)
    }

    @Test
    fun `bodyweight cannot be deselected`() = runTest {
        viewModel.onEvent(PreferencesEvent.ToggleEquipment(Equipment.BODYWEIGHT, false))

        assertTrue(Equipment.BODYWEIGHT in repository.currentDraft.availableEquipment)
    }

    @Test
    fun `bodyweight is not offered as a selectable option`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()

            assertFalse(Equipment.BODYWEIGHT in state.equipmentOptions)
            assertFalse(Equipment.BODYWEIGHT in state.selectedEquipment)
        }
    }

    /** The Phase 3 decision recorded in docs/follow-ups.md: no cardio in the MVP. */
    @Test
    fun `cardio machine is not offered while the catalog is strength-only`() = runTest {
        viewModel.uiState.test {
            assertFalse(Equipment.CARDIO_MACHINE in awaitItem().equipmentOptions)
        }
    }

    @Test
    fun `the split follows the training frequency`() = runTest {
        viewModel.uiState.test {
            assertNull(awaitItem().derivedSplit)

            viewModel.onEvent(PreferencesEvent.SelectDaysPerWeek(2))
            assertEquals(WorkoutSplit.FULL_BODY, awaitItem().derivedSplit)

            viewModel.onEvent(PreferencesEvent.SelectDaysPerWeek(4))
            assertEquals(WorkoutSplit.UPPER_LOWER, awaitItem().derivedSplit)
        }
    }

    @Test
    fun `a stored draft is loaded back into the screen`() = runTest {
        val stored = FakeProfileRepository(
            initialDraft = completeDraft(
                goal = TrainingGoal.IMPROVE_ENDURANCE,
                daysPerWeek = 5,
                availableEquipment = setOf(Equipment.BODYWEIGHT, Equipment.PULL_UP_BAR),
            ),
        )

        PreferencesViewModel(stored, DetermineWorkoutSplitUseCase()).uiState.test {
            val state = awaitItem()

            assertEquals(TrainingGoal.IMPROVE_ENDURANCE, state.goal)
            assertEquals(5, state.daysPerWeek)
            assertEquals(setOf(Equipment.PULL_UP_BAR), state.selectedEquipment)
            assertEquals(WorkoutSplit.UPPER_LOWER, state.derivedSplit)
            assertTrue(state.canContinue)
        }
    }
}
