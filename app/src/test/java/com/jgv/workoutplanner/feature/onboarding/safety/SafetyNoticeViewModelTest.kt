package com.jgv.workoutplanner.feature.onboarding.safety

import app.cash.turbine.test
import com.jgv.workoutplanner.domain.model.OnboardingDraft
import com.jgv.workoutplanner.testing.FakeProfileRepository
import com.jgv.workoutplanner.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Selection, continue-state and persistence for the safety notice (spec §24.5). */
class SafetyNoticeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeProfileRepository()
    private val viewModel by lazy { SafetyNoticeViewModel(repository) }

    @Test
    fun `all four acknowledgements are exposed`() {
        assertEquals(4, SafetyNoticeUiState().acknowledgements.size)
        assertEquals(
            "Acknowledgements should be distinct",
            4,
            SafetyNoticeUiState().acknowledgements.toSet().size,
        )
    }

    @Test
    fun `continue is blocked until the box is ticked`() = runTest {
        viewModel.uiState.test {
            assertFalse(awaitItem().canContinue)

            viewModel.onEvent(SafetyNoticeEvent.SetAcknowledged(true))

            assertTrue(awaitItem().canContinue)
        }
    }

    /**
     * Persisting on toggle rather than on continue is what keeps navigation from racing
     * the write (see the ViewModel's KDoc), so it is asserted rather than assumed.
     */
    @Test
    fun `acknowledging writes to the draft immediately`() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.onEvent(SafetyNoticeEvent.SetAcknowledged(true))
            awaitItem()
        }

        assertTrue(repository.currentDraft.hasAcceptedSafetyNotice)
    }

    @Test
    fun `un-acknowledging writes through too`() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.onEvent(SafetyNoticeEvent.SetAcknowledged(true))
            awaitItem()
            viewModel.onEvent(SafetyNoticeEvent.SetAcknowledged(false))

            assertFalse(awaitItem().canContinue)
        }

        assertFalse(repository.currentDraft.hasAcceptedSafetyNotice)
    }

    /** Someone who accepted and then navigated back should not be asked twice. */
    @Test
    fun `a previously accepted notice loads as accepted`() = runTest {
        val accepted = FakeProfileRepository(
            initialDraft = OnboardingDraft(hasAcceptedSafetyNotice = true),
        )

        SafetyNoticeViewModel(accepted).uiState.test {
            val state = awaitItem()
            assertTrue(state.isAcknowledged)
            assertFalse(state.isLoading)
        }
    }

    @Test
    fun `navigation events do not touch the draft`() = runTest {
        viewModel.onEvent(SafetyNoticeEvent.Continue)
        viewModel.onEvent(SafetyNoticeEvent.Back)

        assertEquals(OnboardingDraft(), repository.currentDraft)
    }
}
