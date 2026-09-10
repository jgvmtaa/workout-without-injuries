package com.jgv.workoutplanner.feature.onboarding.limitations

import app.cash.turbine.test
import com.jgv.workoutplanner.data.repository.DefaultInjuryRepository
import com.jgv.workoutplanner.domain.model.InjuryId
import com.jgv.workoutplanner.domain.model.MovementLimitation
import com.jgv.workoutplanner.domain.usecase.GetSuggestedLimitationsUseCase
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

/**
 * The screen the whole product rule rests on (spec §2, §4.5, §24.5).
 *
 * Most of what follows is one assertion in different clothes: a suggestion is not a
 * confirmation. It is worth the repetition — this is the behaviour that separates
 * "filters exercises the user asked it to" from "decides what the user can do".
 */
class MovementLimitationsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val suggestUseCase = GetSuggestedLimitationsUseCase(DefaultInjuryRepository())

    private fun viewModel(repository: FakeProfileRepository) =
        MovementLimitationsViewModel(repository, suggestUseCase)

    private fun repositoryWithAcl() = FakeProfileRepository(
        initialDraft = completeDraft(selectedInjuries = setOf(injury(InjuryId.KNEE_ACL))),
    )

    // ------------------------------------------------- Suggestions are only questions

    @Test
    fun `an injury produces suggestions`() = runTest {
        viewModel(repositoryWithAcl()).uiState.test {
            val state = awaitItem()

            assertTrue(state.hasSuggestions)
            assertEquals(
                setOf(
                    MovementLimitation.AVOID_JUMPING,
                    MovementLimitation.AVOID_RAPID_DIRECTION_CHANGE,
                ),
                state.suggested.map { it.id }.toSet(),
            )
        }
    }

    /** The core assertion of spec §2, at the screen that could break it. */
    @Test
    fun `suggestions arrive unconfirmed`() = runTest {
        val repository = repositoryWithAcl()

        viewModel(repository).uiState.test {
            val state = awaitItem()

            assertTrue("Nothing suggested — the test proves nothing", state.hasSuggestions)
            assertEquals(emptySet<MovementLimitation>(), state.confirmed)
            assertTrue(state.suggested.none { it.id in state.confirmed })
        }

        assertEquals(
            "Opening the screen must not write a limitation",
            emptySet<MovementLimitation>(),
            repository.currentDraft.confirmedLimitations,
        )
    }

    @Test
    fun `merely observing the screen never writes anything`() = runTest {
        val repository = repositoryWithAcl()
        val before = repository.currentDraft

        viewModel(repository).uiState.test { awaitItem() }

        assertEquals(before, repository.currentDraft)
    }

    @Test
    fun `no injury history means no suggestions but the screen still works`() = runTest {
        viewModel(FakeProfileRepository(initialDraft = completeDraft())).uiState.test {
            val state = awaitItem()

            assertFalse(state.hasSuggestions)
            assertTrue(state.otherGroups.isNotEmpty())
            assertTrue(state.canContinue)
        }
    }

    // ------------------------------------------------------------- Confirming

    @Test
    fun `confirming is the only thing that adds a limitation`() = runTest {
        val repository = repositoryWithAcl()
        val viewModel = viewModel(repository)

        viewModel.uiState.test {
            assertEquals(emptySet<MovementLimitation>(), awaitItem().confirmed)

            viewModel.onEvent(
                MovementLimitationsEvent.SetConfirmed(MovementLimitation.AVOID_JUMPING, true),
            )

            assertEquals(setOf(MovementLimitation.AVOID_JUMPING), awaitItem().confirmed)
        }

        assertEquals(
            setOf(MovementLimitation.AVOID_JUMPING),
            repository.currentDraft.confirmedLimitations,
        )
    }

    @Test
    fun `unconfirming removes it again`() = runTest {
        val repository = repositoryWithAcl()
        val viewModel = viewModel(repository)

        viewModel.onEvent(
            MovementLimitationsEvent.SetConfirmed(MovementLimitation.AVOID_JUMPING, true),
        )
        viewModel.onEvent(
            MovementLimitationsEvent.SetConfirmed(MovementLimitation.AVOID_JUMPING, false),
        )

        assertEquals(
            emptySet<MovementLimitation>(),
            repository.currentDraft.confirmedLimitations,
        )
    }

    /** spec §4.5: a limitation from a clinician need not relate to a selected injury. */
    @Test
    fun `a limitation unrelated to any injury can be added`() = runTest {
        val repository = repositoryWithAcl()
        val viewModel = viewModel(repository)

        viewModel.onEvent(
            MovementLimitationsEvent.SetConfirmed(
                MovementLimitation.AVOID_OVERHEAD_PRESSING,
                true,
            ),
        )

        viewModel.uiState.test {
            val state = awaitItem()

            assertTrue(MovementLimitation.AVOID_OVERHEAD_PRESSING in state.confirmed)
            assertFalse(
                "Shoulder work is not suggested by an ACL history",
                state.suggested.any { it.id == MovementLimitation.AVOID_OVERHEAD_PRESSING },
            )
        }
    }

    /**
     * Removing the injury that prompted a limitation does not un-answer the question.
     * The limitation stays confirmed and simply moves out of the suggested section.
     */
    @Test
    fun `a confirmed limitation survives the injury that suggested it`() = runTest {
        val repository = FakeProfileRepository(
            initialDraft = completeDraft(
                selectedInjuries = setOf(injury(InjuryId.KNEE_ACL)),
                confirmedLimitations = setOf(MovementLimitation.AVOID_JUMPING),
            ),
        )

        repository.updateDraft { it.copy(selectedInjuries = emptySet()) }

        viewModel(repository).uiState.test {
            val state = awaitItem()

            assertFalse(state.hasSuggestions)
            assertTrue(MovementLimitation.AVOID_JUMPING in state.confirmed)
            assertTrue(
                "A confirmed limitation must still be findable and unticked-able",
                state.otherGroups.flatMap { it.limitations }
                    .any { it.id == MovementLimitation.AVOID_JUMPING },
            )
        }
    }

    // ----------------------------------------------------------------- Layout

    /** A limitation must not appear as two checkboxes that have to stay in step. */
    @Test
    fun `a suggested limitation is not repeated in the browsable list`() = runTest {
        viewModel(repositoryWithAcl()).uiState.test {
            val state = awaitItem()

            val suggested = state.suggested.map { it.id }.toSet()
            val other = state.otherGroups.flatMap { group -> group.limitations.map { it.id } }

            assertTrue(
                "Repeated: ${suggested intersect other.toSet()}",
                (suggested intersect other.toSet()).isEmpty(),
            )
        }
    }

    @Test
    fun `together the two sections cover every limitation exactly once`() = runTest {
        viewModel(repositoryWithAcl()).uiState.test {
            val state = awaitItem()

            val shown = state.suggested.map { it.id } +
                state.otherGroups.flatMap { group -> group.limitations.map { it.id } }

            assertEquals(MovementLimitation.entries.size, shown.size)
            assertEquals(MovementLimitation.entries.toSet(), shown.toSet())
        }
    }

    @Test
    fun `groups emptied by suggestions are dropped rather than shown blank`() = runTest {
        // Elbow surgery suggests both elbow limitations, so the elbow group has nothing
        // left to browse.
        val repository = FakeProfileRepository(
            initialDraft = completeDraft(selectedInjuries = setOf(injury(InjuryId.ELBOW_SURGERY))),
        )

        viewModel(repository).uiState.test {
            val state = awaitItem()

            assertTrue(state.otherGroups.none { it.limitations.isEmpty() })
        }
    }

    @Test
    fun `continue is never blocked`() = runTest {
        viewModel(repositoryWithAcl()).uiState.test {
            assertTrue(awaitItem().canContinue)
        }
    }

    @Test
    fun `navigation events do not touch the draft`() = runTest {
        val repository = repositoryWithAcl()
        val before = repository.currentDraft

        viewModel(repository).onEvent(MovementLimitationsEvent.Continue)
        viewModel(repository).onEvent(MovementLimitationsEvent.Back)

        assertEquals(before, repository.currentDraft)
    }
}
