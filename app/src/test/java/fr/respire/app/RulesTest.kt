package fr.respire.app

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

/**
 * Tests de la logique des règles. Fuseau fixé à Europe/Paris pour couvrir
 * les changements d'heure (29 mars et 25 octobre 2026).
 */
class RulesTest {

    private lateinit var savedTz: TimeZone

    @Before
    fun setUp() {
        savedTz = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Paris"))
    }

    @After
    fun tearDown() {
        TimeZone.setDefault(savedTz)
    }

    /** Horodatage d'une date et heure locales. */
    private fun at(y: Int, month: Int, d: Int, h: Int, min: Int = 0): Long =
        Calendar.getInstance().apply {
            clear()
            set(y, month - 1, d, h, min, 0)
        }.timeInMillis

    private fun fields(t: Long): List<Int> {
        val c = Calendar.getInstance().apply { timeInMillis = t }
        return listOf(c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.HOUR_OF_DAY))
    }

    // ---- Plage horaire ---------------------------------------------------------

    @Test
    fun riskHours_sameDay() {
        assertEquals(setOf(14, 15, 16), Rules.riskHours(14, 17))
    }

    @Test
    fun riskHours_crossesMidnight() {
        assertEquals(setOf(22, 23, 0, 1, 2, 3, 4, 5, 6), Rules.riskHours(22, 7))
    }

    @Test
    fun riskHours_endIsExcluded() {
        assertFalse(7 in Rules.riskHours(22, 7))
    }

    @Test
    fun riskHours_startEqualsEnd_isWholeDay() {
        assertEquals(24, Rules.riskHours(9, 9).size)
    }

    @Test
    fun riskWindowStart_outsideRange_isNull() {
        assertNull(Rules.riskWindowStart(at(2026, 6, 10, 15), 22, 7))
        assertNull(Rules.riskWindowStart(at(2026, 6, 10, 7, 0), 22, 7))
    }

    @Test
    fun riskWindowStart_eveningPart_startsSameDay() {
        val start = Rules.riskWindowStart(at(2026, 6, 10, 23, 40), 22, 7)!!
        assertEquals(listOf(6, 10, 22), fields(start))
    }

    @Test
    fun riskWindowStart_afterMidnight_startsTheDayBefore() {
        val start = Rules.riskWindowStart(at(2026, 6, 11, 2, 15), 22, 7)!!
        assertEquals(listOf(6, 10, 22), fields(start))
    }

    @Test
    fun riskWindowStart_acrossMonthEnd() {
        val start = Rules.riskWindowStart(at(2026, 7, 1, 1, 0), 22, 7)!!
        assertEquals(listOf(6, 30, 22), fields(start))
    }

    @Test
    fun riskWindowStart_springForwardNight() {
        // Nuit du 28 au 29 mars 2026 : 2 h n'existe pas.
        val start = Rules.riskWindowStart(at(2026, 3, 29, 4, 0), 22, 7)!!
        assertEquals(listOf(3, 28, 22), fields(start))
    }

    @Test
    fun riskWindowStart_startHourSkippedByDst_terminates() {
        // Plage 2 h → 5 h la nuit où 2 h n'existe pas : ne doit pas boucler.
        // La plage commence alors à 3 h, première heure réelle.
        val start = Rules.riskWindowStart(at(2026, 3, 29, 4, 0), 2, 5)!!
        assertEquals(listOf(3, 29, 3), fields(start))
    }

    // ---- Blocage ---------------------------------------------------------------

    @Test
    fun block_none_belowQuota_outsideRisk() {
        assertEquals(Prefs.Block.NONE, Rules.blockReason(6, 5, null, 0, 0))
    }

    @Test
    fun block_daily_whenQuotaReached() {
        assertEquals(Prefs.Block.DAILY, Rules.blockReason(6, 6, null, 0, 0))
    }

    @Test
    fun block_dailyQuotaZero_meansUnlimited() {
        assertEquals(Prefs.Block.NONE, Rules.blockReason(0, 500, null, 0, 0))
    }

    @Test
    fun block_riskQuotaZero_blocksAtOnce() {
        assertEquals(Prefs.Block.RISK, Rules.blockReason(6, 0, 1L, 0, 0))
    }

    @Test
    fun block_riskQuota_allowsUpToLimit() {
        assertEquals(Prefs.Block.NONE, Rules.blockReason(6, 1, 1L, 1, 2))
        assertEquals(Prefs.Block.RISK, Rules.blockReason(6, 2, 1L, 2, 2))
    }

    @Test
    fun block_dailyWinsOverRisk() {
        assertEquals(Prefs.Block.DAILY, Rules.blockReason(3, 3, 1L, 0, 0))
    }

    // ---- Attente ---------------------------------------------------------------

    @Test
    fun delay_firstAttempt_isBase() {
        assertEquals(10, Rules.delay(10, 5, 1, false))
    }

    @Test
    fun delay_growsWithAttempts() {
        assertEquals(20, Rules.delay(10, 5, 3, false))
    }

    @Test
    fun delay_zeroAttemptsCounted_isBase() {
        assertEquals(10, Rules.delay(10, 5, 0, false))
    }

    @Test
    fun delay_doubledInRiskHours() {
        assertEquals(30, Rules.delay(10, 5, 2, true))
    }

    @Test
    fun delay_cappedAtMax() {
        assertEquals(Rules.MAX_DELAY_S, Rules.delay(60, 15, 10, true))
    }

    // ---- Assouplissement des heures à risque -----------------------------------

    @Test
    fun riskLooser_disabled_isNeverLooser() {
        assertFalse(Rules.isRiskLooser(false, 22, 7, 0, 23, 6, 5))
    }

    @Test
    fun riskLooser_shorterRange_isLooser() {
        assertTrue(Rules.isRiskLooser(true, 22, 7, 0, 23, 7, 0))
    }

    @Test
    fun riskLooser_widerRange_isNotLooser() {
        assertFalse(Rules.isRiskLooser(true, 22, 7, 0, 21, 8, 0))
    }

    @Test
    fun riskLooser_higherQuota_isLooser() {
        assertTrue(Rules.isRiskLooser(true, 22, 7, 0, 22, 7, 1))
    }

    @Test
    fun riskLooser_movedRange_isLooser() {
        // Même durée, mais 22 h n'est plus couvert.
        assertTrue(Rules.isRiskLooser(true, 22, 7, 0, 23, 8, 0))
    }

    // ---- Jours du graphique ----------------------------------------------------

    @Test
    fun pastDays_sevenDistinctDays_oldestFirst() {
        val days = Rules.pastDays(at(2026, 6, 10, 9))
        assertEquals(7, days.size)
        assertEquals(listOf(6, 4, 12), fields(days.first()))
        assertEquals(listOf(6, 10, 12), fields(days.last()))
    }

    @Test
    fun pastDays_afterFallBack_noDuplicate() {
        // 25 octobre 2026 : journée de 25 h. À 23 h 30, « 24 h avant » tombe le même jour.
        val days = Rules.pastDays(at(2026, 10, 25, 23, 30))
        assertEquals(listOf(10, 24, 12), fields(days[5]))
        assertEquals(listOf(10, 25, 12), fields(days[6]))
        assertEquals(7, days.map { fields(it)[1] }.toSet().size)
    }

    @Test
    fun pastDays_afterSpringForward_noGap() {
        // 29 mars 2026 : journée de 23 h. À 0 h 30 le 30, « 24 h avant » saute le 29.
        val days = Rules.pastDays(at(2026, 3, 30, 0, 30))
        assertEquals(listOf(3, 29, 12), fields(days[5]))
        assertEquals(listOf(3, 30, 12), fields(days[6]))
    }

    @Test
    fun startOfDay_isLocalMidnight() {
        assertEquals(at(2026, 6, 10, 0), Rules.startOfDay(at(2026, 6, 10, 17, 45)))
    }
}
