package com.jgv.workoutplanner.screenshots

import androidx.compose.ui.test.junit4.createComposeRule
import com.jgv.workoutplanner.core.ui.ONBOARDING_CONTENT_TAG
import com.jgv.workoutplanner.domain.model.ExperienceLevel
import com.jgv.workoutplanner.domain.model.TrainingGoal
import com.jgv.workoutplanner.domain.model.TrainingOptions
import com.jgv.workoutplanner.domain.model.WorkoutSplit
import com.jgv.workoutplanner.feature.onboarding.preferences.PreferencesScreen
import com.jgv.workoutplanner.feature.onboarding.preferences.PreferencesUiState
import com.jgv.workoutplanner.feature.onboarding.safety.SafetyNoticeScreen
import com.jgv.workoutplanner.feature.onboarding.safety.SafetyNoticeUiState
import com.jgv.workoutplanner.feature.onboarding.welcome.WelcomeScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Onboarding screenshot cases: welcome and safety notice. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(
    sdk = [35],
    qualifiers = LIGHT_QUALIFIERS,
)
class OnboardingScreenshotTest {

    @get:Rule
    val roborazziRule = screenshotRule()

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `should show welcome steps and actions in light theme`() {
        captureScreenshot("welcome-light-1_0") {
            WelcomeScreen(onGetStarted = {}, onReviewSafety = {})
        }
    }

    @Test
    @Config(qualifiers = DARK_QUALIFIERS)
    fun `should show welcome steps and actions in dark theme`() {
        captureScreenshot("welcome-dark-1_0") {
            WelcomeScreen(onGetStarted = {}, onReviewSafety = {})
        }
    }

    @Test
    fun `should disable accept action when safety is unacknowledged in light theme`() {
        captureScreenshot("safety-unacknowledged-light-1_0") {
            SafetyNoticeScreen(
                state = SafetyNoticeUiState(isLoading = false, isAcknowledged = false),
                onEvent = {},
            )
        }
    }

    @Test
    @Config(qualifiers = DARK_QUALIFIERS)
    fun `should disable accept action when safety is unacknowledged in dark theme`() {
        captureScreenshot("safety-unacknowledged-dark-1_0") {
            SafetyNoticeScreen(
                state = SafetyNoticeUiState(isLoading = false, isAcknowledged = false),
                onEvent = {},
            )
        }
    }

    @Test
    fun `should disable accept action when safety is unacknowledged at large text`() {
        captureScreenshot(
            fileName = "safety-unacknowledged-light-1_3",
            fontScale = 1.3f,
        ) {
            SafetyNoticeScreen(
                state = SafetyNoticeUiState(isLoading = false, isAcknowledged = false),
                onEvent = {},
            )
        }
    }

    @Test
    fun `should enable accept action when safety is acknowledged in light theme`() {
        captureScreenshot("safety-acknowledged-light-1_0") {
            SafetyNoticeScreen(
                state = SafetyNoticeUiState(isLoading = false, isAcknowledged = true),
                onEvent = {},
            )
        }
    }

    @Test
    @Config(qualifiers = DARK_QUALIFIERS)
    fun `should enable accept action when safety is acknowledged in dark theme`() {
        captureScreenshot("safety-acknowledged-dark-1_0") {
            SafetyNoticeScreen(
                state = SafetyNoticeUiState(isLoading = false, isAcknowledged = true),
                onEvent = {},
            )
        }
    }

    @Test
    fun `should keep continue disabled when preferences are incomplete in light theme`() {
        composeRule.captureScrollable(
            baseName = "preferences-incomplete",
            variant = "light-1_0",
            scrollTag = ONBOARDING_CONTENT_TAG,
        ) {
            PreferencesScreen(
                state = PreferencesUiState(isLoading = false),
                onEvent = {},
            )
        }
    }

    @Test
    @Config(qualifiers = DARK_QUALIFIERS)
    fun `should keep continue disabled when preferences are incomplete in dark theme`() {
        composeRule.captureScrollable(
            baseName = "preferences-incomplete",
            variant = "dark-1_0",
            scrollTag = ONBOARDING_CONTENT_TAG,
        ) {
            PreferencesScreen(
                state = PreferencesUiState(isLoading = false),
                onEvent = {},
            )
        }
    }

    @Test
    fun `should keep continue disabled when preferences are incomplete at large text`() {
        composeRule.captureScrollable(
            baseName = "preferences-incomplete",
            variant = "light-1_3",
            fontScale = 1.3f,
            scrollTag = ONBOARDING_CONTENT_TAG,
        ) {
            PreferencesScreen(
                state = PreferencesUiState(isLoading = false),
                onEvent = {},
            )
        }
    }

    @Test
    fun `should keep continue disabled when preferences are incomplete at largest text`() {
        composeRule.captureScrollable(
            baseName = "preferences-incomplete",
            variant = "light-2_0",
            fontScale = 2f,
            scrollTag = ONBOARDING_CONTENT_TAG,
        ) {
            PreferencesScreen(
                state = PreferencesUiState(isLoading = false),
                onEvent = {},
            )
        }
    }

    @Test
    fun `should enable continue with derived split when preferences are complete in light theme`() {
        composeRule.captureScrollable(
            baseName = "preferences-complete",
            variant = "light-1_0",
            scrollTag = ONBOARDING_CONTENT_TAG,
        ) {
            PreferencesScreen(
                state = completePreferences,
                onEvent = {},
            )
        }
    }

    @Test
    @Config(qualifiers = DARK_QUALIFIERS)
    fun `should enable continue with derived split when preferences are complete in dark theme`() {
        composeRule.captureScrollable(
            baseName = "preferences-complete",
            variant = "dark-1_0",
            scrollTag = ONBOARDING_CONTENT_TAG,
        ) {
            PreferencesScreen(
                state = completePreferences,
                onEvent = {},
            )
        }
    }

    @Test
    fun `should enable continue with derived split when preferences are complete at large text`() {
        composeRule.captureScrollable(
            baseName = "preferences-complete",
            variant = "light-1_3",
            fontScale = 1.3f,
            scrollTag = ONBOARDING_CONTENT_TAG,
        ) {
            PreferencesScreen(
                state = completePreferences,
                onEvent = {},
            )
        }
    }

    @Test
    fun `should enable continue with derived split when preferences are complete at largest text`() {
        composeRule.captureScrollable(
            baseName = "preferences-complete",
            variant = "light-2_0",
            fontScale = 2f,
            scrollTag = ONBOARDING_CONTENT_TAG,
        ) {
            PreferencesScreen(
                state = completePreferences,
                onEvent = {},
            )
        }
    }

    @Test
    @Config(qualifiers = RTL_QUALIFIERS)
    fun `should enable continue with derived split when preferences are complete in mirrored layout`() {
        composeRule.captureScrollable(
            baseName = "preferences-complete",
            variant = "light-1_0-rtl",
            scrollTag = ONBOARDING_CONTENT_TAG,
        ) {
            PreferencesScreen(
                state = completePreferences,
                onEvent = {},
            )
        }
    }

    private companion object {
        // Representative maximum: every selectable equipment checked, so all
        // row treatments render; the split derives from the 3-day frequency.
        val completePreferences = PreferencesUiState(
            isLoading = false,
            goal = TrainingGoal.BUILD_MUSCLE,
            experienceLevel = ExperienceLevel.INTERMEDIATE,
            daysPerWeek = 3,
            sessionDurationMinutes = 60,
            selectedEquipment = TrainingOptions.SELECTABLE_EQUIPMENT.toSet(),
            derivedSplit = WorkoutSplit.PUSH_PULL_LEGS,
        )
    }
}
