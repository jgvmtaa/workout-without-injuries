package com.jgv.workoutplanner.feature.onboarding.injuries

import androidx.annotation.StringRes
import com.jgv.workoutplanner.domain.model.BodyRegion
import com.jgv.workoutplanner.domain.model.InjuryId
import com.jgv.workoutplanner.domain.model.InjuryStatus

/**
 * Immutable state for the injury history screen (spec §4.4, §20).
 *
 * Modelled as [injuryGroups] plus a [selectedInjuries] map rather than a list of
 * definitions with a `selected` flag: the catalog part never changes while the screen is
 * open, so keeping it separate means a tap recomposes the selection and not the catalog.
 *
 * The map's value is the [InjuryStatus] for that injury, which is only meaningful once
 * the injury is selected — presence in the map *is* the selection.
 */
data class InjuryHistoryUiState(
    val isLoading: Boolean = true,
    val injuryGroups: List<InjuryGroupUiModel> = emptyList(),
    val selectedInjuries: Map<InjuryId, InjuryStatus> = emptyMap(),
) {

    val selectedCount: Int get() = selectedInjuries.size

    /**
     * Always true — selecting nothing is a valid answer (spec §25).
     *
     * Present so the screen reads the same way as the others rather than special-casing
     * a step that happens to have no requirement.
     */
    val canContinue: Boolean = true
}

/** One body region and the injuries listed under it (spec §4.4, §20). */
data class InjuryGroupUiModel(
    val region: BodyRegion,
    @StringRes val regionNameRes: Int,
    val injuries: List<InjuryUiModel>,
)

/** One selectable injury. */
data class InjuryUiModel(
    val id: InjuryId,
    @StringRes val nameRes: Int,
)

/** Everything the injury history screen can do (spec §20). */
sealed interface InjuryHistoryEvent {

    /** Adds the injury with the default status, or removes it if already selected. */
    data class ToggleInjury(val injuryId: InjuryId) : InjuryHistoryEvent

    /** Changes how current a selected injury is. No effect on an unselected one. */
    data class SetStatus(val injuryId: InjuryId, val status: InjuryStatus) : InjuryHistoryEvent

    data object Continue : InjuryHistoryEvent

    data object Back : InjuryHistoryEvent
}
