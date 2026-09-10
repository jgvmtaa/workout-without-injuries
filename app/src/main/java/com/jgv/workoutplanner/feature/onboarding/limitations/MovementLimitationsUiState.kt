package com.jgv.workoutplanner.feature.onboarding.limitations

import androidx.annotation.StringRes
import com.jgv.workoutplanner.domain.model.LimitationGroup
import com.jgv.workoutplanner.domain.model.MovementLimitation

/**
 * Immutable state for the movement limitations screen (spec §4.5, §20).
 *
 * ## The shape encodes the product rule
 * [suggested] and [confirmed] are separate fields and one is never derived from the
 * other. A suggestion is a question the app is asking; a confirmation is the user's
 * answer. There is no representation in this state for "suggested, therefore applied" —
 * which is the point (spec §2).
 *
 * [otherGroups] holds everything *not* suggested, grouped for browsing. Suggested
 * limitations are excluded from it so no limitation appears as two separate checkboxes
 * that have to stay in step.
 */
data class MovementLimitationsUiState(
    val isLoading: Boolean = true,
    val suggested: List<LimitationUiModel> = emptyList(),
    val otherGroups: List<LimitationGroupUiModel> = emptyList(),
    val confirmed: Set<MovementLimitation> = emptySet(),
) {

    /** Whether the injury history produced anything to ask about. */
    val hasSuggestions: Boolean get() = suggested.isNotEmpty()

    val confirmedCount: Int get() = confirmed.size

    /**
     * Always true. Confirming nothing is a valid answer, and an injury without a
     * confirmed limitation must not block the flow (spec §25).
     */
    val canContinue: Boolean = true
}

/** One limitation group and the limitations under it (spec §4.5). */
data class LimitationGroupUiModel(
    val group: LimitationGroup,
    @StringRes val groupNameRes: Int,
    val limitations: List<LimitationUiModel>,
)

/** One selectable limitation. */
data class LimitationUiModel(
    val id: MovementLimitation,
    @StringRes val nameRes: Int,
)

/** Everything the limitations screen can do (spec §20). */
sealed interface MovementLimitationsEvent {

    /**
     * Confirms or unconfirms one limitation.
     *
     * The only way anything reaches [MovementLimitationsUiState.confirmed], and therefore
     * the only way anything reaches the exercise filter.
     */
    data class SetConfirmed(
        val limitation: MovementLimitation,
        val confirmed: Boolean,
    ) : MovementLimitationsEvent

    data object Continue : MovementLimitationsEvent

    data object Back : MovementLimitationsEvent
}
