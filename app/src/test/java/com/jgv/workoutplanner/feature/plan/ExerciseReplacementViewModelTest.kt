package com.jgv.workoutplanner.feature.plan

import com.jgv.workoutplanner.data.repository.DefaultExerciseRepository
import com.jgv.workoutplanner.domain.model.Equipment
import com.jgv.workoutplanner.domain.model.ExerciseId
import com.jgv.workoutplanner.domain.model.ExperienceLevel
import com.jgv.workoutplanner.domain.model.PlannedExercise
import com.jgv.workoutplanner.domain.model.WorkoutDay
import com.jgv.workoutplanner.domain.model.WorkoutDayFocus
import com.jgv.workoutplanner.domain.model.WorkoutPlan
import com.jgv.workoutplanner.domain.usecase.EvaluateExerciseEligibilityUseCase
import com.jgv.workoutplanner.domain.usecase.GetEligibleExercisesUseCase
import com.jgv.workoutplanner.domain.usecase.GetExerciseReplacementsUseCase
import com.jgv.workoutplanner.domain.usecase.UpdateWorkoutExerciseUseCase
import com.jgv.workoutplanner.testing.FakeProfileRepository
import com.jgv.workoutplanner.testing.FakeWorkoutPlanRepository
import com.jgv.workoutplanner.testing.MainDispatcherRule
import com.jgv.workoutplanner.testing.userProfile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ExerciseReplacementViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val exerciseRepo = DefaultExerciseRepository()
    private val evaluate = EvaluateExerciseEligibilityUseCase()
    private val eligible = GetEligibleExercisesUseCase(exerciseRepo, evaluate)
    private val replacements = GetExerciseReplacementsUseCase(eligible, exerciseRepo)

    private fun samplePlan(): WorkoutPlan {
        val day = WorkoutDay(
            id = "day-1",
            name = "Full Body 1",
            focus = WorkoutDayFocus.FULL_BODY,
            exercises = listOf(
                PlannedExercise(ExerciseId.PUSH_UP, 3, 8..12, 90, 0),
                PlannedExercise(ExerciseId.LAT_PULLDOWN, 3, 8..12, 90, 1),
            ),
        )
        return WorkoutPlan(id = "plan", name = "plan", days = listOf(day))
    }

    private fun createViewModel(
        workoutDayId: String = "day-1",
        exerciseId: ExerciseId = ExerciseId.PUSH_UP,
        profileRepo: FakeProfileRepository,
        workoutRepo: FakeWorkoutPlanRepository,
    ): ExerciseReplacementViewModel {
        return ExerciseReplacementViewModel(
            route = com.jgv.workoutplanner.navigation.AppRoute.ExerciseReplacement(
                workoutDayId = workoutDayId,
                exerciseId = exerciseId,
            ),
            profileRepository = profileRepo,
            workoutPlanRepository = workoutRepo,
            exerciseRepository = exerciseRepo,
            getReplacementsUseCase = replacements,
            updateUseCase = UpdateWorkoutExerciseUseCase(workoutRepo, profileRepo, eligible, exerciseRepo),
        )
    }

    @Test
    fun `stale when plan missing navigates back`() = runTest {
        val profileRepo = FakeProfileRepository(initialProfile = userProfile())
        val workoutRepo = FakeWorkoutPlanRepository(initialPlan = null)

        val vm = createViewModel(profileRepo = profileRepo, workoutRepo = workoutRepo)

        val state = vm.uiState.first { !it.isLoading }
        assertTrue(state.isStale)
    }

    @Test
    fun `stale when day missing`() = runTest {
        val profileRepo = FakeProfileRepository(initialProfile = userProfile())
        val workoutRepo = FakeWorkoutPlanRepository(initialPlan = samplePlan())

        val vm = createViewModel(workoutDayId = "nonexistent", profileRepo = profileRepo, workoutRepo = workoutRepo)

        val state = vm.uiState.first { !it.isLoading }
        assertTrue(state.isStale)
    }

    @Test
    fun `stale when exercise missing`() = runTest {
        val profileRepo = FakeProfileRepository(initialProfile = userProfile())
        val workoutRepo = FakeWorkoutPlanRepository(initialPlan = samplePlan())

        val vm = createViewModel(exerciseId = ExerciseId.MACHINE_CHEST_PRESS, profileRepo = profileRepo, workoutRepo = workoutRepo)

        val state = vm.uiState.first { !it.isLoading }
        assertTrue(state.isStale)
    }

    @Test
    fun `candidates loaded excluding current and day duplicates`() = runTest {
        val generous = userProfile(
            experienceLevel = ExperienceLevel.ADVANCED,
            availableEquipment = exerciseRepo.getAllExercises().flatMap { it.requiredEquipment }.toSet() + Equipment.BODYWEIGHT,
        )
        val profileRepo = FakeProfileRepository(initialProfile = generous)
        val workoutRepo = FakeWorkoutPlanRepository(initialPlan = samplePlan())

        val vm = createViewModel(profileRepo = profileRepo, workoutRepo = workoutRepo)

        val state = vm.uiState.first { !it.isLoading }
        assertFalse(state.isStale)
        assertFalse(state.noCandidates)
        assertTrue(state.candidates.isNotEmpty())
        assertFalse(state.candidates.any { it.id == ExerciseId.PUSH_UP })
        assertFalse(state.candidates.any { it.id == ExerciseId.LAT_PULLDOWN })
    }

    @Test
    fun `selection marks pending and confirm emits NavigateBack only on Updated`() = runTest {
        val generous = userProfile(
            experienceLevel = ExperienceLevel.ADVANCED,
            availableEquipment = exerciseRepo.getAllExercises().flatMap { it.requiredEquipment }.toSet() + Equipment.BODYWEIGHT,
        )
        val profileRepo = FakeProfileRepository(initialProfile = generous)
        val workoutRepo = FakeWorkoutPlanRepository(initialPlan = samplePlan())

        val vm = createViewModel(profileRepo = profileRepo, workoutRepo = workoutRepo)

        val state = vm.uiState.first { !it.isLoading && !it.noCandidates }
        val candidateId = state.candidates.first().id

        vm.onEvent(ExerciseReplacementEvent.SelectCandidate(candidateId))
        val afterSelect = vm.uiState.first { it.selectedExerciseId == candidateId }
        assertEquals(candidateId, afterSelect.selectedExerciseId)

        vm.onEvent(ExerciseReplacementEvent.ConfirmReplacement)

        val effect = vm.effects.first()
        assertTrue(effect is ExerciseReplacementEffect.NavigateBack)
    }

    @Test
    fun `failure emits typed EditFailed not NavigateBack`() = runTest {
        // Profile only bodyweight, so replacement with machine not eligible
        val restrictive = userProfile(
            experienceLevel = ExperienceLevel.BEGINNER,
            availableEquipment = setOf(Equipment.BODYWEIGHT),
        )
        val profileRepo = FakeProfileRepository(initialProfile = restrictive)
        val workoutRepo = FakeWorkoutPlanRepository(initialPlan = samplePlan())

        val vm = createViewModel(profileRepo = profileRepo, workoutRepo = workoutRepo)

        // Force a replacement that is ineligible by directly calling event with machine id
        // Even though candidates may be empty, we can still attempt to confirm with ineligible id
        // by selecting via event (bypasses candidate filtering, but use case will reject)
        vm.onEvent(ExerciseReplacementEvent.SelectCandidate(ExerciseId.MACHINE_CHEST_PRESS))
        vm.onEvent(ExerciseReplacementEvent.ConfirmReplacement)

        val effect = vm.effects.first()
        assertTrue(effect is ExerciseReplacementEffect.EditFailed)
    }
}
