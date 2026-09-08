package com.jgv.workoutplanner.feature.home

import app.cash.turbine.test
import com.jgv.workoutplanner.domain.model.ExerciseId
import com.jgv.workoutplanner.domain.model.ExperienceLevel
import com.jgv.workoutplanner.domain.model.PlannedExercise
import com.jgv.workoutplanner.domain.model.TrainingGoal
import com.jgv.workoutplanner.domain.model.WorkoutDay
import com.jgv.workoutplanner.domain.model.WorkoutDayFocus
import com.jgv.workoutplanner.domain.model.WorkoutPlan
import com.jgv.workoutplanner.testing.FakeProfileRepository
import com.jgv.workoutplanner.testing.FakeWorkoutPlanRepository
import com.jgv.workoutplanner.testing.MainDispatcherRule
import com.jgv.workoutplanner.testing.userProfile
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun samplePlan(): WorkoutPlan {
        val day = WorkoutDay(
            id = "day-1",
            name = "Full",
            focus = WorkoutDayFocus.FULL_BODY,
            exercises = listOf(PlannedExercise(ExerciseId.PUSH_UP, 3, 8..12, 90, 0)),
        )
        return WorkoutPlan(id = "p", name = "p", days = listOf(day))
    }

    @Test
    fun `outdated status exposed on home`() = runTest {
        val profileRepo = FakeProfileRepository(initialProfile = userProfile())
        val workoutRepo = FakeWorkoutPlanRepository(initialPlan = samplePlan(), initialRequiresRegeneration = true)

        val vm = HomeViewModel(profileRepo, workoutRepo)

        vm.uiState.test {
            var state = awaitItem()
            if (state.isLoading) state = awaitItem()
            assertTrue(state.requiresRegeneration)
            assertTrue(state.hasPlan)
        }
    }

    @Test
    fun `no outdated when no plan change`() = runTest {
        val profileRepo = FakeProfileRepository(initialProfile = userProfile())
        val workoutRepo = FakeWorkoutPlanRepository(initialPlan = samplePlan(), initialRequiresRegeneration = false)

        val vm = HomeViewModel(profileRepo, workoutRepo)

        vm.uiState.test {
            var state = awaitItem()
            if (state.isLoading) state = awaitItem()
            assertFalse(state.requiresRegeneration)
        }
    }

    @Test
    fun `profile summary exposes typed values for localized rendering`() = runTest {
        val profileRepo = FakeProfileRepository(
            initialProfile = userProfile(
                goal = TrainingGoal.BUILD_MUSCLE,
                experienceLevel = ExperienceLevel.INTERMEDIATE,
                daysPerWeek = 4,
            ),
        )
        val vm = HomeViewModel(profileRepo, FakeWorkoutPlanRepository())

        vm.uiState.test {
            var state = awaitItem()
            if (state.isLoading) state = awaitItem()
            assertEquals(4, state.daysPerWeek)
            assertEquals(TrainingGoal.BUILD_MUSCLE, state.goal)
            assertEquals(ExperienceLevel.INTERMEDIATE, state.experienceLevel)
        }
    }
}
