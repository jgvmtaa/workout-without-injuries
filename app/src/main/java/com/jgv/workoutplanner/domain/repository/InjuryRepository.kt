package com.jgv.workoutplanner.domain.repository

import com.jgv.workoutplanner.domain.model.BodyRegion
import com.jgv.workoutplanner.domain.model.InjuryDefinition
import com.jgv.workoutplanner.domain.model.InjuryId

/**
 * Read access to the injury catalog.
 *
 * The injury catalog has its own repository boundary so grouping and lookup remain
 * independently testable (spec §4.4, §7, §19).
 */
interface InjuryRepository {

    /** Every selectable injury, in catalog order. */
    fun getAllInjuries(): List<InjuryDefinition>

    /** The definition behind [id]. Non-null by construction, as with the exercise catalog. */
    fun getInjury(id: InjuryId): InjuryDefinition

    /** Injuries belonging to [bodyRegion]. */
    fun getInjuriesByBodyRegion(bodyRegion: BodyRegion): List<InjuryDefinition>

    /**
     * Every injury grouped by region, for the injury-history screen (spec §4.4).
     *
     * Regions with no injuries are omitted, so the screen renders whatever the catalog
     * happens to cover instead of showing empty sections.
     */
    fun getInjuriesGroupedByBodyRegion(): Map<BodyRegion, List<InjuryDefinition>>
}
