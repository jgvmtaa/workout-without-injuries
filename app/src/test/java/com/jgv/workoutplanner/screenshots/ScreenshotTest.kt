package com.jgv.workoutplanner.screenshots

import androidx.compose.runtime.Composable
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.RoborazziRule
import com.github.takahirom.roborazzi.captureRoboImage
import com.jgv.workoutplanner.core.designsystem.AppTheme
import com.jgv.workoutplanner.core.ui.LoadingContent
import com.jgv.workoutplanner.feature.onboarding.welcome.WelcomeScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val BASELINE_DIR = "src/test/screenshots"

// tests.spec Harness: locale leads, density trails; only the night
// segment varies between the light and dark variants.
private const val LIGHT_QUALIFIERS = "en-rUS-w360dp-h800dp-notlong-port-notnight-xhdpi"
private const val DARK_QUALIFIERS = "en-rUS-w360dp-h800dp-notlong-port-night-xhdpi"

/**
 * Screenshot suite defined by [tests.spec](../../../tests.spec).
 *
 * Harness pins from the spec: Robolectric native graphics at SDK 35 (the
 * `compileSdk`), device qualifier `en-rUS-w360dp-h800dp-notlong-port-notnight-xhdpi`
 * (night segment replaced per dark variant), fixed `AppTheme` palette, and
 * `changeThreshold = 0.001`. Baselines live in `app/src/test/screenshots/` and are
 * named `<case>[-<scroll>]-<variant>.png`.
 *
 * Each case drives the stateless screen composable directly with fixture inputs —
 * never a ViewModel — and captures the complete app window.
 *
 * Recording vs verifying is owned by the Roborazzi Gradle tasks, not by plain
 * `test` (with no task type set, captures are a no-op):
 *
 * - `./gradlew recordRoborazziDebug` writes baselines into `src/test/screenshots/`.
 * - `./gradlew verifyRoborazziDebug` byte-compares against the committed baselines.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(
    sdk = [35],
    qualifiers = LIGHT_QUALIFIERS,
)
class ScreenshotTest {

    @get:Rule
    val roborazziRule = RoborazziRule(
        options = RoborazziRule.Options(
            roborazziOptions = RoborazziOptions(
                compareOptions = RoborazziOptions.CompareOptions(
                    changeThreshold = 0.001f,
                ),
            ),
        ),
    )

    @Test
    fun sc001_welcome_light() {
        captureCase("SC-001-welcome-light-1_0") {
            WelcomeScreen(onGetStarted = {}, onReviewSafety = {})
        }
    }

    @Test
    @Config(qualifiers = DARK_QUALIFIERS)
    fun sc001_welcome_dark() {
        captureCase("SC-001-welcome-dark-1_0") {
            WelcomeScreen(onGetStarted = {}, onReviewSafety = {})
        }
    }

    @Test
    fun sc042_loading_light() {
        captureCase("SC-042-loading-light-1_0") {
            LoadingContent()
        }
    }

    @Test
    @Config(qualifiers = DARK_QUALIFIERS)
    fun sc042_loading_dark() {
        captureCase("SC-042-loading-dark-1_0") {
            LoadingContent()
        }
    }

    /**
     * Renders [content] in [AppTheme] — the theme is driven by the test
     * qualifier, never by stubbing — and captures it to the spec's baseline
     * path. [fileName] is `<case>[-<scroll>]-<variant>` without extension.
     */
    private fun captureCase(
        fileName: String,
        content: @Composable () -> Unit,
    ) {
        captureRoboImage(
            filePath = "$BASELINE_DIR/$fileName.png",
        ) {
            AppTheme {
                content()
            }
        }
    }
}
