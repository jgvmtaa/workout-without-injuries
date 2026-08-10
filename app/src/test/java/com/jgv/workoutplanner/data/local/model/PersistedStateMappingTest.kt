package com.jgv.workoutplanner.data.local.model

import com.jgv.workoutplanner.domain.model.BodySide
import com.jgv.workoutplanner.domain.model.Equipment
import com.jgv.workoutplanner.domain.model.ExperienceLevel
import com.jgv.workoutplanner.domain.model.InjuryId
import com.jgv.workoutplanner.domain.model.InjuryStatus
import com.jgv.workoutplanner.domain.model.MovementLimitation
import com.jgv.workoutplanner.domain.model.OnboardingDraft
import com.jgv.workoutplanner.domain.model.SelectedInjury
import com.jgv.workoutplanner.domain.model.TrainingGoal
import com.jgv.workoutplanner.domain.model.WorkoutSplit
import com.jgv.workoutplanner.testing.completeDraft
import com.jgv.workoutplanner.testing.injury
import com.jgv.workoutplanner.testing.userProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The storage boundary (README §21).
 *
 * Two properties matter here and everything below tests one of them: nothing the user
 * entered is lost on the way through, and nothing a future build changes can make the
 * stored file unreadable.
 */
class PersistedStateMappingTest {

    // ------------------------------------------------------------- Round trips

    @Test
    fun `an empty draft round-trips`() {
        val draft = OnboardingDraft()

        assertEquals(draft, draft.toPersisted().toDomain())
    }

    @Test
    fun `a fully populated draft round-trips`() {
        val draft = completeDraft(
            availableEquipment = setOf(
                Equipment.BODYWEIGHT,
                Equipment.DUMBBELLS,
                Equipment.CABLE_MACHINE,
            ),
            selectedInjuries = setOf(
                injury(InjuryId.KNEE_ACL, InjuryStatus.RECOVERING),
                injury(InjuryId.SHOULDER_IMPINGEMENT, InjuryStatus.CURRENTLY_SYMPTOMATIC),
            ),
            confirmedLimitations = setOf(
                MovementLimitation.AVOID_JUMPING,
                MovementLimitation.AVOID_OVERHEAD_PRESSING,
            ),
        )

        assertEquals(draft, draft.toPersisted().toDomain())
    }

    @Test
    fun `a maximal profile round-trips`() {
        val profile = userProfile(
            goal = TrainingGoal.BUILD_STRENGTH,
            experienceLevel = ExperienceLevel.ADVANCED,
            daysPerWeek = 5,
            sessionDurationMinutes = 75,
            preferredSplit = WorkoutSplit.UPPER_LOWER,
            availableEquipment = Equipment.entries.toSet(),
            selectedInjuries = InjuryId.entries.map { injury(it) }.toSet(),
            movementLimitations = MovementLimitation.entries.toSet(),
        )

        assertEquals(profile, profile.toPersisted().toDomain())
    }

    @Test
    fun `the side of an injury survives even though the engine ignores it`() {
        val profile = userProfile(
            selectedInjuries = setOf(
                SelectedInjury(
                    injuryId = InjuryId.KNEE_ACL,
                    affectedSide = BodySide.LEFT,
                    status = InjuryStatus.RECOVERING,
                ),
            ),
        )

        val restored = profile.toPersisted().toDomain()

        assertEquals(BodySide.LEFT, restored?.selectedInjuries?.single()?.affectedSide)
    }

    // ------------------------------------------------------ Deterministic output

    /**
     * Sets have no order, so without sorting the same state could serialise two ways.
     * A stable file is what makes the round-trip assertions above exact and the file
     * itself diffable.
     */
    @Test
    fun `collections are written in a stable order`() {
        val a = userProfile(
            movementLimitations = linkedSetOf(
                MovementLimitation.AVOID_OVERHEAD_PRESSING,
                MovementLimitation.AVOID_JUMPING,
            ),
        )
        val b = userProfile(
            movementLimitations = linkedSetOf(
                MovementLimitation.AVOID_JUMPING,
                MovementLimitation.AVOID_OVERHEAD_PRESSING,
            ),
        )

        assertEquals(a.toPersisted(), b.toPersisted())
        assertEquals(
            listOf("AVOID_JUMPING", "AVOID_OVERHEAD_PRESSING"),
            a.toPersisted().movementLimitations,
        )
    }

    // ------------------------------------------------------------ Tolerant reads

    /**
     * A limitation removed from the enum in a later build costs the user that one
     * checkbox — not their profile.
     */
    @Test
    fun `an unknown limitation is dropped rather than throwing`() {
        val stored = completeDraft(
            confirmedLimitations = setOf(MovementLimitation.AVOID_JUMPING),
        ).toPersisted().let {
            it.copy(confirmedLimitations = it.confirmedLimitations + "AVOID_TIME_TRAVEL")
        }

        val restored = stored.toDomain()

        assertEquals(setOf(MovementLimitation.AVOID_JUMPING), restored.confirmedLimitations)
    }

    @Test
    fun `an unknown injury is dropped rather than throwing`() {
        val stored = PersistedDraft(
            selectedInjuries = listOf(
                PersistedInjury("KNEE_ACL", "NOT_SPECIFIED", "HISTORICAL"),
                PersistedInjury("SPLEEN_SPRAIN", "NOT_SPECIFIED", "HISTORICAL"),
            ),
        )

        val restored = stored.toDomain()

        assertEquals(setOf(InjuryId.KNEE_ACL), restored.selectedInjuries.map { it.injuryId }.toSet())
    }

    /**
     * Losing "left knee" is survivable; losing the knee is not. An unreadable side or
     * status falls back to the domain default instead of dropping the injury.
     */
    @Test
    fun `an unknown side or status falls back without losing the injury`() {
        val stored = PersistedDraft(
            selectedInjuries = listOf(PersistedInjury("KNEE_ACL", "SIDEWAYS", "SCHRODINGER")),
        )

        val restored = stored.toDomain().selectedInjuries.single()

        assertEquals(InjuryId.KNEE_ACL, restored.injuryId)
        assertEquals(BodySide.NOT_SPECIFIED, restored.affectedSide)
        assertEquals(InjuryStatus.HISTORICAL, restored.status)
    }

    /** Bodyweight is a rule, not a stored choice (README §25). */
    @Test
    fun `bodyweight is always available however the file was written`() {
        val stored = PersistedDraft(availableEquipment = listOf("DUMBBELLS"))

        assertTrue(Equipment.BODYWEIGHT in stored.toDomain().availableEquipment)
    }

    // --------------------------------------------------- Profiles refuse to guess

    /**
     * A profile whose goal cannot be parsed cannot generate a plan. Returning `null`
     * routes the user back through onboarding with their draft intact, which beats
     * planning around a goal they never chose.
     */
    @Test
    fun `a profile with an unreadable required field reads as absent`() {
        val stored = userProfile().toPersisted()

        assertNull(stored.copy(goal = "GET_SWOLE").toDomain())
        assertNull(stored.copy(experienceLevel = "GRANDMASTER").toDomain())
        assertNull(stored.copy(preferredSplit = "BRO_SPLIT").toDomain())
    }

    @Test
    fun `a profile with an unreadable optional field still reads`() {
        val stored = userProfile(
            movementLimitations = setOf(MovementLimitation.AVOID_JUMPING),
        ).toPersisted()

        val restored = stored.copy(
            movementLimitations = stored.movementLimitations + "AVOID_TIME_TRAVEL",
        ).toDomain()

        assertNotNull(restored)
        assertEquals(setOf(MovementLimitation.AVOID_JUMPING), restored?.movementLimitations)
    }
}
