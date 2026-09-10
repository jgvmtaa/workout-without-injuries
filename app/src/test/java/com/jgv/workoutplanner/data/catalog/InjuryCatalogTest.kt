package com.jgv.workoutplanner.data.catalog

import com.jgv.workoutplanner.domain.model.BodyRegion
import com.jgv.workoutplanner.domain.model.InjuryId
import com.jgv.workoutplanner.domain.model.MovementLimitation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Structural guarantees for the injury catalog (spec §4.5, §7). */
class InjuryCatalogTest {

    private val injuries = InjuryCatalog.injuries

    @Test
    fun `every injury id has exactly one catalog entry`() {
        val idsInCatalog = injuries.map { it.id }

        val duplicates = idsInCatalog.groupBy { it }.filterValues { it.size > 1 }.keys
        assertEquals("Duplicate catalog entries", emptySet<InjuryId>(), duplicates)

        val missing = InjuryId.entries.toSet() - idsInCatalog.toSet()
        assertEquals("Injury ids with no catalog entry", emptySet<InjuryId>(), missing)
    }

    @Test
    fun `every injury has its own label`() {
        val shared = injuries
            .groupBy { it.nameRes }
            .filterValues { it.size > 1 }
            .values
            .map { group -> group.map { it.id } }
        assertEquals("Injuries sharing a name resource", emptyList<List<InjuryId>>(), shared)
    }

    /**
     * An injury with no suggested limitations would present the user an empty
     * limitations screen and silently drop them into plan generation unfiltered.
     */
    @Test
    fun `every injury suggests at least one limitation`() {
        val withoutSuggestions = injuries
            .filter { it.suggestedLimitations.isEmpty() }
            .map { it.id }
        assertEquals(emptyList<InjuryId>(), withoutSuggestions)
    }

    /** The worked example from spec §4.5, pinned so it cannot drift. */
    @Test
    fun `ACL injury suggests avoiding jumping and rapid direction changes`() {
        val acl = injuries.single { it.id == InjuryId.KNEE_ACL }

        assertEquals(BodyRegion.KNEE, acl.bodyRegion)
        assertEquals(
            setOf(
                MovementLimitation.AVOID_JUMPING,
                MovementLimitation.AVOID_RAPID_DIRECTION_CHANGE,
            ),
            acl.suggestedLimitations,
        )
    }

    /**
     * spec §4.4 lists the regions with injury options. Neck and upper back are defined
     * on [BodyRegion] but have no options yet — asserted explicitly so adding one is a
     * deliberate change to this list rather than an accident.
     */
    @Test
    fun `injuries cover the expected body regions`() {
        val covered = injuries.map { it.bodyRegion }.toSet()

        val expected = setOf(
            BodyRegion.SHOULDER,
            BodyRegion.ELBOW,
            BodyRegion.WRIST_HAND,
            BodyRegion.LOWER_BACK,
            BodyRegion.HIP,
            BodyRegion.KNEE,
            BodyRegion.ANKLE_FOOT,
        )

        assertEquals(expected, covered)
    }

    /**
     * Every region the catalog covers offers an undiagnosed-pain option, so nobody has to
     * pick a diagnosis they were never given in order to be heard (spec §2).
     */
    @Test
    fun `every covered body region offers an undiagnosed pain option`() {
        val regionsWithUndiagnosedOption = injuries
            .filter { it.id.name.endsWith("UNDIAGNOSED_PAIN") }
            .map { it.bodyRegion }
            .toSet()

        val coveredRegions = injuries.map { it.bodyRegion }.toSet()

        assertEquals(
            "Regions with injury options but no undiagnosed-pain option",
            emptySet<BodyRegion>(),
            coveredRegions - regionsWithUndiagnosedOption,
        )
    }

    /**
     * Surgery is the most conservative history in each region, so it should never
     * suggest less than the general injury for the same region.
     */
    @Test
    fun `surgery suggests at least what the general injury of the same region suggests`() {
        val generalByRegion = injuries
            .filter { it.id.name.endsWith("_GENERAL") }
            .associateBy { it.bodyRegion }

        val surgeries = injuries.filter { it.id.name.endsWith("_SURGERY") }

        surgeries.forEach { surgery ->
            val general = generalByRegion[surgery.bodyRegion] ?: return@forEach
            assertTrue(
                "${surgery.id} suggests less than ${general.id}: " +
                    "missing ${general.suggestedLimitations - surgery.suggestedLimitations}",
                surgery.suggestedLimitations.containsAll(general.suggestedLimitations),
            )
        }
    }
}
