package com.jgv.workoutplanner.data.repository

import com.jgv.workoutplanner.domain.model.BodyRegion
import com.jgv.workoutplanner.domain.model.InjuryId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Phase 2 completion criterion: injuries can be queried by body region (README §27).
 */
class DefaultInjuryRepositoryTest {

    private val repository = DefaultInjuryRepository()

    @Test
    fun `returns the whole catalog`() {
        assertEquals(
            InjuryId.entries.toSet(),
            repository.getAllInjuries().map { it.id }.toSet(),
        )
    }

    @Test
    fun `resolves an injury by id`() {
        val acl = repository.getInjury(InjuryId.KNEE_ACL)

        assertEquals(InjuryId.KNEE_ACL, acl.id)
        assertEquals(BodyRegion.KNEE, acl.bodyRegion)
    }

    @Test
    fun `resolves every id in the enum`() {
        InjuryId.entries.forEach { id ->
            assertEquals(id, repository.getInjury(id).id)
        }
    }

    @Test
    fun `queries by body region return only that region`() {
        val knee = repository.getInjuriesByBodyRegion(BodyRegion.KNEE)

        assertTrue(knee.isNotEmpty())
        assertTrue(knee.all { it.bodyRegion == BodyRegion.KNEE })
        assertTrue(InjuryId.KNEE_ACL in knee.map { it.id })
    }

    @Test
    fun `a region with no injuries returns empty rather than failing`() {
        assertEquals(emptyList<Nothing>(), repository.getInjuriesByBodyRegion(BodyRegion.NECK))
    }

    @Test
    fun `grouping covers every injury exactly once`() {
        val grouped = repository.getInjuriesGroupedByBodyRegion()

        val flattened = grouped.values.flatten().map { it.id }
        assertEquals(InjuryId.entries.size, flattened.size)
        assertEquals(InjuryId.entries.toSet(), flattened.toSet())
    }

    @Test
    fun `grouping omits regions with no injuries`() {
        val grouped = repository.getInjuriesGroupedByBodyRegion()

        assertTrue(grouped.values.none { it.isEmpty() })
        assertTrue(BodyRegion.NECK !in grouped.keys)
    }

    @Test
    fun `grouping is ordered head to toe`() {
        // The injury-history screen renders the map as-is, so the order is part of the
        // contract rather than an implementation detail.
        val regions = repository.getInjuriesGroupedByBodyRegion().keys.toList()

        assertEquals(regions.sortedBy { it.ordinal }, regions)
    }
}
