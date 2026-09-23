package com.jgv.workoutplanner.screenshots

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.unit.Density
import androidx.test.core.app.ApplicationProvider
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.RoborazziRule
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.roborazziSystemPropertyTaskType
import org.junit.Assume
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import com.jgv.workoutplanner.core.designsystem.AppTheme
import java.io.File
import kotlin.math.ceil
import kotlin.math.roundToInt
import org.junit.Assert.assertEquals

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

// Mirrored layout: pseudolocale keeps the strings legible while flipping direction.
internal const val RTL_QUALIFIERS = "ar-rXB-w360dp-h800dp-notlong-port-notnight-xhdpi"

/**
 * Compose rule for scrollable captures that only runs where screenshots can run.
 *
 * The rule's activity (`ComponentActivity`) is declared by the compose test
 * manifest, which is a debug-only dependency — launching it outside a Roborazzi
 * record/verify/compare run fails (plain `./gradlew test`, including the
 * release variant). So this rule skips those runs via an assumption instead of
 * failing them, and only creates the real rule when a Roborazzi task type is
 * set. Skipped tests report as skipped, never as passed-while-asserting-nothing.
 */
class ScrollableScreenshotRule : TestRule {

    private var delegate: ComposeContentTestRule? = null

    override fun apply(base: Statement, description: Description): Statement =
        object : Statement() {
            override fun evaluate() {
                Assume.assumeTrue(
                    "Screenshots run under record/verify/compare on debug",
                    isScreenshotRun(),
                )
                delegate = createComposeRule()
                requireNotNull(delegate).apply(base, description).evaluate()
            }
        }

    fun captureScrollable(
        baseName: String,
        variant: String,
        fontScale: Float = 1f,
        scrollTag: String,
        content: @Composable () -> Unit,
    ) {
        requireNotNull(delegate) { "Rule did not run" }
            .captureScrollable(
                baseName = baseName,
                variant = variant,
                fontScale = fontScale,
                scrollTag = scrollTag,
                content = content,
            )
    }

    @OptIn(ExperimentalRoborazziApi::class)
    private fun isScreenshotRun(): Boolean =
        roborazziSystemPropertyTaskType().isEnabled()
}

/** Rule carrying the suite's comparison threshold into every capture. */
@OptIn(ExperimentalRoborazziApi::class)
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
            ScaledContent(fontScale, content)
        }
    }
}

/**
 * Multi-frame capture for scrollable screens.
 *
 * Reads the viewport height `V` (the scrollable column's own box) and the scroll
 * range `H - V` (the column's `VerticalScrollAxisRange`, which the scroller
 * maintains — no child enumeration, so bare text rows cannot go missing) and
 * captures `N = ceil(H / (V - 64dp))` frames at offsets `0, (V - 64dp), …`,
 * clamping the last flush with the content end — the overlap keeps a row from
 * falling between frames. Frame suffixes follow the suite contract: none when
 * `N = 1`; `-top`/`-bottom` when 2; `-top`/`-middle`/`-bottom` when 3;
 * `-p1`…`-pN` beyond that.
 *
 * Scrolling goes through the `ScrollBy` semantics action, so the content column
 * needs a test tag and no other machinery. The run then asserts the committed
 * baseline set for ([baseName], [variant]) is exactly the `N` images produced —
 * a missing or orphaned image fails. Measured `H`/`V`/`N` print to test output
 * for review: when a tag and the measurement disagree, the measurement is right.
 *
 * A plain `./gradlew test` run (no Roborazzi task type) skips everything, the
 * same as single-frame captures. Requires [content] to reach Compose idle when
 * enabled: screens with indeterminate progress indicators never settle and
 * cannot use this path.
 */
@OptIn(ExperimentalRoborazziApi::class)
fun ComposeContentTestRule.captureScrollable(
    baseName: String,
    variant: String,
    fontScale: Float = 1f,
    scrollTag: String,
    content: @Composable () -> Unit,
) {
    if (!roborazziSystemPropertyTaskType().isEnabled()) {
        return
    }
    setContent {
        AppTheme {
            ScaledContent(fontScale, content)
        }
    }
    waitForIdle()

    val displayDensity = ApplicationProvider.getApplicationContext<Context>()
        .resources.displayMetrics.density
    val scrollNode = onNodeWithTag(scrollTag).fetchSemanticsNode()
    val viewportHeight = scrollNode.size.height
    require(scrollNode.config.contains(SemanticsProperties.VerticalScrollAxisRange)) {
        "No vertical scroll range on $scrollTag"
    }
    val maxScrollOffset = scrollNode.config[SemanticsProperties.VerticalScrollAxisRange].maxValue()
    val contentHeight = viewportHeight + maxScrollOffset.roundToInt()
    val step = viewportHeight - (64 * displayDensity).roundToInt()
    val lastOffset = (contentHeight - viewportHeight).coerceAtLeast(0)
    // Content that fits the viewport needs no scrolling: the formula's offsets
    // would all coincide, so collapse to the single unsuffixed frame instead of
    // committing duplicate images.
    val offsets = if (lastOffset == 0) {
        listOf(0)
    } else {
        val measuredFrames = ceil(contentHeight / step.toFloat()).toInt().coerceAtLeast(1)
        List(measuredFrames) { index -> (index * step).coerceAtMost(lastOffset) }
    }
    val frameCount = offsets.size
    println("Screenshot $baseName-$variant: content=${contentHeight}px viewport=${viewportHeight}px frames=$frameCount")
    var scrolled = 0
    offsets.forEachIndexed { index, offset ->
        onNodeWithTag(scrollTag).performSemanticsAction(SemanticsActions.ScrollBy) {
            it(0f, (offset - scrolled).toFloat())
        }
        waitForIdle()
        scrolled = offset
        onRoot().captureRoboImage("$BASELINE_DIR/${frameName(baseName, variant, frameCount, index)}.png")
    }

    val expected = offsets.indices
        .map { "${frameName(baseName, variant, frameCount, it)}.png" }
        .toSet()
    // Exact match on the full stem: a startsWith check would let a longer case
    // name (profile-review-incomplete) leak into a shorter one's set.
    val filePattern = Regex(
        "^${Regex.escape(baseName)}(-top|-middle|-bottom|-p\\d+)?-${Regex.escape(variant)}\\.png$",
    )
    val actual = File(BASELINE_DIR).listFiles { _, name ->
        filePattern.matches(name)
    }?.map { it.name }?.toSet().orEmpty()
    assertEquals(
        "Baseline set for $baseName-$variant must be exactly the $frameCount measured frames",
        expected,
        actual,
    )
}

private fun frameName(baseName: String, variant: String, frameCount: Int, index: Int): String {
    val scroll = when (frameCount) {
        1 -> ""
        2 -> if (index == 0) "-top" else "-bottom"
        3 -> listOf("-top", "-middle", "-bottom")[index]
        else -> "-p${index + 1}"
    }
    return "$baseName$scroll-$variant"
}

@Composable
private fun ScaledContent(
    fontScale: Float,
    content: @Composable () -> Unit,
) {
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
