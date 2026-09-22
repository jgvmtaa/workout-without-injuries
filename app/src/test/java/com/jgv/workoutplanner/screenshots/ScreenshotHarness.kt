package com.jgv.workoutplanner.screenshots

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.RoborazziRule
import com.github.takahirom.roborazzi.captureRoboImage
import com.jgv.workoutplanner.core.designsystem.AppTheme

/**
 * Shared harness for the screenshot suite.
 *
 * Pins from the suite contract: Robolectric native graphics at SDK 35 (the
 * `compileSdk`), device qualifier `en-rUS-w360dp-h800dp-notlong-port-notnight-xhdpi`
 * (night segment replaced per dark variant), fixed `AppTheme` palette, and
 * `changeThreshold = 0.001`. Baselines live in `app/src/test/screenshots/` and are
 * named `<screen>-<state>[-<scroll>]-<variant>.png`.
 *
 * Each case drives the stateless screen composable directly with fixture inputs —
 * never a ViewModel — and captures the complete app window.
 *
 * Recording vs verifying is owned by the Roborazzi Gradle tasks, not by plain
 * `test` (with no task type set, captures are a no-op):
 *
 * - `./gradlew recordRoborazziDebug` writes baselines into `src/test/screenshots/`.
 * - `./gradlew verifyRoborazziDebug` byte-compares against the committed baselines.
 *
 * One test class per product area lives in this package; every class declares the
 * runner, graphics mode, and light qualifier, plus this rule.
 */
internal const val BASELINE_DIR = "src/test/screenshots"

// Locale leads, density trails; only the night segment varies between the
// light and dark variants.
internal const val LIGHT_QUALIFIERS = "en-rUS-w360dp-h800dp-notlong-port-notnight-xhdpi"
internal const val DARK_QUALIFIERS = "en-rUS-w360dp-h800dp-notlong-port-night-xhdpi"

/** Rule carrying the suite's comparison threshold into every capture. */
fun screenshotRule(): RoborazziRule = RoborazziRule(
    options = RoborazziRule.Options(
        roborazziOptions = RoborazziOptions(
            compareOptions = RoborazziOptions.CompareOptions(
                changeThreshold = 0.001f,
            ),
        ),
    ),
)

/**
 * Renders [content] in [AppTheme] — the theme is driven by the test
 * qualifier, never by stubbing — and captures it to the suite's baseline
 * path. [fileName] is `<screen>-<state>[-<scroll>]-<variant>` without extension.
 *
 * Large-text variants ([fontScale] other than 1.0) provide a [Density] with
 * the scaled font size. The app is fully Compose, and Compose reads text
 * scaling from [Density], so this renders the same pixels as a device
 * setting; `dp` dimensions and the viewport are untouched. Variants at the
 * default scale run the unmodified production composition.
 */
fun captureScreenshot(
    fileName: String,
    fontScale: Float = 1f,
    content: @Composable () -> Unit,
) {
    captureRoboImage(
        filePath = "$BASELINE_DIR/$fileName.png",
    ) {
        AppTheme {
            if (fontScale == 1f) {
                content()
            } else {
                val density = LocalDensity.current
                CompositionLocalProvider(
                    LocalDensity provides Density(density.density, fontScale),
                ) {
                    content()
                }
            }
        }
    }
}
