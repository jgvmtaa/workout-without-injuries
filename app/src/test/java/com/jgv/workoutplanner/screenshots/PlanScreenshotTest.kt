package com.jgv.workoutplanner.screenshots

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.SnackbarHostState
import com.jgv.workoutplanner.core.ui.TestTags
import com.jgv.workoutplanner.data.catalog.ExerciseCatalog
import com.jgv.workoutplanner.domain.model.ExerciseDefinition
import com.jgv.workoutplanner.domain.model.ExerciseId
import com.jgv.workoutplanner.domain.model.PlanWarning
import com.jgv.workoutplanner.domain.model.WorkoutDayFocus
import com.jgv.workoutplanner.feature.plan.PlannedExerciseUiModel
import com.jgv.workoutplanner.feature.plan.PlanScreen
import com.jgv.workoutplanner.feature.plan.PlanUiState
import com.jgv.workoutplanner.feature.plan.WorkoutDayUiModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Plan screenshot cases: empty, complete, partial, outdated, generating. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(
    sdk = [35],
    qualifiers = LIGHT_QUALIFIERS,
)
class PlanScreenshotTest {

    @get:Rule
    val roborazziRule = screenshotRule()

    @get:Rule
    val composeRule = ScrollableScreenshotRule()

    @Test
    fun `should show generate action with no saved plan in light theme`() {
        captureScreenshot("plan-not-generated-light-1_0") {
            PlanScreen(
                state = PlanUiState(isLoading = false, hasNoPlan = true),
                showRegenerateConfirm = false,
                pendingRemoval = null,
                snackbarHostState = SnackbarHostState(),
                onEvent = {},
            )
        }
    }

    @Test
    @Config(qualifiers = DARK_QUALIFIERS)
    fun `should show generate action with no saved plan in dark theme`() {
        captureScreenshot("plan-not-generated-dark-1_0") {
            PlanScreen(
                state = PlanUiState(isLoading = false, hasNoPlan = true),
                showRegenerateConfirm = false,
                pendingRemoval = null,
                snackbarHostState = SnackbarHostState(),
                onEvent = {},
            )
        }
    }

    @Test
    fun `should show generate action with no saved plan at large text`() {
        captureScreenshot(
            fileName = "plan-not-generated-light-1_3",
            fontScale = 1.3f,
        ) {
            PlanScreen(
                state = PlanUiState(isLoading = false, hasNoPlan = true),
                showRegenerateConfirm = false,
                pendingRemoval = null,
                snackbarHostState = SnackbarHostState(),
                onEvent = {},
            )
        }
    }

    @Test
    fun `should show four days with prescriptions in light theme`() {
        planCase(
            baseName = "plan-complete",
            variant = "light-1_0",
            state = completePlan,
        )
    }

    @Test
    @Config(qualifiers = DARK_QUALIFIERS)
    fun `should show four days with prescriptions in dark theme`() {
        planCase(
            baseName = "plan-complete",
            variant = "dark-1_0",
            state = completePlan,
        )
    }

    @Test
    fun `should show four days with prescriptions at large text`() {
        planCase(
            baseName = "plan-complete",
            variant = "light-1_3",
            fontScale = 1.3f,
            state = completePlan,
        )
    }

    @Test
    fun `should show four days with prescriptions at largest text`() {
        planCase(
            baseName = "plan-complete",
            variant = "light-2_0",
            fontScale = 2f,
            state = completePlan,
        )
    }

    @Test
    @Config(qualifiers = RTL_QUALIFIERS)
    fun `should show four days with prescriptions in mirrored layout`() {
        planCase(
            baseName = "plan-complete",
            variant = "light-1_0-rtl",
            state = completePlan,
        )
    }

    @Test
    fun `should show no-match warnings above retained days in light theme`() {
        planCase(
            baseName = "plan-partial",
            variant = "light-1_0",
            state = partialPlan,
        )
    }

    @Test
    @Config(qualifiers = DARK_QUALIFIERS)
    fun `should show no-match warnings above retained days in dark theme`() {
        planCase(
            baseName = "plan-partial",
            variant = "dark-1_0",
            state = partialPlan,
        )
    }

    @Test
    fun `should show no-match warnings above retained days at large text`() {
        planCase(
            baseName = "plan-partial",
            variant = "light-1_3",
            fontScale = 1.3f,
            state = partialPlan,
        )
    }

    @Test
    fun `should show no-match warnings above retained days at largest text`() {
        planCase(
            baseName = "plan-partial",
            variant = "light-2_0",
            fontScale = 2f,
            state = partialPlan,
        )
    }

    @Test
    fun `should show singular add label with one slot left in light theme`() {
        planCase(
            baseName = "plan-add-one-slot",
            variant = "light-1_0",
            state = PlanUiState(
                isLoading = false,
                planName = "UPPER_LOWER - 4 days",
                days = listOf(
                    WorkoutDayUiModel(
                        id = "day-upper-1",
                        name = "Upper 1",
                        focus = WorkoutDayFocus.UPPER_BODY,
                        exercises = listOf(
                            planned(ExerciseId.MACHINE_CHEST_PRESS, 3, 8..12, 90, 0),
                            planned(ExerciseId.LAT_PULLDOWN, 3, 8..12, 90, 1),
                        ),
                        remainingCapacity = 1,
                    ),
                ),
            ),
        )
    }

    @Test
    @Config(qualifiers = DARK_QUALIFIERS)
    fun `should show singular add label with one slot left in dark theme`() {
        planCase(
            baseName = "plan-add-one-slot",
            variant = "dark-1_0",
            state = PlanUiState(
                isLoading = false,
                planName = "UPPER_LOWER - 4 days",
                days = listOf(
                    WorkoutDayUiModel(
                        id = "day-upper-1",
                        name = "Upper 1",
                        focus = WorkoutDayFocus.UPPER_BODY,
                        exercises = listOf(
                            planned(ExerciseId.MACHINE_CHEST_PRESS, 3, 8..12, 90, 0),
                            planned(ExerciseId.LAT_PULLDOWN, 3, 8..12, 90, 1),
                        ),
                        remainingCapacity = 1,
                    ),
                ),
            ),
        )
    }

    @Test
    fun `should show singular add label with one slot left at large text`() {
        planCase(
            baseName = "plan-add-one-slot",
            variant = "light-1_3",
            fontScale = 1.3f,
            state = PlanUiState(
                isLoading = false,
                planName = "UPPER_LOWER - 4 days",
                days = listOf(
                    WorkoutDayUiModel(
                        id = "day-upper-1",
                        name = "Upper 1",
                        focus = WorkoutDayFocus.UPPER_BODY,
                        exercises = listOf(
                            planned(ExerciseId.MACHINE_CHEST_PRESS, 3, 8..12, 90, 0),
                            planned(ExerciseId.LAT_PULLDOWN, 3, 8..12, 90, 1),
                        ),
                        remainingCapacity = 1,
                    ),
                ),
            ),
        )
    }

    @Test
    fun `should show emptied day with add row in light theme`() {
        planCase(
            baseName = "plan-empty-day",
            variant = "light-1_0",
            state = PlanUiState(
                isLoading = false,
                planName = "UPPER_LOWER - 4 days",
                days = listOf(
                    WorkoutDayUiModel(
                        id = "day-upper-1",
                        name = "Upper 1",
                        focus = WorkoutDayFocus.UPPER_BODY,
                        exercises = emptyList(),
                        remainingCapacity = 4,
                    ),
                ),
            ),
        )
    }

    @Test
    @Config(qualifiers = DARK_QUALIFIERS)
    fun `should show emptied day with add row in dark theme`() {
        planCase(
            baseName = "plan-empty-day",
            variant = "dark-1_0",
            state = PlanUiState(
                isLoading = false,
                planName = "UPPER_LOWER - 4 days",
                days = listOf(
                    WorkoutDayUiModel(
                        id = "day-upper-1",
                        name = "Upper 1",
                        focus = WorkoutDayFocus.UPPER_BODY,
                        exercises = emptyList(),
                        remainingCapacity = 4,
                    ),
                ),
            ),
        )
    }

    @Test
    fun `should hide menus rows and warnings when outdated in light theme`() {
        planCase(
            baseName = "plan-outdated",
            variant = "light-1_0",
            state = completePlan.copy(
                requiresRegeneration = true,
                warnings = listOf(PlanWarning(dayIndex = 0, dayFocus = WorkoutDayFocus.UPPER_BODY, slotId = "slot-0")),
            ),
        )
    }

    @Test
    @Config(qualifiers = DARK_QUALIFIERS)
    fun `should hide menus rows and warnings when outdated in dark theme`() {
        planCase(
            baseName = "plan-outdated",
            variant = "dark-1_0",
            state = completePlan.copy(
                requiresRegeneration = true,
                warnings = listOf(PlanWarning(dayIndex = 0, dayFocus = WorkoutDayFocus.UPPER_BODY, slotId = "slot-0")),
            ),
        )
    }

    @Test
    fun `should hide menus rows and warnings when outdated at large text`() {
        planCase(
            baseName = "plan-outdated",
            variant = "light-1_3",
            fontScale = 1.3f,
            state = completePlan.copy(
                requiresRegeneration = true,
                warnings = listOf(PlanWarning(dayIndex = 0, dayFocus = WorkoutDayFocus.UPPER_BODY, slotId = "slot-0")),
            ),
        )
    }

    @Test
    fun `should show generating message while regenerating in light theme`() {
        captureScreenshot("plan-generating-light-1_0") {
            PlanScreen(
                state = PlanUiState(isLoading = false, isGenerating = true),
                showRegenerateConfirm = false,
                pendingRemoval = null,
                snackbarHostState = SnackbarHostState(),
                onEvent = {},
            )
        }
    }

    @Test
    @Config(qualifiers = DARK_QUALIFIERS)
    fun `should show generating message while regenerating in dark theme`() {
        captureScreenshot("plan-generating-dark-1_0") {
            PlanScreen(
                state = PlanUiState(isLoading = false, isGenerating = true),
                showRegenerateConfirm = false,
                pendingRemoval = null,
                snackbarHostState = SnackbarHostState(),
                onEvent = {},
            )
        }
    }

    /**
     * Scrollable plan capture with a fresh list state per case: reusing one
     * across tests would carry the scroll position over.
     */
    private fun planCase(
        baseName: String,
        variant: String,
        state: PlanUiState,
        fontScale: Float = 1f,
    ) {
        val listState = LazyListState()
        composeRule.captureScrollable(
            baseName = baseName,
            variant = variant,
            fontScale = fontScale,
            scrollTag = TestTags.PLAN_CONTENT,
            listState = listState,
        ) {
            PlanScreen(
                state = state,
                showRegenerateConfirm = false,
                pendingRemoval = null,
                snackbarHostState = SnackbarHostState(),
                onEvent = {},
                listState = listState,
            )
        }
    }

    private companion object {
        fun planned(
            id: ExerciseId,
            sets: Int,
            reps: IntRange,
            restSeconds: Int,
            order: Int,
        ): PlannedExerciseUiModel = PlannedExerciseUiModel(
            exerciseId = id,
            definition = ExerciseCatalog.exercises.first { it.id == id },
            sets = sets,
            repRange = reps,
            restSeconds = restSeconds,
            order = order,
        )

        val completePlan = PlanUiState(
            isLoading = false,
            planName = "UPPER_LOWER - 4 days",
            days = listOf(
                WorkoutDayUiModel(
                    id = "day-upper-1",
                    name = "Upper 1",
                    focus = WorkoutDayFocus.UPPER_BODY,
                    exercises = listOf(
                        planned(ExerciseId.MACHINE_CHEST_PRESS, 3, 8..12, 90, 0),
                        planned(ExerciseId.LAT_PULLDOWN, 3, 8..12, 90, 1),
                        planned(ExerciseId.DUMBBELL_LATERAL_RAISE, 3, 12..15, 60, 2),
                    ),
                    remainingCapacity = 1,
                ),
                WorkoutDayUiModel(
                    id = "day-lower-1",
                    name = "Lower 1",
                    focus = WorkoutDayFocus.LOWER_BODY,
                    exercises = listOf(
                        planned(ExerciseId.BODYWEIGHT_SQUAT, 3, 12..15, 90, 0),
                        planned(ExerciseId.DUMBBELL_ROMANIAN_DEADLIFT, 3, 8..12, 90, 1),
                    ),
                    remainingCapacity = 2,
                ),
                WorkoutDayUiModel(
                    id = "day-upper-2",
                    name = "Upper 2",
                    focus = WorkoutDayFocus.UPPER_BODY,
                    exercises = listOf(
                        planned(ExerciseId.BARBELL_BENCH_PRESS, 4, 6..8, 120, 0),
                        planned(ExerciseId.BARBELL_ROW, 4, 6..8, 120, 1),
                        planned(ExerciseId.FACE_PULL, 3, 12..15, 60, 2),
                        planned(ExerciseId.DUMBBELL_CURL, 3, 10..12, 60, 3),
                    ),
                    remainingCapacity = 0,
                ),
                WorkoutDayUiModel(
                    id = "day-lower-2",
                    name = "Lower 2",
                    focus = WorkoutDayFocus.LOWER_BODY,
                    exercises = listOf(
                        planned(ExerciseId.BULGARIAN_SPLIT_SQUAT, 3, 8..12, 90, 0),
                        planned(ExerciseId.CABLE_PULL_THROUGH, 3, 10..12, 60, 1),
                    ),
                    remainingCapacity = 2,
                ),
            ),
        )

        val partialPlan = PlanUiState(
            isLoading = false,
            planName = "PUSH_PULL_LEGS - 3 days",
            days = listOf(
                WorkoutDayUiModel(
                    id = "day-upper-1",
                    name = "Upper 1",
                    focus = WorkoutDayFocus.UPPER_BODY,
                    exercises = listOf(
                        planned(ExerciseId.MACHINE_CHEST_PRESS, 3, 8..12, 90, 0),
                        planned(ExerciseId.LAT_PULLDOWN, 3, 8..12, 90, 1),
                    ),
                    remainingCapacity = 1,
                ),
                WorkoutDayUiModel(
                    id = "day-lower-1",
                    name = "Lower 1",
                    focus = WorkoutDayFocus.LOWER_BODY,
                    exercises = listOf(
                        planned(ExerciseId.BODYWEIGHT_SQUAT, 3, 12..15, 90, 0),
                    ),
                    remainingCapacity = 2,
                ),
            ),
            warnings = listOf(
                PlanWarning(dayIndex = 1, dayFocus = WorkoutDayFocus.PULL, slotId = "slot-pull-2"),
                PlanWarning(dayIndex = 2, dayFocus = WorkoutDayFocus.LEGS, slotId = "slot-legs-1"),
            ),
        )
    }
}
