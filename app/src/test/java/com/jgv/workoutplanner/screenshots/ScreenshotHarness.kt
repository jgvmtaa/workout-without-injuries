package com.jgv.workoutplanner.screenshots

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.unit.Density
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.RoborazziRule
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.captureScreenRoboImage
import com.github.takahirom.roborazzi.roborazziSystemPropertyTaskType
import org.junit.Assume
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import com.jgv.workoutplanner.core.designsystem.AppTheme
import org.robolectric.RuntimeEnvironment
import java.io.File
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
 * `test` (with no task type set, screenshot tests are skipped):
 *
 * - `./gradlew recordRoborazziDebug` writes baselines into `src/test/screenshots/`.
 * - `./gradlew verifyRoborazziDebug` byte-compares against the committed baselines.
 *
 * One test class per product area lives in this package; every class declares the
 * runner, graphics mode, and light qualifier, plus this rule.
 */
internal const val BASELINE_DIR = "src/test/screenshots"
internal const val LOADING_FRAME_TIME_MILLIS = 600L

// Locale leads, density trails; only the night segment varies between the
// light and dark variants.
internal const val LIGHT_QUALIFIERS = "en-rUS-w360dp-h800dp-notlong-port-notnight-xhdpi"
internal const val DARK_QUALIFIERS = "en-rUS-w360dp-h800dp-notlong-port-night-xhdpi"

// Mirrored layout: pseudolocale keeps the strings legible while flipping direction.
internal const val RTL_QUALIFIERS = "ar-rXB-w360dp-h800dp-notlong-port-notnight-xhdpi"

/**
 * Compose rule for single-frame and scrollable captures that only runs where
 * screenshots can run.
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
        listState: LazyListState? = null,
        content: @Composable () -> Unit,
    ) {
        requireNotNull(delegate) { "Rule did not run" }
            .captureScrollable(
                baseName = baseName,
                variant = variant,
                fontScale = fontScale,
                scrollTag = scrollTag,
                listState = listState,
                content = content,
            )
    }

    fun captureScreenshot(
        fileName: String,
        fontScale: Float = 1f,
        frameTimeMillis: Long? = null,
        content: @Composable () -> Unit,
    ) {
        requireNotNull(delegate) { "Rule did not run" }
            .captureScreenshot(
                fileName = fileName,
                fontScale = fontScale,
                frameTimeMillis = frameTimeMillis,
                content = content,
            )
    }

    fun captureDialogScreenshot(
        fileName: String,
        fontScale: Float = 1f,
        dialogTitle: String,
        content: @Composable () -> Unit,
    ) {
        requireNotNull(delegate) { "Rule did not run" }
            .captureDialogScreenshot(
                fileName = fileName,
                fontScale = fontScale,
                dialogTitle = dialogTitle,
                content = content,
            )
    }

    fun captureMenuScreenshot(
        fileName: String,
        fontScale: Float = 1f,
        menuButtonDescription: String,
        enabledItems: List<String>,
        disabledItems: List<String> = emptyList(),
        content: @Composable () -> Unit,
    ) {
        requireNotNull(delegate) { "Rule did not run" }
            .captureMenuScreenshot(
                fileName = fileName,
                fontScale = fontScale,
                menuButtonDescription = menuButtonDescription,
                enabledItems = enabledItems,
                disabledItems = disabledItems,
                content = content,
            )
    }

    fun assertIndeterminateProgressDisplayed() {
        requireNotNull(delegate) { "Rule did not run" }
            .onNode(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.ProgressBarRangeInfo,
                    ProgressBarRangeInfo.Indeterminate,
                ),
                useUnmergedTree = true,
            )
            .assertIsDisplayed()
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
 * Renders [content] through the Compose test rule in [AppTheme] — the theme is
 * driven by the test qualifier, never by stubbing — and captures it to the
 * suite's baseline path. [fileName] is
 * `<screen>-<state>[-<scroll>]-<variant>` without extension.
 *
 * Large-text variants ([fontScale] other than 1.0) provide a [Density] with
 * the scaled font size. The app is fully Compose, and Compose reads text
 * scaling from [Density], so this renders the same pixels as a device
 * setting; `dp` dimensions and the viewport are untouched. The test clock is
 * held after one frame by default, or after [frameTimeMillis] when a case needs
 * a representative animation frame, so animations render deterministically
 * and cannot keep Robolectric's main thread alive during teardown. Variants at
 * the default scale run the otherwise unmodified production composition.
 */
@OptIn(ExperimentalRoborazziApi::class)
fun ComposeContentTestRule.captureScreenshot(
    fileName: String,
    fontScale: Float = 1f,
    frameTimeMillis: Long? = null,
    content: @Composable () -> Unit,
) {
    Assume.assumeTrue(
        "Screenshots run under record/verify/compare on debug",
        roborazziSystemPropertyTaskType().isEnabled(),
    )
    mainClock.autoAdvance = false
    setContent {
        AppTheme {
            ScaledContent(fontScale, content)
        }
    }
    // Render a deterministic frame. Keeping auto-advance disabled prevents
    // indeterminate indicators from continuously scheduling frames during
    // capture and activity teardown. Loading cases advance farther so their
    // pinned frame contains a substantial, regression-detectable arc.
    if (frameTimeMillis == null) {
        mainClock.advanceTimeByFrame()
    } else {
        require(frameTimeMillis >= 0L) { "frameTimeMillis must not be negative" }
        mainClock.advanceTimeBy(frameTimeMillis)
    }
    waitForIdle()
    onRoot().captureRoboImage("$BASELINE_DIR/$fileName.png")
}

/**
 * Single-frame capture with a hoisted dialog held open (`showRegenerateConfirm`,
 * `pendingRemoval`).
 *
 * Dialogs render in a second window, so there are two roots and `onRoot()` is
 * ambiguous; capturing through any single node would also crop to that node's
 * bounds. This captures the complete app window instead. [dialogTitle] must
 * name the dialog's title and is asserted displayed so a missing dialog fails
 * instead of recording a dialog-less baseline. The clock advances past the
 * dialog's enter animation so the captured frame is the settled state.
 */
@OptIn(ExperimentalRoborazziApi::class)
fun ComposeContentTestRule.captureDialogScreenshot(
    fileName: String,
    fontScale: Float = 1f,
    dialogTitle: String,
    content: @Composable () -> Unit,
) {
    Assume.assumeTrue(
        "Screenshots run under record/verify/compare on debug",
        roborazziSystemPropertyTaskType().isEnabled(),
    )
    val originalFontScale = RuntimeEnvironment.getFontScale()
    try {
        // Dialog uses a separate Android window, which does not inherit the
        // LocalDensity override used by ordinary captures. Set Robolectric's
        // resource configuration so both the activity and dialog windows see
        // the requested system font scale.
        RuntimeEnvironment.setFontScale(fontScale)
        mainClock.autoAdvance = false
        setContent {
            AppTheme {
                content()
            }
        }
        mainClock.advanceTimeBy(1_000L)
        waitForIdle()
        onNodeWithText(dialogTitle).assertIsDisplayed()
        captureScreenRoboImage("$BASELINE_DIR/$fileName.png")
    } finally {
        RuntimeEnvironment.setFontScale(originalFontScale)
    }
}

/**
 * Opens and captures a [androidx.compose.material3.DropdownMenu].
 *
 * Menus render in a separate popup window, so this uses the same full-screen
 * capture as dialogs. Keep automatic clock advancement enabled until the
 * popup's enter transition settles: freezing its initial frame records a
 * transparent popup even though its semantics are already displayed.
 */
@OptIn(ExperimentalRoborazziApi::class)
fun ComposeContentTestRule.captureMenuScreenshot(
    fileName: String,
    fontScale: Float = 1f,
    menuButtonDescription: String,
    enabledItems: List<String>,
    disabledItems: List<String> = emptyList(),
    content: @Composable () -> Unit,
) {
    Assume.assumeTrue(
        "Screenshots run under record/verify/compare on debug",
        roborazziSystemPropertyTaskType().isEnabled(),
    )
    val originalFontScale = RuntimeEnvironment.getFontScale()
    try {
        RuntimeEnvironment.setFontScale(fontScale)
        mainClock.autoAdvance = true
        setContent {
            AppTheme {
                content()
            }
        }
        waitForIdle()
        onNodeWithContentDescription(menuButtonDescription).performClick()
        waitForIdle()
        enabledItems.forEach { item ->
            onNodeWithText(item).assertIsDisplayed().assertIsEnabled()
        }
        disabledItems.forEach { item ->
            onNodeWithText(item).assertIsDisplayed().assertIsNotEnabled()
        }
        mainClock.autoAdvance = false
        captureScreenRoboImage("$BASELINE_DIR/$fileName.png")
    } finally {
        RuntimeEnvironment.setFontScale(originalFontScale)
    }
}

/**
 * Multi-frame capture for scrollable screens.
 *
 * Reads the viewport height `V` (the scrollable column's own box) and the scroll
 * range `H - V` (the column's `VerticalScrollAxisRange`, which the scroller
 * maintains — no child enumeration, so bare text rows cannot go missing) and
 * captures at offsets `0, (V - 64dp), …`, followed by the exact content-end
 * offset when it is not already present. The overlap keeps a row from falling
 * between frames without ever capturing the same terminal frame twice. Frame
 * suffixes follow the suite contract: none when `N = 1`; `-top`/`-bottom` when
 * 2; `-top`/`-middle`/`-bottom` when 3; `-p1`…`-pN` beyond that.
 *
 * Scrolling a `Column` goes through the `ScrollBy` semantics action; scrolling a
 * `LazyColumn` needs its hoisted [LazyListState] (lazy lists offer no `ScrollBy`
 * action), driven here with exact pixel deltas. Either way the content needs a
 * test tag and no other machinery. The run then asserts the committed baseline
 * set for ([baseName], [variant]) is exactly the `N` images produced — a missing
 * or orphaned image fails. Measured `H`/`V`/`N` print to test output for review:
 * when a tag and the measurement disagree, the measurement is right.
 *
 * Lazy lists estimate unseen items and refine the range as they realize, so
 * with [listState] the harness traverses once to observe the stable maximum
 * before capturing. A plain `./gradlew test` run (no Roborazzi task type) skips
 * everything, the same as single-frame captures. Requires [content] to reach
 * Compose idle when enabled: screens with indeterminate progress indicators
 * never settle and cannot use this path.
 */
@OptIn(ExperimentalRoborazziApi::class)
fun ComposeContentTestRule.captureScrollable(
    baseName: String,
    variant: String,
    fontScale: Float = 1f,
    scrollTag: String,
    listState: LazyListState? = null,
    content: @Composable () -> Unit,
) {
    Assume.assumeTrue(
        "Screenshots run under record/verify/compare on debug",
        roborazziSystemPropertyTaskType().isEnabled(),
    )
    setContent {
        AppTheme {
            ScaledContent(fontScale, content)
        }
    }
    waitForIdle()

    val displayDensity = ApplicationProvider.getApplicationContext<Context>()
        .resources.displayMetrics.density

    fun scrollMetrics(): Pair<Int, Int> {
        val node = onNodeWithTag(scrollTag).fetchSemanticsNode()
        val viewport = node.size.height
        require(node.config.contains(SemanticsProperties.VerticalScrollAxisRange)) {
            "No vertical scroll range on $scrollTag"
        }
        val maxScroll = node.config[SemanticsProperties.VerticalScrollAxisRange].maxValue().roundToInt()
        return viewport to maxScroll
    }

    fun scrollByPixels(delta: Float) {
        if (listState != null) {
            runBlocking { listState.scrollBy(delta) }
        } else {
            onNodeWithTag(scrollTag).performSemanticsAction(SemanticsActions.ScrollBy) {
                it(0f, delta)
            }
        }
        waitForIdle()
    }

    val (viewportHeight, initialMax) = scrollMetrics()
    val step = viewportHeight - (64 * displayDensity).roundToInt()
    var maxSeen = initialMax
    var position = 0
    if (listState != null) {
        while (true) {
            maxSeen = maxOf(maxSeen, scrollMetrics().second)
            if (position >= maxSeen) {
                break
            }
            val next = minOf(position + step, maxSeen)
            scrollByPixels((next - position).toFloat())
            position = next
        }
        if (position > 0) {
            scrollByPixels(-position.toFloat())
        }
    }
    val contentHeight = viewportHeight + maxSeen
    val offsets = screenshotOffsets(contentHeight, viewportHeight, step)
    val frameCount = offsets.size
    println("Screenshot $baseName-$variant: content=${contentHeight}px viewport=${viewportHeight}px frames=$frameCount")
    var scrolled = 0
    offsets.forEachIndexed { index, offset ->
        scrollByPixels((offset - scrolled).toFloat())
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

/** Returns overlapping scroll offsets with exactly one frame flush to the content end. */
internal fun screenshotOffsets(
    contentHeight: Int,
    viewportHeight: Int,
    stepPixels: Int,
): List<Int> {
    require(contentHeight >= 0) { "contentHeight must not be negative" }
    require(viewportHeight >= 0) { "viewportHeight must not be negative" }
    require(stepPixels > 0) { "stepPixels must be positive" }

    val lastOffset = (contentHeight - viewportHeight).coerceAtLeast(0)
    if (lastOffset == 0) return listOf(0)

    return (0 until lastOffset step stepPixels).toList() + lastOffset
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
