package com.jgv.workoutplanner.domain.usecase

import com.jgv.workoutplanner.domain.model.TrainingOptions
import com.jgv.workoutplanner.domain.model.WorkoutSplit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/** The frequency-to-split mapping from spec §12.1. */
class DetermineWorkoutSplitUseCaseTest {

    private val determineSplit = DetermineWorkoutSplitUseCase()

    @Test
    fun `two days is full body`() {
        assertEquals(WorkoutSplit.FULL_BODY, determineSplit(2))
    }

    @Test
    fun `three days is push pull legs`() {
        assertEquals(WorkoutSplit.PUSH_PULL_LEGS, determineSplit(3))
    }

    @Test
    fun `four days is upper lower`() {
        assertEquals(WorkoutSplit.UPPER_LOWER, determineSplit(4))
    }

    @Test
    fun `five days is upper lower`() {
        assertEquals(WorkoutSplit.UPPER_LOWER, determineSplit(5))
    }

    /** Every frequency the preferences screen offers must map to something. */
    @Test
    fun `every offered frequency has a split`() {
        TrainingOptions.DAYS_PER_WEEK.forEach { days ->
            assertNotNull("No split for $days days", determineSplit(days))
        }
    }

    /**
     * A profile persisted by an older build could carry a frequency this one no longer
     * offers. spec §24.4 requires generation not to crash on odd input, and it starts
     * here.
     */
    @Test
    fun `an unsupported frequency falls back instead of failing`() {
        assertEquals(WorkoutSplit.UPPER_LOWER, determineSplit(6))
        assertEquals(WorkoutSplit.UPPER_LOWER, determineSplit(0))
        assertEquals(WorkoutSplit.UPPER_LOWER, determineSplit(-1))
    }

    /** Same input, same split — the first link in spec §12.5's determinism chain. */
    @Test
    fun `the mapping is deterministic`() {
        repeat(10) {
            assertEquals(WorkoutSplit.PUSH_PULL_LEGS, determineSplit(3))
        }
    }
}
