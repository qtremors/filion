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
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
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

        preferences.themeMode = ThemeMode.OLED
        assertEquals(ThemeMode.OLED, FilionPreferences(context).themeMode)
        assertTrue(ThemeMode.OLED.resolveDarkTheme(systemDark = false))
        assertTrue(ThemeMode.OLED.resolveDarkTheme(systemDark = true))

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

        val result1 = preferences.addFolder(second)
        assertEquals(FolderAddResult.ADDED, result1)

        val result2 = preferences.addFolder(first)
        assertEquals(FolderAddResult.ADDED, result2)

        val result3 = preferences.addFolder(second)
        assertEquals(FolderAddResult.ALREADY_EXISTS, result3)

        assertEquals(listOf(first, second), preferences.folders())

        preferences.removeFolder(first)
        assertEquals(listOf(second), preferences.folders())
    }

    @Test
    fun `adding parent folder prunes existing child folders`() {
        val parent = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AModels")
        val child1 = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AModels%2FCharacters")
        val child2 = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AModels%2FProps")

        preferences.addFolder(child1)
        preferences.addFolder(child2)
        assertEquals(2, preferences.folders().size)

        val result = preferences.addFolder(parent)
        assertEquals(FolderAddResult.REPLACED_CHILDREN, result)

        val folders = preferences.folders()
        assertEquals(1, folders.size)
        assertEquals(parent, folders.first())
    }

    @Test
    fun `adding child folder when parent is already present is ignored and reported`() {
        val parent = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AModels")
        val child = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AModels%2FCharacters")

        val parentResult = preferences.addFolder(parent)
        assertEquals(FolderAddResult.ADDED, parentResult)

        val childResult = preferences.addFolder(child)
        assertEquals(FolderAddResult.COVERED_BY_PARENT, childResult)

        val folders = preferences.folders()
        assertEquals(1, folders.size)
        assertEquals(parent, folders.first())
    }
}
