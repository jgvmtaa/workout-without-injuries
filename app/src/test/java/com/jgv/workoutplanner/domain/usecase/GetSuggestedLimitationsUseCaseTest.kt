package com.jgv.workoutplanner.domain.usecase

import com.jgv.workoutplanner.data.repository.DefaultInjuryRepository
import com.jgv.workoutplanner.domain.model.InjuryId
import com.jgv.workoutplanner.domain.model.InjuryStatus
import com.jgv.workoutplanner.domain.model.MovementLimitation
import com.jgv.workoutplanner.testing.completeDraft
import com.jgv.workoutplanner.testing.injury
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The suggestion half of the core product rule (spec §2, §4.5, §24.3).
 *
 * Run against the real injury catalog rather than a stub. The behaviour worth protecting
 * is "the catalog's suggestions reach the screen unchanged and nothing else happens",
 * and a stubbed catalog would only prove the plumbing.
 */
class GetSuggestedLimitationsUseCaseTest {

    private val useCase = GetSuggestedLimitationsUseCase(DefaultInjuryRepository())

    /** The worked example from spec §4.5, and the test §24.3 asks for. */
    @Test
    fun `ACL history suggests jumping and direction-change limitations`() {
        val suggestions = useCase(setOf(injury(InjuryId.KNEE_ACL)))

        assertEquals(
            setOf(
                MovementLimitation.AVOID_JUMPING,
                MovementLimitation.AVOID_RAPID_DIRECTION_CHANGE,
            ),
            suggestions,
        )
    }

    /**
     * The other half of §24.3: suggesting must not confirm.
     *
     * The use case is given the draft's injuries and its output is compared against the
     * draft's confirmed limitations — which stay empty. There is no path from injury to
     * filter that does not go through the user.
     */
    @Test
    fun `suggestions do not become confirmed limitations`() {
        val draft = completeDraft(selectedInjuries = setOf(injury(InjuryId.KNEE_ACL)))

        val suggestions = useCase(draft.selectedInjuries)

        assertTrue("Nothing to suggest — the test is not exercising anything", suggestions.isNotEmpty())
        assertEquals(
            "Selecting an injury must not confirm anything",
            emptySet<MovementLimitation>(),
            draft.confirmedLimitations,
        )
    }

    @Test
    fun `no injuries suggests nothing`() {
        assertEquals(emptySet<MovementLimitation>(), useCase(emptySet()))
    }

    @Test
    fun `suggestions from several injuries are unioned`() {
        val suggestions = useCase(
            setOf(injury(InjuryId.KNEE_ACL), injury(InjuryId.SHOULDER_IMPINGEMENT)),
        )

        assertTrue(MovementLimitation.AVOID_JUMPING in suggestions)
        assertTrue(MovementLimitation.AVOID_OVERHEAD_PRESSING in suggestions)
    }

    /**
     * Two injuries suggesting the same limitation must produce one suggestion — the
     * screen renders this set directly, and a duplicate would be a duplicate checkbox.
     */
    @Test
    fun `an overlapping suggestion appears once`() {
        val both = useCase(
            setOf(injury(InjuryId.KNEE_ACL), injury(InjuryId.KNEE_PATELLAR_TENDON)),
        )

        // Both suggest AVOID_JUMPING; a Set makes the count structural, so assert the
        // union is exactly what each contributes rather than trusting the type.
        val acl = useCase(setOf(injury(InjuryId.KNEE_ACL)))
        val patellar = useCase(setOf(injury(InjuryId.KNEE_PATELLAR_TENDON)))

        assertTrue(MovementLimitation.AVOID_JUMPING in acl)
        assertTrue(MovementLimitation.AVOID_JUMPING in patellar)
        assertEquals(acl + patellar, both)
    }

    /**
     * spec §4.4: the MVP engine ignores how current an injury is. A historical ACL
     * raises the same question as one that hurts today — the user decides the answer.
     */
    @Test
    fun `injury status does not change the suggestions`() {
        val historical = useCase(setOf(injury(InjuryId.KNEE_ACL, InjuryStatus.HISTORICAL)))
        val symptomatic =
            useCase(setOf(injury(InjuryId.KNEE_ACL, InjuryStatus.CURRENTLY_SYMPTOMATIC)))

        assertEquals(historical, symptomatic)
    }

    /** Every catalogued injury suggests something, so no selection is a dead end. */
    @Test
    fun `every injury produces at least one suggestion`() {
        val silent = InjuryId.entries.filter { useCase(setOf(injury(it))).isEmpty() }

        assertEquals(emptyList<InjuryId>(), silent)
    }
}
