package com.jgv.workoutplanner.core.ui

import com.jgv.workoutplanner.domain.model.BodyRegion
import com.jgv.workoutplanner.domain.model.Equipment
import com.jgv.workoutplanner.domain.model.ExperienceLevel
import com.jgv.workoutplanner.domain.model.InjuryStatus
import com.jgv.workoutplanner.domain.model.LimitationGroup
import com.jgv.workoutplanner.domain.model.TrainingGoal
import com.jgv.workoutplanner.domain.model.WorkoutSplit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Label coverage for the domain enums (README §6).
 *
 * The `when` expressions are exhaustive, so the compiler already guarantees every
 * constant has *a* label. What it cannot catch is two constants pointing at the same
 * string — a copy-paste that would render two different options identically, and which
 * nothing else in the app would flag.
 */
class EnumLabelsTest {

    @Test
    fun `every goal has a distinct label`() {
        assertDistinctLabels(TrainingGoal.entries.associateWith { it.labelRes })
    }

    @Test
    fun `every experience level has a distinct label`() {
        assertDistinctLabels(ExperienceLevel.entries.associateWith { it.labelRes })
    }

    @Test
    fun `every equipment type has a distinct label`() {
        assertDistinctLabels(Equipment.entries.associateWith { it.labelRes })
    }

    @Test
    fun `every split has a distinct label`() {
        assertDistinctLabels(WorkoutSplit.entries.associateWith { it.labelRes })
    }

    @Test
    fun `every body region has a distinct label`() {
        assertDistinctLabels(BodyRegion.entries.associateWith { it.labelRes })
    }

    @Test
    fun `every injury status has a distinct label`() {
        assertDistinctLabels(InjuryStatus.entries.associateWith { it.labelRes })
    }

    @Test
    fun `every limitation group has a distinct label`() {
        assertDistinctLabels(LimitationGroup.entries.associateWith { it.labelRes })
    }

    private fun <T : Enum<T>> assertDistinctLabels(labels: Map<T, Int>) {
        labels.forEach { (value, res) ->
            assertNotEquals("$value has no label resource", 0, res)
        }

        val shared = labels.entries
            .groupBy { it.value }
            .filterValues { it.size > 1 }
            .values
            .map { group -> group.map { it.key } }

        assertEquals("Values sharing a label", emptyList<List<T>>(), shared)
    }
}
