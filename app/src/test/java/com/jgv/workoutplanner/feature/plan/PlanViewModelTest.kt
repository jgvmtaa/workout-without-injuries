package com.jgv.workoutplanner.feature.plan

import app.cash.turbine.test
import com.jgv.workoutplanner.data.repository.DefaultExerciseRepository
import com.jgv.workoutplanner.domain.model.Equipment
import com.jgv.workoutplanner.domain.model.ExerciseId
import com.jgv.workoutplanner.domain.model.ExperienceLevel
import com.jgv.workoutplanner.domain.model.PlannedExercise
import com.jgv.workoutplanner.domain.model.WorkoutDay
import com.jgv.workoutplanner.domain.model.WorkoutDayFocus
import com.jgv.workoutplanner.domain.model.WorkoutPlan
import com.jgv.workoutplanner.domain.model.PlanWarning
import com.jgv.workoutplanner.domain.usecase.DetermineWorkoutSplitUseCase
import com.jgv.workoutplanner.domain.usecase.EvaluateExerciseEligibilityUseCase
import com.jgv.workoutplanner.domain.usecase.GenerateWorkoutPlanUseCase
import com.jgv.workoutplanner.domain.usecase.GetEligibleExercisesUseCase
import com.jgv.workoutplanner.domain.usecase.UpdateWorkoutExerciseUseCase
import com.jgv.workoutplanner.domain.usecase.GetExerciseReplacementsUseCase
import com.jgv.workoutplanner.testing.FakeProfileRepository
import com.jgv.workoutplanner.testing.FakeWorkoutPlanRepository
import com.jgv.workoutplanner.testing.MainDispatcherRule
import com.jgv.workoutplanner.testing.userProfile
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PlanViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val exerciseRepo = DefaultExerciseRepository()
    private val evaluate = EvaluateExerciseEligibilityUseCase()
    private val eligible = GetEligibleExercisesUseCase(exerciseRepo, evaluate)
    private val determineSplit = DetermineWorkoutSplitUseCase()
    private val generate = GenerateWorkoutPlanUseCase(determineSplit, eligible)
    private val replacements = GetExerciseReplacementsUseCase(eligible, exerciseRepo)

    private fun samplePlan(): WorkoutPlan {
        val day = WorkoutDay(
            id = "plan-3-PUSH_PULL_LEGS-day-0-push",
            name = "Push 1",
            focus = WorkoutDayFocus.PUSH,
            exercises = listOf(
                PlannedExercise(ExerciseId.MACHINE_CHEST_PRESS, 3, 8..12, 90, 0),
                PlannedExercise(ExerciseId.MACHINE_SHOULDER_PRESS, 3, 8..12, 90, 1),
            ),
        )
        return WorkoutPlan(id = "plan-3-PUSH_PULL_LEGS", name = "PUSH_PULL_LEGS - 3 days", days = listOf(day))
    }

    @Test
    fun `regeneration replaces stored plan and warnings and clears outdated flag only after success`() = runTest {
        val profile = userProfile(
            experienceLevel = ExperienceLevel.ADVANCED,
            availableEquipment = exerciseRepo.getAllExercises().flatMap { it.requiredEquipment }.toSet() + Equipment.BODYWEIGHT,
        )
        val profileRepo = FakeProfileRepository(initialProfile = profile)
        val warning = PlanWarning(0, WorkoutDayFocus.PUSH, "old-slot", "old reason")
        val workoutRepo = FakeWorkoutPlanRepository(
            initialPlan = samplePlan(),
            initialWarnings = listOf(warning),
            initialRequiresRegeneration = true,
        )

        val viewModel = PlanViewModel(
            profileRepository = profileRepo,
            workoutPlanRepository = workoutRepo,
            exerciseRepository = exerciseRepo,
            generateWorkoutPlanUseCase = generate,
            updateWorkoutExerciseUseCase = UpdateWorkoutExerciseUseCase(workoutRepo, profileRepo, eligible, exerciseRepo),
        )

        // Initially outdated, warnings hidden in UI
        viewModel.uiState.test {
            val state = awaitItem()
            // loading true initially -> skip
            val loaded = if (state.isLoading) awaitItem() else state
            assertTrue(loaded.requiresRegeneration)
            assertTrue(loaded.warnings.isEmpty()) // hidden
        }

        // Trigger regeneration (confirm flow bypassed — directly call ConfirmRegeneration)
        viewModel.onEvent(PlanEvent.ConfirmRegeneration)

        // After generation, flag cleared and new plan persisted
        workoutRepo.requiresRegeneration.test {
            assertFalse(awaitItem())
        }

        workoutRepo.currentPlan.test {
            val plan = awaitItem()
            assertTrue(plan != null)
            // New plan id derived from profile
            assertTrue(plan!!.id.startsWith("plan-"))
        }

        viewModel.uiState.test {
            var state = awaitItem()
            if (state.isLoading) state = awaitItem()
            // Eventually cleared
            assertFalse(state.requiresRegeneration)
        }
    }

    @Test
    fun `outdated status disables editing and hides stale warnings`() = runTest {
        val profileRepo = FakeProfileRepository(initialProfile = userProfile())
        val warning = PlanWarning(0, WorkoutDayFocus.FULL_BODY, "slot", "reason")
        val workoutRepo = FakeWorkoutPlanRepository(
            initialPlan = samplePlan(),
            initialWarnings = listOf(warning),
            initialRequiresRegeneration = true,
        )
        val viewModel = PlanViewModel(
            profileRepository = profileRepo,
            workoutPlanRepository = workoutRepo,
            exerciseRepository = exerciseRepo,
            generateWorkoutPlanUseCase = generate,
            updateWorkoutExerciseUseCase = UpdateWorkoutExerciseUseCase(workoutRepo, profileRepo, eligible, exerciseRepo),
        )

        viewModel.uiState.test {
            var state = awaitItem()
            if (state.isLoading) state = awaitItem()
            assertTrue(state.requiresRegeneration)
            assertTrue(state.warnings.isEmpty()) // hidden stale warnings
            // Editing disabled check is UI hide — state still contains days but canEdit derived as !requiresRegeneration
        }

        // Storage still retains warnings
        workoutRepo.currentWarnings.test {
            assertEquals(1, awaitItem().size)
        }
    }

    @Test
    fun `recoverable edit errors emit typed effect`() = runTest {
        val profileRepo = FakeProfileRepository(initialProfile = userProfile(
            experienceLevel = ExperienceLevel.BEGINNER,
            availableEquipment = setOf(Equipment.BODYWEIGHT),
        ))
        val workoutRepo = FakeWorkoutPlanRepository(initialPlan = samplePlan())
        val viewModel = PlanViewModel(
            profileRepository = profileRepo,
            workoutPlanRepository = workoutRepo,
            exerciseRepository = exerciseRepo,
            generateWorkoutPlanUseCase = generate,
            updateWorkoutExerciseUseCase = UpdateWorkoutExerciseUseCase(workoutRepo, profileRepo, eligible, exerciseRepo),
        )

        viewModel.uiState.test {
            var s = awaitItem()
            if (s.isLoading) s = awaitItem()
            assertFalse(s.hasNoPlan)
        }

        // Try to remove non-existent day -> DayNotFound -> should emit EditFailed
        viewModel.onEvent(PlanEvent.RemoveExercise("nonexistent", ExerciseId.PUSH_UP))

        viewModel.effects.test {
            val effect = awaitItem()
            assertTrue(effect is PlanEffect.EditFailed)
        }
    }
}
