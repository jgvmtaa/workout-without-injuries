package com.jgv.workoutplanner.data.catalog

import com.jgv.workoutplanner.domain.model.LimitationGroup
import com.jgv.workoutplanner.domain.model.MovementLimitation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Structural guarantees for the limitation catalog (README §4.5, §6).
 *
 * The same shape of test as [ExerciseCatalogTest] and [InjuryCatalogTest]: the catalog is
 * hand-written data, so what protects it is exhaustiveness checks rather than examples.
 */
class MovementLimitationCatalogTest {

    private val limitations = MovementLimitationCatalog.limitations

    /**
     * A missing entry would make the limitations screen silently omit a limitation the
     * user might need — and there would be nothing on screen to notice.
     */
    @Test
    fun `every limitation has exactly one catalog entry`() {
        val ids = limitations.map { it.id }

        val duplicates = ids.groupBy { it }.filterValues { it.size > 1 }.keys
        assertEquals("Duplicate catalog entries", emptySet<MovementLimitation>(), duplicates)

        val missing = MovementLimitation.entries.toSet() - ids.toSet()
        assertEquals("Limitations with no catalog entry", emptySet<MovementLimitation>(), missing)
    }

    @Test
    fun `every limitation has its own label`() {
        val shared = limitations
            .groupBy { it.nameRes }
            .filterValues { it.size > 1 }
            .values
            .map { group -> group.map { it.id } }

        assertEquals("Limitations sharing a label", emptyList<List<MovementLimitation>>(), shared)
    }

    @Test
    fun `no label resource is missing`() {
        val unlabelled = limitations.filter { it.nameRes == 0 }.map { it.id }

        assertEquals(emptyList<MovementLimitation>(), unlabelled)
    }

    @Test
    fun `every id resolves to its definition`() {
        MovementLimitation.entries.forEach { id ->
            assertEquals(id, MovementLimitationCatalog.definition(id).id)
        }
    }

    /**
     * The catalog is written in the same order as the enum, so the two files can be read
     * side by side. Worth pinning: the screen renders catalog order, so a reordering is a
     * user-visible change and should be deliberate.
     */
    @Test
    fun `catalog order matches the enum declaration order`() {
        assertEquals(MovementLimitation.entries.toList(), limitations.map { it.id })
    }

    /** Every group is used, so no section of the screen can be empty. */
    @Test
    fun `every group has at least one limitation`() {
        val used = limitations.map { it.group }.toSet()

        assertEquals(LimitationGroup.entries.toSet(), used)
    }

    @Test
    fun `grouping covers every limitation exactly once`() {
        val grouped = MovementLimitationCatalog.groupedForDisplay()
        val flattened = grouped.values.flatten().map { it.id }

        assertEquals(MovementLimitation.entries.size, flattened.size)
        assertEquals(MovementLimitation.entries.toSet(), flattened.toSet())
    }

    @Test
    fun `grouping is ordered by group declaration order`() {
        val groups = MovementLimitationCatalog.groupedForDisplay().keys.toList()

        assertEquals(groups.sortedBy { it.ordinal }, groups)
    }

    /**
     * Cross-check against the injury catalog: everything an injury can suggest has to be
     * renderable, or the limitations screen would have a suggestion it cannot draw.
     */
    @Test
    fun `every limitation an injury suggests is catalogued`() {
        val suggested = InjuryCatalog.injuries.flatMap { it.suggestedLimitations }.toSet()
        val catalogued = limitations.map { it.id }.toSet()

        assertTrue(
            "Suggested but not catalogued: ${suggested - catalogued}",
            catalogued.containsAll(suggested),
        )
    }
}
