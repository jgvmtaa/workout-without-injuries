package com.jgv.workoutplanner.screenshots

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
}
