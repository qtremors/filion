package dev.qtremors.filion

import org.junit.Assert.assertEquals
import org.junit.Test

class AppNavigationTest {
    @Test
    fun `destination stack ignores duplicate pushes and pops in order`() {
        var stack = listOf(AppDestination.HOME)
        stack = stack.push(AppDestination.SETTINGS)
        stack = stack.push(AppDestination.SETTINGS)
        stack = stack.push(AppDestination.ABOUT)
        stack = stack.push(AppDestination.LICENSES)

        assertEquals(
            listOf(
                AppDestination.HOME,
                AppDestination.SETTINGS,
                AppDestination.ABOUT,
                AppDestination.LICENSES
            ),
            stack
        )

        stack = stack.pop()
        assertEquals(AppDestination.ABOUT, stack.last())
        stack = stack.pop()
        assertEquals(AppDestination.SETTINGS, stack.last())
        stack = stack.pop()
        assertEquals(listOf(AppDestination.HOME), stack)
        assertEquals(listOf(AppDestination.HOME), stack.pop())
    }
}
