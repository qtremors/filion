package dev.qtremors.filion.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FilionFastScrollbarTest {

    @Test
    fun `fractionToIndex handles edge cases correctly`() {
        assertEquals(0, fractionToIndex(0f, 0))
        assertEquals(0, fractionToIndex(0.5f, 1))
        assertEquals(0, fractionToIndex(0f, 100))
        assertEquals(99, fractionToIndex(1f, 100))
        assertEquals(50, fractionToIndex(0.5f, 101))
    }

    @Test
    fun `continuousScrollFraction clamps correctly`() {
        val fractionStart = continuousScrollFraction(
            firstVisibleIndex = 0f,
            firstVisibleOffset = 0f,
            stride = 100f,
            totalItems = 100,
            estimatedVisibleItems = 10f,
            canScrollBackward = false,
            canScrollForward = true
        )
        assertEquals(0f, fractionStart, 0.001f)

        val fractionEnd = continuousScrollFraction(
            firstVisibleIndex = 90f,
            firstVisibleOffset = 0f,
            stride = 100f,
            totalItems = 100,
            estimatedVisibleItems = 10f,
            canScrollBackward = true,
            canScrollForward = false
        )
        assertEquals(1f, fractionEnd, 0.001f)
    }

    @Test
    fun `scrollbarStretchForDelta applies dynamic stretch within bounds`() {
        assertEquals(1f, scrollbarStretchForDelta(0f), 0.0001f)
        assertTrue(scrollbarStretchForDelta(0.01f) > 1f)
        assertTrue(scrollbarStretchForDelta(0.5f) <= 1.18f)
    }
}
