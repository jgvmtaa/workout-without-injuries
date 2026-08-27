package com.jgv.workoutplanner.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExperienceLevelTest {

    @Test
    fun `BEGINNER only supports BEGINNER`() {
        assertTrue(ExperienceLevel.BEGINNER.supports(ExerciseDifficulty.BEGINNER))
        assertFalse(ExperienceLevel.BEGINNER.supports(ExerciseDifficulty.INTERMEDIATE))
        assertFalse(ExperienceLevel.BEGINNER.supports(ExerciseDifficulty.ADVANCED))
    }

    @Test
    fun `INTERMEDIATE supports BEGINNER and INTERMEDIATE`() {
        assertTrue(ExperienceLevel.INTERMEDIATE.supports(ExerciseDifficulty.BEGINNER))
        assertTrue(ExperienceLevel.INTERMEDIATE.supports(ExerciseDifficulty.INTERMEDIATE))
        assertFalse(ExperienceLevel.INTERMEDIATE.supports(ExerciseDifficulty.ADVANCED))
    }

    @Test
    fun `ADVANCED supports all difficulties`() {
        assertTrue(ExperienceLevel.ADVANCED.supports(ExerciseDifficulty.BEGINNER))
        assertTrue(ExperienceLevel.ADVANCED.supports(ExerciseDifficulty.INTERMEDIATE))
        assertTrue(ExperienceLevel.ADVANCED.supports(ExerciseDifficulty.ADVANCED))
    }
}
