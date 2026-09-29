package com.jgv.workoutplanner.screenshots

import org.junit.Assert.assertEquals
import org.junit.Test

class ScreenshotOffsetsTest {

    @Test
    fun `content that fits produces one frame`() {
        assertEquals(
            listOf(0),
            screenshotOffsets(contentHeight = 1_312, viewportHeight = 1_312, stepPixels = 1_184),
        )
    }

    @Test
    fun `terminal offset is included exactly once when shorter than the next step`() {
        assertEquals(
            listOf(0, 1_311),
            screenshotOffsets(contentHeight = 2_783, viewportHeight = 1_472, stepPixels = 1_344),
        )
    }

    @Test
    fun `long content uses regular steps followed by one terminal offset`() {
        assertEquals(
            listOf(0, 1_184, 2_368, 3_507),
            screenshotOffsets(contentHeight = 4_819, viewportHeight = 1_312, stepPixels = 1_184),
        )
    }

    @Test
    fun `exact step boundary is not duplicated`() {
        assertEquals(
            listOf(0, 1_184, 2_368),
            screenshotOffsets(contentHeight = 3_680, viewportHeight = 1_312, stepPixels = 1_184),
        )
    }
}
