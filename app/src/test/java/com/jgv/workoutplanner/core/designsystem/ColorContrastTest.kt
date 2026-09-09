package com.jgv.workoutplanner.core.designsystem

import androidx.compose.ui.graphics.Color
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import org.junit.Assert.assertTrue
import org.junit.Test

/** WCAG contrast guard for text/background pairs used by [AppTheme]. */
class ColorContrastTest {

    @Test
    fun `light and dark theme text pairs meet WCAG AA`() {
        val pairs = mapOf(
            "light primary" to (OnPrimaryLight to PrimaryLight),
            "light primary container" to (OnPrimaryContainerLight to PrimaryContainerLight),
            "light secondary" to (OnSecondaryLight to SecondaryLight),
            "light secondary container" to (OnSecondaryContainerLight to SecondaryContainerLight),
            "light tertiary" to (OnTertiaryLight to TertiaryLight),
            "light tertiary container" to (OnTertiaryContainerLight to TertiaryContainerLight),
            "light error" to (OnErrorLight to ErrorLight),
            "light error container" to (OnErrorContainerLight to ErrorContainerLight),
            "light background" to (OnBackgroundLight to BackgroundLight),
            "light surface" to (OnSurfaceLight to SurfaceLight),
            "light surface variant" to (OnSurfaceVariantLight to SurfaceVariantLight),
            "dark primary" to (OnPrimaryDark to PrimaryDark),
            "dark primary container" to (OnPrimaryContainerDark to PrimaryContainerDark),
            "dark secondary" to (OnSecondaryDark to SecondaryDark),
            "dark secondary container" to (OnSecondaryContainerDark to SecondaryContainerDark),
            "dark tertiary" to (OnTertiaryDark to TertiaryDark),
            "dark tertiary container" to (OnTertiaryContainerDark to TertiaryContainerDark),
            "dark error" to (OnErrorDark to ErrorDark),
            "dark error container" to (OnErrorContainerDark to ErrorContainerDark),
            "dark background" to (OnBackgroundDark to BackgroundDark),
            "dark surface" to (OnSurfaceDark to SurfaceDark),
            "dark surface variant" to (OnSurfaceVariantDark to SurfaceVariantDark),
        )

        pairs.forEach { (name, colors) ->
            val ratio = contrastRatio(colors.first, colors.second)
            assertTrue("$name contrast was $ratio; expected at least 4.5", ratio >= 4.5)
        }
    }

    private fun contrastRatio(first: Color, second: Color): Double {
        val firstLuminance = relativeLuminance(first)
        val secondLuminance = relativeLuminance(second)
        return (max(firstLuminance, secondLuminance) + 0.05) /
            (min(firstLuminance, secondLuminance) + 0.05)
    }

    private fun relativeLuminance(color: Color): Double =
        0.2126 * linearize(color.red.toDouble()) +
            0.7152 * linearize(color.green.toDouble()) +
            0.0722 * linearize(color.blue.toDouble())

    private fun linearize(component: Double): Double =
        if (component <= 0.04045) component / 12.92
        else ((component + 0.055) / 1.055).pow(2.4)
}
