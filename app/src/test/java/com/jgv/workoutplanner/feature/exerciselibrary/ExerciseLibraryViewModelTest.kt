package com.jgv.workoutplanner.feature.exerciselibrary

import app.cash.turbine.test
import com.jgv.workoutplanner.data.catalog.ExerciseCatalog
import com.jgv.workoutplanner.data.repository.DefaultExerciseRepository
import com.jgv.workoutplanner.domain.model.Equipment
import com.jgv.workoutplanner.domain.model.ExerciseAvailability
import com.jgv.workoutplanner.domain.model.ExerciseEligibility
import com.jgv.workoutplanner.domain.model.ExerciseId
import com.jgv.workoutplanner.domain.model.ExclusionReason
import com.jgv.workoutplanner.domain.model.ExperienceLevel
import com.jgv.workoutplanner.domain.model.MovementLimitation
import com.jgv.workoutplanner.domain.usecase.EvaluateExerciseEligibilityUseCase
import com.jgv.workoutplanner.domain.usecase.GetEligibleExercisesUseCase
import com.jgv.workoutplanner.testing.FakeProfileRepository
import com.jgv.workoutplanner.testing.MainDispatcherRule
import com.jgv.workoutplanner.testing.userProfile
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ExerciseLibraryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createViewModel(): Pair<ExerciseLibraryViewModel, FakeProfileRepository> {
        val allEquipment = ExerciseCatalog.exercises.flatMap { it.requiredEquipment }.toSet() + Equipment.BODYWEIGHT
        val profile = userProfile(
            experienceLevel = ExperienceLevel.ADVANCED,
            availableEquipment = allEquipment,
        )
        val repository = FakeProfileRepository(initialProfile = profile)
        val exerciseRepository = DefaultExerciseRepository()
        val evaluate = EvaluateExerciseEligibilityUseCase()
        val getEligible = GetEligibleExercisesUseCase(exerciseRepository, evaluate)
        val viewModel = ExerciseLibraryViewModel(repository, getEligible)
        return viewModel to repository
    }

    @Test
    fun `two-word query chest press matches MACHINE_CHEST_PRESS`() = runTest {
        val (viewModel, _) = createViewModel()

        viewModel.uiState.test {
            // Initial state with all exercises
            val initial = awaitItem()
            // Wait until loaded
            val loaded = if (initial.isLoading) awaitItem() else initial

            // No filter should include the target
            assertTrue(
                "Initial load should contain MACHINE_CHEST_PRESS",
                loaded.allRows.any { it.definition.id == ExerciseId.MACHINE_CHEST_PRESS },
            )

            // Search with two-word query containing space
            viewModel.onEvent(ExerciseLibraryEvent.SearchQueryChanged("chest press"))

            val filtered = awaitItem()

            assertTrue(
                "Query 'chest press' should match MACHINE_CHEST_PRESS after underscore->space normalization",
                filtered.filteredRows.any { it.definition.id == ExerciseId.MACHINE_CHEST_PRESS },
            )
        }
    }

    @Test
    fun `underscore query chest_press also matches`() = runTest {
        val (viewModel, _) = createViewModel()

        viewModel.uiState.test {
            val initial = awaitItem()
            val loaded = if (initial.isLoading) awaitItem() else initial

            viewModel.onEvent(ExerciseLibraryEvent.SearchQueryChanged("chest_press"))

            val filtered = awaitItem()

            assertTrue(
                "Query 'chest_press' should match MACHINE_CHEST_PRESS after normalization",
                filtered.filteredRows.any { it.definition.id == ExerciseId.MACHINE_CHEST_PRESS },
            )
        }
    }

    @Test
    fun `single word query still works`() = runTest {
        val (viewModel, _) = createViewModel()

        viewModel.uiState.test {
            val initial = awaitItem()
            if (initial.isLoading) awaitItem()

            viewModel.onEvent(ExerciseLibraryEvent.SearchQueryChanged("squat"))

            val filtered = awaitItem()

            assertTrue(
                "Query 'squat' should match at least one exercise",
                filtered.filteredRows.isNotEmpty(),
            )
        }
    }

    @Test
    fun `all filtered excluded is true only for limitation exclusions`() {
        val exercise = ExerciseCatalog.exercises.first()
        val excluded = ExerciseRowUiModel(
            definition = exercise,
            eligibility = ExerciseEligibility(
                exercise = exercise,
                isEligible = false,
                exclusionReasons = setOf(
                    ExclusionReason.ConflictingLimitation(MovementLimitation.AVOID_HIGH_IMPACT),
                ),
            ),
            availability = ExerciseAvailability.EXCLUDED,
        )
        val unavailable = ExerciseRowUiModel(
            definition = exercise,
            eligibility = ExerciseEligibility(
                exercise = exercise,
                isEligible = false,
                exclusionReasons = setOf(ExclusionReason.MissingEquipment(Equipment.DUMBBELLS)),
            ),
            availability = ExerciseAvailability.UNAVAILABLE,
        )

        assertTrue(
            ExerciseLibraryUiState(isLoading = false, filteredRows = listOf(excluded))
                .areAllFilteredExercisesExcluded,
        )
        assertFalse(
            ExerciseLibraryUiState(isLoading = false, filteredRows = listOf(unavailable))
                .areAllFilteredExercisesExcluded,
        )
        assertFalse(
            ExerciseLibraryUiState(isLoading = false, filteredRows = listOf(excluded, unavailable))
                .areAllFilteredExercisesExcluded,
        )
        assertFalse(
            ExerciseLibraryUiState(isLoading = false).areAllFilteredExercisesExcluded,
        )
    }
}
