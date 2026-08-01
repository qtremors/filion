package dev.qtremors.filion.settings

import android.content.Context
import android.net.Uri
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class FilionPreferencesTest {
    private lateinit var context: Context
    private lateinit var preferences: FilionPreferences

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        context.getSharedPreferences("filion_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        preferences = FilionPreferences(context)
    }

    @After
    fun tearDown() {
        context.getSharedPreferences("filion_prefs", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test
    fun `appearance defaults follow system with dynamic color enabled`() {
        assertEquals(ThemeMode.SYSTEM, preferences.themeMode)
        assertTrue(preferences.dynamicColor)
        assertTrue(ThemeMode.SYSTEM.resolveDarkTheme(systemDark = true))
        assertFalse(ThemeMode.SYSTEM.resolveDarkTheme(systemDark = false))
    }

    @Test
    fun `appearance choices persist and invalid theme falls back to system`() {
        preferences.themeMode = ThemeMode.DARK
        preferences.dynamicColor = false

        val restored = FilionPreferences(context)
        assertEquals(ThemeMode.DARK, restored.themeMode)
        assertFalse(restored.dynamicColor)

        context.getSharedPreferences("filion_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("theme_mode", "UNKNOWN")
            .commit()
        assertEquals(ThemeMode.SYSTEM, restored.themeMode)
    }

    @Test
    fun `folders remain compatible deduplicated sorted and removable`() {
        val second = Uri.parse("content://storage/tree/z-models")
        val first = Uri.parse("content://storage/tree/a-models")

        preferences.addFolder(second)
        preferences.addFolder(first)
        preferences.addFolder(second)

        assertEquals(listOf(first, second), preferences.folders())

        preferences.removeFolder(first)
        assertEquals(listOf(second), preferences.folders())
    }
}
