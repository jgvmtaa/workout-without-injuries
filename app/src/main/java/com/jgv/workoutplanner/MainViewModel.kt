package com.jgv.workoutplanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jgv.workoutplanner.core.WhileUiSubscribed
import com.jgv.workoutplanner.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import javax.inject.Inject

/**
 * Decides where the app opens (spec §3).
 *
 * A returning user goes to Home; anyone without a saved profile starts at Welcome. The
 * profile is read from disk, so there is a moment before either is known — that is
 * [StartDestination.Undecided], and the activity shows a loading state rather than
 * guessing and then correcting itself with a visible jump.
 *
 * ## Why the decision is taken once
 * `take(1)` freezes it at the first emission. `NavHost` rebuilds its graph when
 * `startDestination` changes, discarding the back stack — so a live flow would tear down
 * the user's navigation the moment onboarding saved a profile, fighting the explicit
 * `navigate(Home)` that the review screen already performs.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    profileRepository: ProfileRepository,
) : ViewModel() {

    val startDestination: StateFlow<StartDestination> =
        profileRepository.profile
            .map { profile ->
                if (profile == null) StartDestination.Onboarding else StartDestination.Home
            }
            .take(1)
            .stateIn(
                scope = viewModelScope,
                started = WhileUiSubscribed,
                initialValue = StartDestination.Undecided,
            )
}

/** Where the app should open (spec §3). */
enum class StartDestination {
    /** The stored profile has not been read yet. */
    Undecided,

    /** No profile: start the first-launch flow at Welcome. */
    Onboarding,

    /** A profile exists: start at Home. */
    Home,
}
