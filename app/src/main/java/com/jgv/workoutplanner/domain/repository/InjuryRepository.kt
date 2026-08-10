package com.jgv.workoutplanner.domain.repository

import com.jgv.workoutplanner.domain.model.BodyRegion
import com.jgv.workoutplanner.domain.model.InjuryDefinition
import com.jgv.workoutplanner.domain.model.InjuryId

/**
 * Read access to the injury catalog.
 *
 * README §19 sketches three repositories and does not name this one, but "injuries can
 * be queried by body region" is a Phase 2 completion criterion (README §27) and that
 * query does not belong on [ExerciseRepository]. Kept separate rather than merged so
 * the two catalogs stay independently replaceable.
 */
interface InjuryRepository {

    /** Every selectable injury, in catalog order. */
    fun getAllInjuries(): List<InjuryDefinition>

    /** The definition behind [id]. Non-null by construction, as with the exercise catalog. */
    fun getInjury(id: InjuryId): InjuryDefinition

    /** Injuries belonging to [bodyRegion]. */
    fun getInjuriesByBodyRegion(bodyRegion: BodyRegion): List<InjuryDefinition>

    /**
     * Every injury grouped by region, for the injury-history screen (README §4.4).
     *
     * Regions with no injuries are omitted, so the screen renders whatever the catalog
     * happens to cover instead of showing empty sections.
     */
    fun getInjuriesGroupedByBodyRegion(): Map<BodyRegion, List<InjuryDefinition>>
}
