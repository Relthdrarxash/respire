package fr.respire.app.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Tests des calculs affichés : pic de tentation, suggestion de plage, assouplissement des règles. */
class StatsLogicTest {

    private fun day(attempts: Map<Int, Int> = emptyMap(), shorts: Map<Int, Int> = emptyMap()) = DayDetail(
        title = "",
        attempts = List(24) { attempts[it] ?: 0 },
        giveUps = List(24) { 0 },
        shorts = List(24) { shorts[it] ?: 0 },
    )

    @Test
    fun peakHour_emptyDay_isNull() {
        assertNull(peakHour(List(24) { 0 }))
    }

    @Test
    fun peakHour_findsMax() {
        assertEquals(21, peakHour(List(24) { if (it == 21) 5 else 1 }))
    }

    @Test
    fun temptation_countsPausesAndShorts() {
        val d = day(attempts = mapOf(20 to 2), shorts = mapOf(20 to 3))
        assertEquals(5, d.temptation[20])
    }

    @Test
    fun sumHours_addsDays() {
        val week = listOf(day(attempts = mapOf(8 to 1)), day(attempts = mapOf(8 to 2)))
        assertEquals(3, sumHours(week)[8])
    }

    @Test
    fun suggestRiskWindow_noData_isNull() {
        assertNull(suggestRiskWindow(listOf(day())))
    }

    @Test
    fun suggestRiskWindow_coversBusiestThreeHours() {
        val week = listOf(day(attempts = mapOf(21 to 4, 22 to 6, 23 to 5, 9 to 2)))
        assertEquals(21 to 0, suggestRiskWindow(week))
    }

    @Test
    fun suggestRiskWindow_wrapsAroundMidnight() {
        val week = listOf(day(attempts = mapOf(23 to 5, 0 to 5, 1 to 5)))
        assertEquals(23 to 2, suggestRiskWindow(week))
    }

    @Test
    fun ruleLooser_lowerWait_isLooser() {
        assertTrue(Rule.DELAY.isLooser(10, 5))
        assertFalse(Rule.DELAY.isLooser(10, 20))
    }

    @Test
    fun ruleLooser_longerSession_isLooser() {
        assertTrue(Rule.SESSION.isLooser(10, 15))
        assertFalse(Rule.SESSION.isLooser(10, 5))
    }

    @Test
    fun ruleLooser_quota_unlimitedIsLoosest() {
        assertTrue(Rule.QUOTA.isLooser(6, 0))
        assertTrue(Rule.QUOTA.isLooser(6, 8))
        assertFalse(Rule.QUOTA.isLooser(6, 3))
        // Passer d'illimité à une limite rend la règle plus stricte.
        assertFalse(Rule.QUOTA.isLooser(0, 6))
    }
}
