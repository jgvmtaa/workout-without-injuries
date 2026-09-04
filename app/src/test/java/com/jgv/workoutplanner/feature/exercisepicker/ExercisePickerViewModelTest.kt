package com.jgv.workoutplanner.feature.exercisepicker

import com.jgv.workoutplanner.data.repository.DefaultExerciseRepository
import com.jgv.workoutplanner.domain.model.Equipment
import com.jgv.workoutplanner.domain.model.ExerciseId
import com.jgv.workoutplanner.domain.model.ExperienceLevel
import com.jgv.workoutplanner.domain.model.MuscleGroup
import com.jgv.workoutplanner.domain.model.PlannedExercise
import com.jgv.workoutplanner.domain.model.WorkoutDay
import com.jgv.workoutplanner.domain.model.WorkoutDayFocus
import com.jgv.workoutplanner.domain.model.WorkoutPlan
import com.jgv.workoutplanner.domain.usecase.EvaluateExerciseEligibilityUseCase
import com.jgv.workoutplanner.domain.usecase.GetEligibleExercisesUseCase
import com.jgv.workoutplanner.domain.usecase.GetExercisesForWorkoutDayUseCase
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

class ExercisePickerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val exerciseRepo = DefaultExerciseRepository()
    private val evaluate = EvaluateExerciseEligibilityUseCase()
    private val eligible = GetEligibleExercisesUseCase(exerciseRepo, evaluate)
    private val forDay = GetExercisesForWorkoutDayUseCase(eligible)

    private fun samplePlan(): WorkoutPlan {
        val day = WorkoutDay(
            id = "day-1",
            name = "Full Body 1",
            focus = WorkoutDayFocus.FULL_BODY,
            exercises = listOf(
                PlannedExercise(ExerciseId.PUSH_UP, 3, 8..12, 90, 0),
            ),
        )
        return WorkoutPlan(id = "plan", name = "plan", days = listOf(day))
    }

    private fun createViewModel(
        profileRepo: FakeProfileRepository,
        workoutRepo: FakeWorkoutPlanRepository,
    ): ExercisePickerViewModel {
        return ExercisePickerViewModel(
            route = com.jgv.workoutplanner.navigation.AppRoute.ExercisePicker("day-1"),
            profileRepository = profileRepo,
            workoutPlanRepository = workoutRepo,
            getExercisesForDayUseCase = forDay,
            updateUseCase = UpdateWorkoutExerciseUseCase(workoutRepo, profileRepo, eligible, exerciseRepo),
        )
    }

    @Test
    fun `search filters candidates but retains ordered selection`() = runTest {
        val generous = userProfile(
            experienceLevel = ExperienceLevel.ADVANCED,
            availableEquipment = exerciseRepo.getAllExercises().flatMap { it.requiredEquipment }.toSet() + Equipment.BODYWEIGHT,
        )
        val profileRepo = FakeProfileRepository(initialProfile = generous)
        val workoutRepo = FakeWorkoutPlanRepository(initialPlan = samplePlan())
        val vm = createViewModel(profileRepo, workoutRepo)

        val initial = vm.uiState.first { !it.isLoading }
        assertFalse(initial.isStale)
        assertTrue(initial.allCandidates.isNotEmpty())

        val firstId = initial.allCandidates.first().id
        vm.onEvent(ExercisePickerEvent.ToggleSelection(firstId))
        val afterSelect = vm.uiState.first { it.selectedIdsOrdered.contains(firstId) }
        assertEquals(listOf(firstId), afterSelect.selectedIdsOrdered)

        vm.onEvent(ExercisePickerEvent.SearchQueryChanged("nonexistentquery123"))
        val afterFilter = vm.uiState.first { it.searchQuery == "nonexistentquery123" }
        assertEquals(listOf(firstId), afterFilter.selectedIdsOrdered)
        assertTrue(afterFilter.filteredCandidates.isEmpty())

        vm.onEvent(ExercisePickerEvent.ClearFilters)
        val afterClear = vm.uiState.first { it.searchQuery.isBlank() && it.selectedMuscleGroup == null }
        assertEquals(listOf(firstId), afterClear.selectedIdsOrdered)
    }

    @Test
    fun `muscle and equipment filters work`() = runTest {
        val generous = userProfile(
            experienceLevel = ExperienceLevel.ADVANCED,
            availableEquipment = exerciseRepo.getAllExercises().flatMap { it.requiredEquipment }.toSet() + Equipment.BODYWEIGHT,
        )
        val profileRepo = FakeProfileRepository(initialProfile = generous)
        val workoutRepo = FakeWorkoutPlanRepository(initialPlan = samplePlan())
        val vm = createViewModel(profileRepo, workoutRepo)

        val initial = vm.uiState.first { !it.isLoading }

        vm.onEvent(ExercisePickerEvent.SelectMuscleGroup(MuscleGroup.CHEST))
        val afterMuscle = vm.uiState.first { it.selectedMuscleGroup == MuscleGroup.CHEST }
        afterMuscle.filteredCandidates.forEach { def ->
            assertEquals(MuscleGroup.CHEST, def.primaryMuscle)
        }

        vm.onEvent(ExercisePickerEvent.SelectEquipment(Equipment.DUMBBELLS))
        val afterEquip = vm.uiState.first { it.selectedEquipment == Equipment.DUMBBELLS }
        afterEquip.filteredCandidates.forEach { def ->
            assertTrue(Equipment.DUMBBELLS in def.requiredEquipment)
        }

        vm.onEvent(ExercisePickerEvent.ClearFilters)
        val afterClear = vm.uiState.first { it.selectedMuscleGroup == null && it.selectedEquipment == null }
        assertEquals(initial.filteredCandidates.size, afterClear.filteredCandidates.size)
    }

    @Test
    fun `capacity enforcement prevents over-selection`() = runTest {
        val generous = userProfile(
            experienceLevel = ExperienceLevel.ADVANCED,
            availableEquipment = exerciseRepo.getAllExercises().flatMap { it.requiredEquipment }.toSet() + Equipment.BODYWEIGHT,
        )
        val profileRepo = FakeProfileRepository(initialProfile = generous)
        val workoutRepo = FakeWorkoutPlanRepository(initialPlan = samplePlan())
        val vm = createViewModel(profileRepo, workoutRepo)

        val initial = vm.uiState.first { !it.isLoading }
        val capacity = initial.remainingCapacity
        assertTrue(capacity > 0)

        val candidates = initial.allCandidates.take(capacity)
        candidates.forEach { def ->
            vm.onEvent(ExercisePickerEvent.ToggleSelection(def.id))
        }

        val afterFull = vm.uiState.first { it.selectedIdsOrdered.size == capacity }
        assertEquals(capacity, afterFull.selectedIdsOrdered.size)

        val extra = initial.allCandidates.drop(capacity).firstOrNull()
        if (extra != null) {
            vm.onEvent(ExercisePickerEvent.ToggleSelection(extra.id))
            val afterExtra = vm.uiState.value
            assertTrue(afterExtra.selectedIdsOrdered.size <= capacity)
        }
    }

    @Test
    fun `atomic confirmation preserves selection order`() = runTest {
        val generous = userProfile(
            experienceLevel = ExperienceLevel.ADVANCED,
            availableEquipment = exerciseRepo.getAllExercises().flatMap { it.requiredEquipment }.toSet() + Equipment.BODYWEIGHT,
        )
        val profileRepo = FakeProfileRepository(initialProfile = generous)
        val workoutRepo = FakeWorkoutPlanRepository(initialPlan = samplePlan())
        val vm = createViewModel(profileRepo, workoutRepo)

        val initial = vm.uiState.first { !it.isLoading }
        val toSelect = initial.allCandidates.take(2).map { it.id }
        assertEquals(2, toSelect.size)

        vm.onEvent(ExercisePickerEvent.ToggleSelection(toSelect[0]))
        vm.onEvent(ExercisePickerEvent.ToggleSelection(toSelect[1]))

        val selectedState = vm.uiState.first { it.selectedIdsOrdered == toSelect }
        assertEquals(toSelect, selectedState.selectedIdsOrdered)

        vm.onEvent(ExercisePickerEvent.Confirm)

        val plan = workoutRepo.currentPlan.first { p ->
            p?.days?.first()?.exercises?.size == 1 + 2
        }
        val day = plan!!.days.first()
        val lastTwo = day.exercises.takeLast(2).map { it.exerciseId }
        assertEquals(toSelect, lastTwo)
    }
}
