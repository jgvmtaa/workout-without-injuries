package com.jgv.workoutplanner.feature.onboarding.injuries

import app.cash.turbine.test
import com.jgv.workoutplanner.data.repository.DefaultInjuryRepository
import com.jgv.workoutplanner.domain.model.BodyRegion
import com.jgv.workoutplanner.domain.model.InjuryId
import com.jgv.workoutplanner.domain.model.InjuryStatus
import com.jgv.workoutplanner.domain.model.MovementLimitation
import com.jgv.workoutplanner.testing.FakeProfileRepository
import com.jgv.workoutplanner.testing.MainDispatcherRule
import com.jgv.workoutplanner.testing.completeDraft
import com.jgv.workoutplanner.testing.injury
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Selection and persistence for the injury history step (README §24.5). */
class InjuryHistoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeProfileRepository()
    private val viewModel by lazy { InjuryHistoryViewModel(repository, DefaultInjuryRepository()) }

    @Test
    fun `injuries are grouped by body region in head-to-toe order`() = runTest {
        viewModel.uiState.test {
            val groups = awaitItem().injuryGroups

            assertTrue(groups.isNotEmpty())
            assertEquals(groups.sortedBy { it.region.ordinal }, groups)
            assertTrue(groups.none { it.injuries.isEmpty() })
        }
    }

    @Test
    fun `every catalogued injury appears exactly once`() = runTest {
        viewModel.uiState.test {
            val ids = awaitItem().injuryGroups.flatMap { group -> group.injuries.map { it.id } }

            assertEquals(InjuryId.entries.size, ids.size)
            assertEquals(InjuryId.entries.toSet(), ids.toSet())
        }
    }

    @Test
    fun `a region with no injuries is not shown`() = runTest {
        viewModel.uiState.test {
            val regions = awaitItem().injuryGroups.map { it.region }

            assertFalse(BodyRegion.NECK in regions)
        }
    }

    @Test
    fun `toggling selects and deselects`() = runTest {
        viewModel.uiState.test {
            assertTrue(awaitItem().selectedInjuries.isEmpty())

            viewModel.onEvent(InjuryHistoryEvent.ToggleInjury(InjuryId.KNEE_ACL))
            assertEquals(
                InjuryStatus.HISTORICAL,
                awaitItem().selectedInjuries[InjuryId.KNEE_ACL],
            )

            viewModel.onEvent(InjuryHistoryEvent.ToggleInjury(InjuryId.KNEE_ACL))
            assertTrue(awaitItem().selectedInjuries.isEmpty())
        }
    }

    @Test
    fun `selection is persisted`() = runTest {
        viewModel.onEvent(InjuryHistoryEvent.ToggleInjury(InjuryId.SHOULDER_IMPINGEMENT))

        assertEquals(
            setOf(InjuryId.SHOULDER_IMPINGEMENT),
            repository.currentDraft.selectedInjuries.map { it.injuryId }.toSet(),
        )
    }

    @Test
    fun `status can be changed on a selected injury`() = runTest {
        viewModel.onEvent(InjuryHistoryEvent.ToggleInjury(InjuryId.KNEE_ACL))
        viewModel.onEvent(
            InjuryHistoryEvent.SetStatus(InjuryId.KNEE_ACL, InjuryStatus.RECOVERING),
        )

        assertEquals(
            InjuryStatus.RECOVERING,
            repository.currentDraft.selectedInjuries.single().status,
        )
    }

    @Test
    fun `setting the status of an unselected injury does nothing`() = runTest {
        viewModel.onEvent(
            InjuryHistoryEvent.SetStatus(InjuryId.KNEE_ACL, InjuryStatus.RECOVERING),
        )

        assertTrue(repository.currentDraft.selectedInjuries.isEmpty())
    }

    /**
     * Deselecting drops the status too. Re-selecting must not silently restore an answer
     * the user may have changed their mind about.
     */
    @Test
    fun `deselecting forgets the status`() = runTest {
        viewModel.onEvent(InjuryHistoryEvent.ToggleInjury(InjuryId.KNEE_ACL))
        viewModel.onEvent(
            InjuryHistoryEvent.SetStatus(InjuryId.KNEE_ACL, InjuryStatus.CURRENTLY_SYMPTOMATIC),
        )
        viewModel.onEvent(InjuryHistoryEvent.ToggleInjury(InjuryId.KNEE_ACL))
        viewModel.onEvent(InjuryHistoryEvent.ToggleInjury(InjuryId.KNEE_ACL))

        assertEquals(
            InjuryStatus.HISTORICAL,
            repository.currentDraft.selectedInjuries.single().status,
        )
    }

    /** README §25: selecting nothing is a valid answer. */
    @Test
    fun `continue is never blocked`() = runTest {
        viewModel.uiState.test {
            assertTrue(awaitItem().canContinue)
        }
    }

    /**
     * The product rule at this step: selecting an injury must widen nothing but the
     * question set. If this ever confirmed a limitation, the app would be filtering on a
     * diagnosis the user never agreed to (README §2).
     */
    @Test
    fun `selecting an injury confirms no limitation`() = runTest {
        viewModel.onEvent(InjuryHistoryEvent.ToggleInjury(InjuryId.KNEE_ACL))
        viewModel.onEvent(InjuryHistoryEvent.ToggleInjury(InjuryId.LOWER_BACK_SURGERY))

        assertEquals(
            emptySet<MovementLimitation>(),
            repository.currentDraft.confirmedLimitations,
        )
    }

    @Test
    fun `a stored selection is loaded back into the screen`() = runTest {
        val stored = FakeProfileRepository(
            initialDraft = completeDraft(
                selectedInjuries = setOf(injury(InjuryId.HIP_IMPINGEMENT, InjuryStatus.RECOVERING)),
            ),
        )

        InjuryHistoryViewModel(stored, DefaultInjuryRepository()).uiState.test {
            val selected = awaitItem().selectedInjuries

            assertEquals(InjuryStatus.RECOVERING, selected[InjuryId.HIP_IMPINGEMENT])
            assertNull(selected[InjuryId.KNEE_ACL])
        }
    }
}
