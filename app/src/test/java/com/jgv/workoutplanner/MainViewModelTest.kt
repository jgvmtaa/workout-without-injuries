package com.jgv.workoutplanner

import app.cash.turbine.test
import com.jgv.workoutplanner.testing.FakeProfileRepository
import com.jgv.workoutplanner.testing.MainDispatcherRule
import com.jgv.workoutplanner.testing.userProfile
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Returning-user routing (spec §3).
 *
 * `StartDestination.Undecided` is the initial value and is what the activity renders
 * while the profile is read from disk, but it is not asserted here: the test dispatcher
 * is unconfined, so the stored value has already replaced it by the time a collector
 * arrives. What matters — and what these pin — is where it settles.
 */
class MainViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `the initial value is undecided`() {
        val viewModel = MainViewModel(FakeProfileRepository(initialProfile = null))

        // Read without collecting, so nothing has started the upstream flow yet.
        assertEquals(StartDestination.Undecided, viewModel.startDestination.value)
    }

    @Test
    fun `no stored profile starts onboarding`() = runTest {
        val viewModel = MainViewModel(FakeProfileRepository(initialProfile = null))

        viewModel.startDestination.test {
            assertEquals(StartDestination.Onboarding, awaitItem())
        }
    }

    @Test
    fun `a stored profile starts at home`() = runTest {
        val viewModel = MainViewModel(FakeProfileRepository(initialProfile = userProfile()))

        viewModel.startDestination.test {
            assertEquals(StartDestination.Home, awaitItem())
        }
    }

    /**
     * `NavHost` rebuilds its graph when `startDestination` changes, discarding the back
     * stack. Saving a profile mid-session must therefore not move this — the review
     * screen navigates to Home itself.
     */
    @Test
    fun `the decision does not change when a profile is saved later`() = runTest {
        val repository = FakeProfileRepository(initialProfile = null)
        val viewModel = MainViewModel(repository)

        viewModel.startDestination.test {
            assertEquals(StartDestination.Onboarding, awaitItem())

            repository.saveProfile(userProfile())

            expectNoEvents()
        }
    }
}
