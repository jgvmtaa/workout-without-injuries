package com.jgv.workoutplanner.screenshots

import com.jgv.workoutplanner.domain.model.ExperienceLevel
import com.jgv.workoutplanner.domain.model.TrainingGoal
import com.jgv.workoutplanner.domain.model.WorkoutSplit
import com.jgv.workoutplanner.core.ui.TestTags
import com.jgv.workoutplanner.feature.home.HomeScreen
import com.jgv.workoutplanner.feature.home.HomeUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Home screenshot cases: no plan, current plan, outdated plan, no profile. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(
    sdk = [35],
    qualifiers = LIGHT_QUALIFIERS,
)
class HomeScreenshotTest {

    @get:Rule
    val roborazziRule = screenshotRule()

    @get:Rule
    val composeRule = ScrollableScreenshotRule()

    @Test
    fun `should show generate action with no saved plan in light theme`() {
        composeRule.captureScrollable(
            baseName = "home-no-plan",
            variant = "light-1_0",
            scrollTag = TestTags.HOME_CONTENT,
        ) {
            HomeScreen(
                state = HomeUiState(
                    isLoading = false,
                    daysPerWeek = 3,
                    goal = TrainingGoal.GENERAL_FITNESS,
                    experienceLevel = ExperienceLevel.BEGINNER,
                    split = WorkoutSplit.PUSH_PULL_LEGS,
                    limitationsCount = 0,
                    planName = null,
                ),
                onOpenPlan = {},
                onOpenExerciseLibrary = {},
                onOpenProfile = {},
            )
        }
    }

    @Test
    @Config(qualifiers = DARK_QUALIFIERS)
    fun `should show generate action with no saved plan in dark theme`() {
        composeRule.captureScrollable(
            baseName = "home-no-plan",
            variant = "dark-1_0",
            scrollTag = TestTags.HOME_CONTENT,
        ) {
            HomeScreen(
                state = HomeUiState(
                    isLoading = false,
                    daysPerWeek = 3,
                    goal = TrainingGoal.GENERAL_FITNESS,
                    experienceLevel = ExperienceLevel.BEGINNER,
                    split = WorkoutSplit.PUSH_PULL_LEGS,
                    limitationsCount = 0,
                    planName = null,
                ),
                onOpenPlan = {},
                onOpenExerciseLibrary = {},
                onOpenProfile = {},
            )
        }
    }

    @Test
    fun `should show generate action with no saved plan at large text`() {
        composeRule.captureScrollable(
            baseName = "home-no-plan",
            variant = "light-1_3",
            fontScale = 1.3f,
            scrollTag = TestTags.HOME_CONTENT,
        ) {
            HomeScreen(
                state = HomeUiState(
                    isLoading = false,
                    daysPerWeek = 3,
                    goal = TrainingGoal.GENERAL_FITNESS,
                    experienceLevel = ExperienceLevel.BEGINNER,
                    split = WorkoutSplit.PUSH_PULL_LEGS,
                    limitationsCount = 0,
                    planName = null,
                ),
                onOpenPlan = {},
                onOpenExerciseLibrary = {},
                onOpenProfile = {},
            )
        }
    }

    @Test
    fun `should show saved plan with view action in light theme`() {
        composeRule.captureScrollable(
            baseName = "home-current-plan",
            variant = "light-1_0",
            scrollTag = TestTags.HOME_CONTENT,
        ) {
            HomeScreen(
                state = currentPlan,
                onOpenPlan = {},
                onOpenExerciseLibrary = {},
                onOpenProfile = {},
            )
        }
    }

    @Test
    @Config(qualifiers = DARK_QUALIFIERS)
    fun `should show saved plan with view action in dark theme`() {
        composeRule.captureScrollable(
            baseName = "home-current-plan",
            variant = "dark-1_0",
            scrollTag = TestTags.HOME_CONTENT,
        ) {
            HomeScreen(
                state = currentPlan,
                onOpenPlan = {},
                onOpenExerciseLibrary = {},
                onOpenProfile = {},
            )
        }
    }

    @Test
    fun `should show saved plan with view action at large text`() {
        composeRule.captureScrollable(
            baseName = "home-current-plan",
            variant = "light-1_3",
            fontScale = 1.3f,
            scrollTag = TestTags.HOME_CONTENT,
        ) {
            HomeScreen(
                state = currentPlan,
                onOpenPlan = {},
                onOpenExerciseLibrary = {},
                onOpenProfile = {},
            )
        }
    }

    @Test
    fun `should keep plan visible with profile-changed warning in light theme`() {
        composeRule.captureScrollable(
            baseName = "home-outdated-plan",
            variant = "light-1_0",
            scrollTag = TestTags.HOME_CONTENT,
        ) {
            HomeScreen(
                state = currentPlan.copy(requiresRegeneration = true),
                onOpenPlan = {},
                onOpenExerciseLibrary = {},
                onOpenProfile = {},
            )
        }
    }

    @Test
    @Config(qualifiers = DARK_QUALIFIERS)
    fun `should keep plan visible with profile-changed warning in dark theme`() {
        composeRule.captureScrollable(
            baseName = "home-outdated-plan",
            variant = "dark-1_0",
            scrollTag = TestTags.HOME_CONTENT,
        ) {
            HomeScreen(
                state = currentPlan.copy(requiresRegeneration = true),
                onOpenPlan = {},
                onOpenExerciseLibrary = {},
                onOpenProfile = {},
            )
        }
    }

    @Test
    fun `should keep plan visible with profile-changed warning at large text`() {
        composeRule.captureScrollable(
            baseName = "home-outdated-plan",
            variant = "light-1_3",
            fontScale = 1.3f,
            scrollTag = TestTags.HOME_CONTENT,
        ) {
            HomeScreen(
                state = currentPlan.copy(requiresRegeneration = true),
                onOpenPlan = {},
                onOpenExerciseLibrary = {},
                onOpenProfile = {},
            )
        }
    }

    @Test
    fun `should show missing summary with no-plan action when profile is unavailable in light theme`() {
        composeRule.captureScrollable(
            baseName = "home-profile-unavailable",
            variant = "light-1_0",
            scrollTag = TestTags.HOME_CONTENT,
        ) {
            HomeScreen(
                state = HomeUiState(isLoading = false),
                onOpenPlan = {},
                onOpenExerciseLibrary = {},
                onOpenProfile = {},
            )
        }
    }

    @Test
    @Config(qualifiers = DARK_QUALIFIERS)
    fun `should show missing summary with no-plan action when profile is unavailable in dark theme`() {
        composeRule.captureScrollable(
            baseName = "home-profile-unavailable",
            variant = "dark-1_0",
            scrollTag = TestTags.HOME_CONTENT,
        ) {
            HomeScreen(
                state = HomeUiState(isLoading = false),
                onOpenPlan = {},
                onOpenExerciseLibrary = {},
                onOpenProfile = {},
            )
        }
    }

    private companion object {
        val currentPlan = HomeUiState(
            isLoading = false,
            daysPerWeek = 4,
            goal = TrainingGoal.BUILD_MUSCLE,
            experienceLevel = ExperienceLevel.INTERMEDIATE,
            split = WorkoutSplit.UPPER_LOWER,
            limitationsCount = 2,
            planName = "UPPER_LOWER - 4 days",
        )
    }
}
