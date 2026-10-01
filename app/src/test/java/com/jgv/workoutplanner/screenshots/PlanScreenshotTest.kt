package com.jgv.workoutplanner.screenshots

import android.content.Context
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.SnackbarHostState
import androidx.test.core.app.ApplicationProvider
import com.jgv.workoutplanner.R
import com.jgv.workoutplanner.core.ui.TestTags
import com.jgv.workoutplanner.data.catalog.ExerciseCatalog
import com.jgv.workoutplanner.domain.model.ExerciseDefinition
import com.jgv.workoutplanner.domain.model.ExerciseId
import com.jgv.workoutplanner.domain.model.PlanWarning
import com.jgv.workoutplanner.domain.model.WorkoutDayFocus
import com.jgv.workoutplanner.feature.plan.PendingExerciseRemoval
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
        composeRule.captureScreenshot("plan-not-generated-light-1_0") {
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
        composeRule.captureScreenshot("plan-not-generated-dark-1_0") {
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
        composeRule.captureScreenshot(
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
    fun `should show all actions for a middle exercise in light theme`() {
        menuCase(
            fileName = "plan-menu-middle-row-light-1_0",
            state = completePlan,
            exerciseId = ExerciseId.LAT_PULLDOWN,
        )
    }

    @Test
    @Config(qualifiers = DARK_QUALIFIERS)
    fun `should show all actions for a middle exercise in dark theme`() {
        menuCase(
            fileName = "plan-menu-middle-row-dark-1_0",
            state = completePlan,
            exerciseId = ExerciseId.LAT_PULLDOWN,
        )
    }

    @Test
    fun `should show all actions for a middle exercise at large text`() {
        menuCase(
            fileName = "plan-menu-middle-row-light-1_3",
            fontScale = 1.3f,
            state = completePlan,
            exerciseId = ExerciseId.LAT_PULLDOWN,
        )
    }

    @Test
    fun `should disable move up for the first exercise in light theme`() {
        menuCase(
            fileName = "plan-menu-first-row-light-1_0",
            state = completePlan,
            exerciseId = ExerciseId.MACHINE_CHEST_PRESS,
            disabledItems = setOf(R.string.plan_action_move_up),
        )
    }

    @Test
    @Config(qualifiers = DARK_QUALIFIERS)
    fun `should disable move up for the first exercise in dark theme`() {
        menuCase(
            fileName = "plan-menu-first-row-dark-1_0",
            state = completePlan,
            exerciseId = ExerciseId.MACHINE_CHEST_PRESS,
            disabledItems = setOf(R.string.plan_action_move_up),
        )
    }

    @Test
    fun `should disable move down for the last exercise in light theme`() {
        menuCase(
            fileName = "plan-menu-last-row-light-1_0",
            state = completePlan,
            exerciseId = ExerciseId.DUMBBELL_LATERAL_RAISE,
            disabledItems = setOf(R.string.plan_action_move_down),
        )
    }

    @Test
    @Config(qualifiers = DARK_QUALIFIERS)
    fun `should disable move down for the last exercise in dark theme`() {
        menuCase(
            fileName = "plan-menu-last-row-dark-1_0",
            state = completePlan,
            exerciseId = ExerciseId.DUMBBELL_LATERAL_RAISE,
            disabledItems = setOf(R.string.plan_action_move_down),
        )
    }

    @Test
    fun `should disable both move actions for an only exercise in light theme`() {
        menuCase(
            fileName = "plan-menu-only-row-light-1_0",
            state = onlyExercisePlan,
            exerciseId = ExerciseId.MACHINE_CHEST_PRESS,
            disabledItems = setOf(
                R.string.plan_action_move_up,
                R.string.plan_action_move_down,
            ),
        )
    }

    @Test
    @Config(qualifiers = DARK_QUALIFIERS)
    fun `should disable both move actions for an only exercise in dark theme`() {
        menuCase(
            fileName = "plan-menu-only-row-dark-1_0",
            state = onlyExercisePlan,
            exerciseId = ExerciseId.MACHINE_CHEST_PRESS,
            disabledItems = setOf(
                R.string.plan_action_move_up,
                R.string.plan_action_move_down,
            ),
        )
    }

    @Test
    fun `should warn that manual edits will be lost in regeneration dialog in light theme`() {
        composeRule.captureDialogScreenshot(
            fileName = "plan-regenerate-dialog-light-1_0",
            dialogTitle = regenerateDialogTitle(),
        ) {
            PlanScreen(
                state = completePlan,
                showRegenerateConfirm = true,
                pendingRemoval = null,
                snackbarHostState = SnackbarHostState(),
                onEvent = {},
            )
        }
    }

    @Test
    @Config(qualifiers = DARK_QUALIFIERS)
    fun `should warn that manual edits will be lost in regeneration dialog in dark theme`() {
        composeRule.captureDialogScreenshot(
            fileName = "plan-regenerate-dialog-dark-1_0",
            dialogTitle = regenerateDialogTitle(),
        ) {
            PlanScreen(
                state = completePlan,
                showRegenerateConfirm = true,
                pendingRemoval = null,
                snackbarHostState = SnackbarHostState(),
                onEvent = {},
            )
        }
    }

    @Test
    fun `should warn that manual edits will be lost in regeneration dialog at large text`() {
        composeRule.captureDialogScreenshot(
            fileName = "plan-regenerate-dialog-light-1_3",
            fontScale = 1.3f,
            dialogTitle = regenerateDialogTitle(),
        ) {
            PlanScreen(
                state = completePlan,
                showRegenerateConfirm = true,
                pendingRemoval = null,
                snackbarHostState = SnackbarHostState(),
                onEvent = {},
            )
        }
    }

    @Test
    fun `should warn that manual edits will be lost in regeneration dialog at largest text`() {
        composeRule.captureDialogScreenshot(
            fileName = "plan-regenerate-dialog-light-2_0",
            fontScale = 2f,
            dialogTitle = regenerateDialogTitle(),
        ) {
            PlanScreen(
                state = completePlan,
                showRegenerateConfirm = true,
                pendingRemoval = null,
                snackbarHostState = SnackbarHostState(),
                onEvent = {},
            )
        }
    }

    @Test
    fun `should confirm removal over a populated plan in light theme`() {
        composeRule.captureDialogScreenshot(
            fileName = "plan-remove-dialog-light-1_0",
            dialogTitle = removeDialogTitle(),
        ) {
            PlanScreen(
                state = completePlan,
                showRegenerateConfirm = false,
                pendingRemoval = PendingExerciseRemoval(
                    dayId = "day-upper-1",
                    exerciseId = ExerciseId.MACHINE_CHEST_PRESS,
                ),
                snackbarHostState = SnackbarHostState(),
                onEvent = {},
            )
        }
    }

    @Test
    @Config(qualifiers = DARK_QUALIFIERS)
    fun `should confirm removal over a populated plan in dark theme`() {
        composeRule.captureDialogScreenshot(
            fileName = "plan-remove-dialog-dark-1_0",
            dialogTitle = removeDialogTitle(),
        ) {
            PlanScreen(
                state = completePlan,
                showRegenerateConfirm = false,
                pendingRemoval = PendingExerciseRemoval(
                    dayId = "day-upper-1",
                    exerciseId = ExerciseId.MACHINE_CHEST_PRESS,
                ),
                snackbarHostState = SnackbarHostState(),
                onEvent = {},
            )
        }
    }

    @Test
    fun `should confirm removal over a populated plan at large text`() {
        composeRule.captureDialogScreenshot(
            fileName = "plan-remove-dialog-light-1_3",
            fontScale = 1.3f,
            dialogTitle = removeDialogTitle(),
        ) {
            PlanScreen(
                state = completePlan,
                showRegenerateConfirm = false,
                pendingRemoval = PendingExerciseRemoval(
                    dayId = "day-upper-1",
                    exerciseId = ExerciseId.MACHINE_CHEST_PRESS,
                ),
                snackbarHostState = SnackbarHostState(),
                onEvent = {},
            )
        }
    }

    @Test
    fun `should show plural add label with two slots left in light theme`() {
        planCase(
            baseName = "plan-add-multiple-slots",
            variant = "light-1_0",
            state = multiSlotPlan,
        )
    }

    @Test
    @Config(qualifiers = DARK_QUALIFIERS)
    fun `should show plural add label with two slots left in dark theme`() {
        planCase(
            baseName = "plan-add-multiple-slots",
            variant = "dark-1_0",
            state = multiSlotPlan,
        )
    }

    @Test
    fun `should show plural add label with two slots left at large text`() {
        planCase(
            baseName = "plan-add-multiple-slots",
            variant = "light-1_3",
            fontScale = 1.3f,
            state = multiSlotPlan,
        )
    }

    @Test
    fun `should show ineligible edit failure over a populated plan in light theme`() {
        planSnackbarCase(
            fileName = "plan-error-snackbar-light-1_0",
            state = completePlan,
            messageRes = R.string.plan_edit_failed_ineligible,
        )
    }

    @Test
    @Config(qualifiers = DARK_QUALIFIERS)
    fun `should show ineligible edit failure over a populated plan in dark theme`() {
        planSnackbarCase(
            fileName = "plan-error-snackbar-dark-1_0",
            state = completePlan,
            messageRes = R.string.plan_edit_failed_ineligible,
        )
    }

    @Test
    fun `should show ineligible edit failure over a populated plan at large text`() {
        planSnackbarCase(
            fileName = "plan-error-snackbar-light-1_3",
            state = completePlan,
            messageRes = R.string.plan_edit_failed_ineligible,
            fontScale = 1.3f,
        )
    }

    @Test
    fun `should show ineligible edit failure over a populated plan at largest text`() {
        planSnackbarCase(
            fileName = "plan-error-snackbar-light-2_0",
            state = completePlan,
            messageRes = R.string.plan_edit_failed_ineligible,
            fontScale = 2f,
        )
    }

    @Test
    fun `should show generation failure over the empty plan body in light theme`() {
        planSnackbarCase(
            fileName = "plan-generation-error-snackbar-light-1_0",
            state = PlanUiState(isLoading = false, hasNoPlan = true),
            messageRes = R.string.plan_generation_failed,
        )
    }

    @Test
    @Config(qualifiers = DARK_QUALIFIERS)
    fun `should show generation failure over the empty plan body in dark theme`() {
        planSnackbarCase(
            fileName = "plan-generation-error-snackbar-dark-1_0",
            state = PlanUiState(isLoading = false, hasNoPlan = true),
            messageRes = R.string.plan_generation_failed,
        )
    }

    @Test
    fun `should show generation failure over the empty plan body at large text`() {
        planSnackbarCase(
            fileName = "plan-generation-error-snackbar-light-1_3",
            state = PlanUiState(isLoading = false, hasNoPlan = true),
            messageRes = R.string.plan_generation_failed,
            fontScale = 1.3f,
        )
    }

    @Test
    fun `should show generation failure over the empty plan body at largest text`() {
        planSnackbarCase(
            fileName = "plan-generation-error-snackbar-light-2_0",
            state = PlanUiState(isLoading = false, hasNoPlan = true),
            messageRes = R.string.plan_generation_failed,
            fontScale = 2f,
        )
    }

    @Test
    fun `should show generating message while regenerating in light theme`() {
        composeRule.captureScreenshot(
            fileName = "plan-generating-light-1_0",
            frameTimeMillis = LOADING_FRAME_TIME_MILLIS,
        ) {
            PlanScreen(
                state = PlanUiState(isLoading = false, isGenerating = true),
                showRegenerateConfirm = false,
                pendingRemoval = null,
                snackbarHostState = SnackbarHostState(),
                onEvent = {},
            )
        }
        composeRule.assertIndeterminateProgressDisplayed()
    }

    @Test
    @Config(qualifiers = DARK_QUALIFIERS)
    fun `should show generating message while regenerating in dark theme`() {
        composeRule.captureScreenshot(
            fileName = "plan-generating-dark-1_0",
            frameTimeMillis = LOADING_FRAME_TIME_MILLIS,
        ) {
            PlanScreen(
                state = PlanUiState(isLoading = false, isGenerating = true),
                showRegenerateConfirm = false,
                pendingRemoval = null,
                snackbarHostState = SnackbarHostState(),
                onEvent = {},
            )
        }
        composeRule.assertIndeterminateProgressDisplayed()
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

    /**
     * Single-frame plan capture with a hoisted snackbar held open showing the
     * longest message for its host layout.
     */
    private fun planSnackbarCase(
        fileName: String,
        state: PlanUiState,
        messageRes: Int,
        fontScale: Float = 1f,
    ) {
        val message = ApplicationProvider.getApplicationContext<Context>()
            .getString(messageRes)
        composeRule.captureSnackbarScreenshot(
            fileName = fileName,
            fontScale = fontScale,
            snackbarMessage = message,
        ) { snackbarHostState ->
            PlanScreen(
                state = state,
                showRegenerateConfirm = false,
                pendingRemoval = null,
                snackbarHostState = snackbarHostState,
                onEvent = {},
            )
        }
    }

    private fun menuCase(
        fileName: String,
        state: PlanUiState,
        exerciseId: ExerciseId,
        fontScale: Float = 1f,
        disabledItems: Set<Int> = emptySet(),
    ) {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val actionResources = listOf(
            R.string.plan_action_details,
            R.string.action_replace_exercise_short,
            R.string.plan_action_move_up,
            R.string.plan_action_move_down,
            R.string.plan_action_remove,
        )
        composeRule.captureMenuScreenshot(
            fileName = fileName,
            fontScale = fontScale,
            menuButtonDescription = context.getString(
                R.string.plan_more_actions,
                context.getString(
                    ExerciseCatalog.exercises.first { it.id == exerciseId }.nameRes,
                ),
            ),
            enabledItems = actionResources
                .filterNot { it in disabledItems }
                .map(context::getString),
            disabledItems = actionResources
                .filter { it in disabledItems }
                .map(context::getString),
        ) {
            PlanScreen(
                state = state,
                showRegenerateConfirm = false,
                pendingRemoval = null,
                snackbarHostState = SnackbarHostState(),
                onEvent = {},
            )
        }
    }

    private companion object {
        /** Regeneration dialog title, resolved from resources. */
        fun regenerateDialogTitle(): String =
            ApplicationProvider.getApplicationContext<Context>()
                .getString(R.string.plan_regenerate_confirm_title)

        /** Removal dialog title, resolved from resources. */
        fun removeDialogTitle(): String =
            ApplicationProvider.getApplicationContext<Context>()
                .getString(R.string.plan_remove_confirm_title)

        /** Single editable day with two remaining slots, exercising the plural add label. */
        val multiSlotPlan = PlanUiState(
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
                    remainingCapacity = 2,
                ),
            ),
        )

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

        val onlyExercisePlan = PlanUiState(
            isLoading = false,
            planName = "FULL_BODY - 1 day",
            days = listOf(
                WorkoutDayUiModel(
                    id = "day-full-body",
                    name = "Full body",
                    focus = WorkoutDayFocus.FULL_BODY,
                    exercises = listOf(
                        planned(ExerciseId.MACHINE_CHEST_PRESS, 3, 8..12, 90, 0),
                    ),
                    remainingCapacity = 3,
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
