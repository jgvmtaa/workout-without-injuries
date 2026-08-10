package com.jgv.workoutplanner.feature.onboarding.review

import androidx.annotation.StringRes

/**
 * Immutable state for the profile review screen (README §3, §4, §20).
 *
 * Everything is pre-resolved to string resources here rather than passed as domain
 * enums, because this screen only reads: it has no interaction that needs to know a
 * value's identity, and flattening it keeps the composable free of label lookups.
 *
 * [isComplete] is `false` when an earlier step was skipped — reachable by navigating
 * back and clearing an answer. The screen says so and disables the finish action rather
 * than assembling a profile with a guessed value.
 */
data class ProfileReviewUiState(
    val isLoading: Boolean = true,
    val isComplete: Boolean = false,
    @StringRes val goalRes: Int? = null,
    @StringRes val experienceRes: Int? = null,
    val daysPerWeek: Int? = null,
    val sessionDurationMinutes: Int? = null,
    @StringRes val splitRes: Int? = null,
    val equipment: List<Int> = emptyList(),
    val injuries: List<ReviewInjuryUiModel> = emptyList(),
    val limitations: List<Int> = emptyList(),
) {
    val canContinue: Boolean get() = isComplete
}

/** One selected injury as shown on the review screen. */
data class ReviewInjuryUiModel(
    @StringRes val nameRes: Int,
    @StringRes val statusRes: Int,
)

/** Everything the review screen can do (README §20). */
sealed interface ProfileReviewEvent {

    /** Assembles and saves the profile, ending onboarding. */
    data object Confirm : ProfileReviewEvent

    data object Back : ProfileReviewEvent
}
