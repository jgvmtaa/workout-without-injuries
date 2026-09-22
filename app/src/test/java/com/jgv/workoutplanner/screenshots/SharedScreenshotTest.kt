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

    @Test
    fun `should show loading indicator in light theme`() {
        captureScreenshot("loading-light-1_0") {
            LoadingContent()
        }
    }

    @Test
    @Config(qualifiers = DARK_QUALIFIERS)
    fun `should show loading indicator in dark theme`() {
        captureScreenshot("loading-dark-1_0") {
            LoadingContent()
        }
    }
}
