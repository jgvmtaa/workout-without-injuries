package com.jgv.workoutplanner.domain.model

import com.jgv.workoutplanner.testing.completeDraft
import com.jgv.workoutplanner.testing.injury
import com.jgv.workoutplanner.testing.userProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** The draft-to-profile boundary (README §4, §10). */
class OnboardingDraftTest {

    @Test
    fun `a new draft is incomplete and produces no profile`() {
        val draft = OnboardingDraft()

        assertFalse(draft.isComplete)
        assertNull(draft.toUserProfile(WorkoutSplit.FULL_BODY))
    }

    @Test
    fun `a new draft already has bodyweight available`() {
        assertEquals(setOf(Equipment.BODYWEIGHT), OnboardingDraft().availableEquipment)
    }

    @Test
    fun `a complete draft produces a profile`() {
        val profile = completeDraft().toUserProfile(WorkoutSplit.PUSH_PULL_LEGS)

        assertNotNull(profile)
        assertEquals(TrainingGoal.BUILD_MUSCLE, profile!!.goal)
        assertEquals(WorkoutSplit.PUSH_PULL_LEGS, profile.preferredSplit)
    }

    /**
     * Each answer is individually required. Table-driven so a new required field cannot
     * be added without either being covered here or making [OnboardingDraft.isComplete]
     * wrong.
     */
    @Test
    fun `every missing answer makes the draft incomplete`() {
        val missing = mapOf(
            "safety notice" to completeDraft().copy(hasAcceptedSafetyNotice = false),
            "goal" to completeDraft().copy(goal = null),
            "experience" to completeDraft().copy(experienceLevel = null),
            "days per week" to completeDraft().copy(daysPerWeek = null),
            "session duration" to completeDraft().copy(sessionDurationMinutes = null),
            "equipment" to completeDraft().copy(availableEquipment = emptySet()),
        )

        missing.forEach { (what, draft) ->
            assertFalse("Draft with no $what should be incomplete", draft.isComplete)
            assertNull(
                "Draft with no $what should produce no profile",
                draft.toUserProfile(WorkoutSplit.FULL_BODY),
            )
        }
    }

    /** README §25: an injury with nothing confirmed must not block completion. */
    @Test
    fun `an injury with no confirmed limitation still completes`() {
        val draft = completeDraft(selectedInjuries = setOf(injury(InjuryId.KNEE_ACL)))

        assertTrue(draft.isComplete)
        val profile = draft.toUserProfile(WorkoutSplit.FULL_BODY)
        assertEquals(emptySet<MovementLimitation>(), profile?.movementLimitations)
    }

    /**
     * Only confirmed limitations cross into the profile. This is the last point at which
     * an injury could leak into the filter, so it is asserted at the boundary rather than
     * trusted from the screen above it (README §2).
     */
    @Test
    fun `only confirmed limitations reach the profile`() {
        val draft = completeDraft(
            selectedInjuries = setOf(injury(InjuryId.KNEE_ACL)),
            confirmedLimitations = setOf(MovementLimitation.AVOID_JUMPING),
        )

        val profile = draft.toUserProfile(WorkoutSplit.FULL_BODY)

        assertEquals(setOf(MovementLimitation.AVOID_JUMPING), profile?.movementLimitations)
    }

    @Test
    fun `a draft built from a profile round-trips back to it`() {
        val original = userProfile(
            goal = TrainingGoal.BUILD_STRENGTH,
            experienceLevel = ExperienceLevel.ADVANCED,
            daysPerWeek = 4,
            sessionDurationMinutes = 75,
            preferredSplit = WorkoutSplit.UPPER_LOWER,
            availableEquipment = setOf(Equipment.BODYWEIGHT, Equipment.BARBELL),
            selectedInjuries = setOf(injury(InjuryId.LOWER_BACK_GENERAL)),
            movementLimitations = setOf(MovementLimitation.AVOID_LOADED_SPINAL_FLEXION),
        )

        val restored = OnboardingDraft.from(original).toUserProfile(original.preferredSplit)

        assertEquals(original, restored)
    }

    /**
     * A profile from an older build could carry a frequency this build no longer offers.
     * Re-deriving the split must not silently drop the rest of the profile.
     */
    @Test
    fun `a profile with an unusual frequency still round-trips`() {
        val original = userProfile(daysPerWeek = 6, preferredSplit = WorkoutSplit.UPPER_LOWER)

        val restored = OnboardingDraft.from(original).toUserProfile(WorkoutSplit.UPPER_LOWER)

        assertEquals(original, restored)
    }
}
