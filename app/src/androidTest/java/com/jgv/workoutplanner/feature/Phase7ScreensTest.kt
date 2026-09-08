package com.jgv.workoutplanner.feature

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.jgv.workoutplanner.R
import com.jgv.workoutplanner.core.designsystem.AppTheme
import com.jgv.workoutplanner.data.catalog.ExerciseCatalog
import com.jgv.workoutplanner.data.catalog.MovementLimitationCatalog
import com.jgv.workoutplanner.domain.model.ExclusionReason
import com.jgv.workoutplanner.domain.model.ExerciseAvailability
import com.jgv.workoutplanner.domain.model.ExerciseEligibility
import com.jgv.workoutplanner.domain.model.ExerciseId
import com.jgv.workoutplanner.domain.model.MovementLimitation
import com.jgv.workoutplanner.domain.model.PlanWarning
import com.jgv.workoutplanner.domain.model.WorkoutDayFocus
import com.jgv.workoutplanner.feature.exercisedetails.ExerciseDetailsScreen
import com.jgv.workoutplanner.feature.exercisedetails.ExerciseDetailsUiState
import com.jgv.workoutplanner.feature.exerciselibrary.ExerciseLibraryScreen
import com.jgv.workoutplanner.feature.exerciselibrary.ExerciseLibraryUiState
import com.jgv.workoutplanner.feature.exerciselibrary.ExerciseRowUiModel
import com.jgv.workoutplanner.feature.plan.ExerciseReplacementEvent
import com.jgv.workoutplanner.feature.plan.ExerciseReplacementScreen
import com.jgv.workoutplanner.feature.plan.ExerciseReplacementUiState
import com.jgv.workoutplanner.feature.plan.PendingExerciseRemoval
import com.jgv.workoutplanner.feature.plan.PlanEvent
import com.jgv.workoutplanner.feature.plan.PlanScreen
import com.jgv.workoutplanner.feature.plan.PlanUiState
import com.jgv.workoutplanner.feature.plan.PlannedExerciseUiModel
import com.jgv.workoutplanner.feature.plan.WorkoutDayUiModel
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Compose coverage for the Phase 7 plan, replacement, and exclusion flows. */
class Phase7ScreensTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val resources = InstrumentationRegistry.getInstrumentation().targetContext.resources

    private fun text(resId: Int, vararg args: Any): String = resources.getString(resId, *args)

    @Test
    fun generatePlan_emitsGenerateEvent() {
        val events = mutableListOf<PlanEvent>()
        composeRule.setContent {
            AppTheme {
                PlanScreen(
                    state = PlanUiState(isLoading = false, hasNoPlan = true),
                    showRegenerateConfirm = false,
                    pendingRemoval = null,
                    snackbarHostState = SnackbarHostState(),
                    onEvent = { events += it },
                )
            }
        }

        composeRule.onNodeWithText(text(R.string.action_generate_plan)).performClick()

        assertEquals(listOf(PlanEvent.GeneratePlan), events)
    }

    @Test
    fun partialPlan_showsLocalizedNoMatchMessage() {
        composeRule.setContent {
            AppTheme {
                PlanScreen(
                    state = PlanUiState(
                        isLoading = false,
                        hasNoPlan = false,
                        warnings = listOf(PlanWarning(0, WorkoutDayFocus.FULL_BODY, "core")),
                    ),
                    showRegenerateConfirm = false,
                    pendingRemoval = null,
                    snackbarHostState = SnackbarHostState(),
                    onEvent = {},
                )
            }
        }

        composeRule.onNodeWithText(text(R.string.plan_slot_no_match), substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun removeExercise_requestsConfirmationBeforeDestructiveEvent() {
        val events = mutableListOf<PlanEvent>()
        val exercise = ExerciseCatalog.exercises.first { it.id == ExerciseId.MACHINE_CHEST_PRESS }
        composeRule.setContent {
            AppTheme {
                PlanScreen(
                    state = planState(exercise.id),
                    showRegenerateConfirm = false,
                    pendingRemoval = null,
                    snackbarHostState = SnackbarHostState(),
                    onEvent = { events += it },
                )
            }
        }

        composeRule.onNodeWithContentDescription(
            text(R.string.plan_more_actions, text(exercise.nameRes)),
        ).performClick()
        composeRule.onNodeWithText(text(R.string.plan_action_remove)).performClick()

        assertEquals(
            listOf(PlanEvent.RequestRemoveExercise("day-1", exercise.id)),
            events,
        )
    }

    @Test
    fun removeExerciseDialog_emitsConfirmEvent() {
        val events = mutableListOf<PlanEvent>()
        val exerciseId = ExerciseId.MACHINE_CHEST_PRESS
        composeRule.setContent {
            AppTheme {
                PlanScreen(
                    state = planState(exerciseId),
                    showRegenerateConfirm = false,
                    pendingRemoval = PendingExerciseRemoval("day-1", exerciseId),
                    snackbarHostState = SnackbarHostState(),
                    onEvent = { events += it },
                )
            }
        }

        composeRule.onNodeWithText(text(R.string.plan_remove_confirm_title)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.plan_remove_confirm_ok)).performClick()

        assertEquals(listOf(PlanEvent.ConfirmRemoveExercise), events)
    }

    @Test
    fun replacement_selectsCandidateAndConfirms() {
        val candidate = ExerciseCatalog.exercises.first { it.id == ExerciseId.PUSH_UP }
        val events = mutableListOf<ExerciseReplacementEvent>()
        var selectedId by mutableStateOf<ExerciseId?>(null)
        composeRule.setContent {
            AppTheme {
                ExerciseReplacementScreen(
                    state = ExerciseReplacementUiState(
                        isLoading = false,
                        candidates = listOf(candidate),
                        selectedExerciseId = selectedId,
                    ),
                    snackbarHostState = SnackbarHostState(),
                    onEvent = { event ->
                        when (event) {
                            is ExerciseReplacementEvent.SelectCandidate -> selectedId = event.exerciseId
                            else -> events += event
                        }
                    },
                )
            }
        }

        composeRule.onNodeWithText(text(candidate.nameRes)).performClick()
        composeRule.onNodeWithText(text(R.string.replacement_confirm))
            .assertIsEnabled()
            .performClick()

        assertEquals(listOf(ExerciseReplacementEvent.ConfirmReplacement), events)
    }

    @Test
    fun exerciseDetails_showsConfirmedLimitationReason() {
        val limitation = MovementLimitation.AVOID_UNSUPPORTED_HIP_HINGE
        val exercise = ExerciseCatalog.exercises.first {
            it.id == ExerciseId.DUMBBELL_ROMANIAN_DEADLIFT
        }
        val eligibility = ExerciseEligibility(
            exercise = exercise,
            isEligible = false,
            exclusionReasons = setOf(ExclusionReason.ConflictingLimitation(limitation)),
        )
        composeRule.setContent {
            AppTheme {
                ExerciseDetailsScreen(
                    state = ExerciseDetailsUiState(
                        isLoading = false,
                        definition = exercise,
                        eligibility = eligibility,
                        availability = ExerciseAvailability.EXCLUDED,
                    ),
                    onEvent = {},
                )
            }
        }

        val limitationName = text(MovementLimitationCatalog.definition(limitation).nameRes)
        composeRule.onNodeWithText(
            text(R.string.detail_conflicting_limitation_reason, limitationName),
        ).assertIsDisplayed()
    }

    @Test
    fun library_allExcluded_showsSafetyGuidance() {
        val limitation = MovementLimitation.AVOID_UNSUPPORTED_HIP_HINGE
        val exercise = ExerciseCatalog.exercises.first {
            it.id == ExerciseId.DUMBBELL_ROMANIAN_DEADLIFT
        }
        val eligibility = ExerciseEligibility(
            exercise = exercise,
            isEligible = false,
            exclusionReasons = setOf(ExclusionReason.ConflictingLimitation(limitation)),
        )
        val row = ExerciseRowUiModel(exercise, eligibility, ExerciseAvailability.EXCLUDED)
        composeRule.setContent {
            AppTheme {
                ExerciseLibraryScreen(
                    state = ExerciseLibraryUiState(
                        isLoading = false,
                        allRows = listOf(row),
                        filteredRows = listOf(row),
                        excludedCount = 1,
                    ),
                    onEvent = {},
                )
            }
        }

        composeRule.onNodeWithText(text(R.string.library_all_excluded)).assertIsDisplayed()
    }

    private fun planState(exerciseId: ExerciseId): PlanUiState {
        val definition = ExerciseCatalog.exercises.first { it.id == exerciseId }
        return PlanUiState(
            isLoading = false,
            planName = "Plan",
            hasNoPlan = false,
            days = listOf(
                WorkoutDayUiModel(
                    id = "day-1",
                    name = "Day 1",
                    focus = WorkoutDayFocus.FULL_BODY,
                    exercises = listOf(
                        PlannedExerciseUiModel(exerciseId, definition, 3, 8..12, 90, 0),
                    ),
                ),
            ),
        )
    }
}
