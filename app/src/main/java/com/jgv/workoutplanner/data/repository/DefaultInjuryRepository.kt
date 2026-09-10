package com.jgv.workoutplanner.data.repository

import com.jgv.workoutplanner.data.catalog.InjuryCatalog
import com.jgv.workoutplanner.domain.model.BodyRegion
import com.jgv.workoutplanner.domain.model.InjuryDefinition
import com.jgv.workoutplanner.domain.model.InjuryId
import com.jgv.workoutplanner.domain.repository.InjuryRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [InjuryRepository] backed by the static [InjuryCatalog] (spec §7, §19).
 *
 * Same shape as [DefaultExerciseRepository]: index once, serve from memory.
 */
@Singleton
class DefaultInjuryRepository @Inject constructor() : InjuryRepository {

    private val injuries: List<InjuryDefinition> = InjuryCatalog.injuries

    private val byId: Map<InjuryId, InjuryDefinition> = injuries.associateBy { it.id }

    // Catalog order is preserved within each region, and BodyRegion's declaration order
    // determines the grouping order — so the injury-history screen lists regions
    // head-to-toe without sorting anything itself.
    private val byBodyRegion: Map<BodyRegion, List<InjuryDefinition>> =
        injuries
            .groupBy { it.bodyRegion }
            .toSortedMap(compareBy { it.ordinal })

    override fun getAllInjuries(): List<InjuryDefinition> = injuries

    override fun getInjury(id: InjuryId): InjuryDefinition =
        requireNotNull(byId[id]) { "No catalog entry for injury id $id" }

    override fun getInjuriesByBodyRegion(bodyRegion: BodyRegion): List<InjuryDefinition> =
        byBodyRegion[bodyRegion].orEmpty()

    override fun getInjuriesGroupedByBodyRegion(): Map<BodyRegion, List<InjuryDefinition>> =
        byBodyRegion
}
