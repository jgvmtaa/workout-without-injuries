package com.jgv.workoutplanner.screenshots

import com.jgv.workoutplanner.core.ui.LoadingContent
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Shared and transient visual states: the generic loading component. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(
    sdk = [35],
    qualifiers = LIGHT_QUALIFIERS,
)
class SharedScreenshotTest {

    @get:Rule
    val roborazziRule = screenshotRule()

    @get:Rule
    val composeRule = ScrollableScreenshotRule()

    @Test
    fun `should show loading indicator in light theme`() {
        composeRule.captureScreenshot(
            fileName = "loading-light-1_0",
            frameTimeMillis = LOADING_FRAME_TIME_MILLIS,
        ) {
            LoadingContent()
        }
        composeRule.assertIndeterminateProgressDisplayed()
    }

    @Test
    @Config(qualifiers = DARK_QUALIFIERS)
    fun `should show loading indicator in dark theme`() {
        composeRule.captureScreenshot(
            fileName = "loading-dark-1_0",
            frameTimeMillis = LOADING_FRAME_TIME_MILLIS,
        ) {
            LoadingContent()
        }
        composeRule.assertIndeterminateProgressDisplayed()
    }
}
