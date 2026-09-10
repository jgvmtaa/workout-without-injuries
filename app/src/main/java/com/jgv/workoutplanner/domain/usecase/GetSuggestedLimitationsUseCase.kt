package com.jgv.workoutplanner.domain.usecase

import com.jgv.workoutplanner.domain.model.MovementLimitation
import com.jgv.workoutplanner.domain.model.SelectedInjury
import com.jgv.workoutplanner.domain.repository.InjuryRepository
import javax.inject.Inject

/**
 * Derives the limitations worth *asking about* from the injuries the user selected
 * (spec §4.5, §19).
 *
 * This is the hinge of the whole product rule (spec §2). It returns a suggestion set
 * and nothing else: it does not touch the draft, the profile, or the confirmed
 * limitations, so there is no code path by which selecting an injury silently excludes
 * an exercise. The limitations screen presents what this returns as unticked boxes; the
 * user's ticks are the only thing that reaches the filter.
 *
 * Suggestions are the union across selected injuries — two injuries suggesting the same
 * limitation produce one suggestion, and a limitation is never suggested "twice".
 * [com.jgv.workoutplanner.domain.model.SelectedInjury.status] and `affectedSide` are not
 * read: a historical injury raises the same question as a current one, and the answer
 * is the user's to give (spec §4.4).
 */
class GetSuggestedLimitationsUseCase @Inject constructor(
    private val injuryRepository: InjuryRepository,
) {

    /** Every limitation suggested by [selectedInjuries]. Empty when nothing is selected. */
    operator fun invoke(selectedInjuries: Set<SelectedInjury>): Set<MovementLimitation> =
        selectedInjuries
            .flatMap { injuryRepository.getInjury(it.injuryId).suggestedLimitations }
            .toSet()
}
