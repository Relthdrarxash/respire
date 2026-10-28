package fr.respire.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SuggestionsTest {

    @Test
    fun onlyInstalledAndNotSelected() {
        val installed = listOf("com.android.chrome", "com.instagram.android", "com.zhiliaoapp.musically", "com.reddit.frontpage")
        val picked = Suggestions.pick(installed, selected = setOf("com.reddit.frontpage"))
        assertEquals(listOf("com.zhiliaoapp.musically", "com.instagram.android"), picked)
    }

    @Test
    fun nothingWhenAllChosen() {
        val installed = listOf("com.instagram.android", "com.google.android.youtube")
        assertTrue(Suggestions.pick(installed, installed.toSet()).isEmpty())
    }

    @Test
    fun noDuplicatePackages() {
        assertEquals(Suggestions.PACKAGES.size, Suggestions.PACKAGES.toSet().size)
    }
}
