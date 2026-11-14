package fr.respire.app

import java.time.LocalDate
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PrefsPruneTest {

    private fun epoch(y: Int, m: Int, d: Int) = LocalDate.of(y, m, d).toEpochDay()

    @Test
    fun `31 decembre et 1er janvier sont voisins`() {
        // 1er janvier 2025 : la clé du 31 décembre 2024 (jour 366, bissextile) a 1 jour.
        val today = epoch(2025, 1, 1)
        assertFalse(Prefs.isExpiredDayKey("d_att_2024_366", today))
        assertFalse(Prefs.isExpiredDayKey("d_att_2024_365", today))
    }

    @Test
    fun `soixante jours exacts sont gardes, soixante et un supprimes`() {
        val today = epoch(2025, 3, 1)
        val keep = LocalDate.of(2025, 3, 1).minusDays(60)
        val drop = LocalDate.of(2025, 3, 1).minusDays(61)
        assertFalse(Prefs.isExpiredDayKey("d_att_${keep.year}_${keep.dayOfYear}", today))
        assertTrue(Prefs.isExpiredDayKey("d_att_${drop.year}_${drop.dayOfYear}", today))
    }

    @Test
    fun `annee bissextile`() {
        // 1er mars 2024 = jour 61 ; le 29 février = jour 60.
        val today = epoch(2024, 4, 30)
        assertTrue(Prefs.isExpiredDayKey("h_give_2024_60", today)) // 29 fév : 61 jours
        assertFalse(Prefs.isExpiredDayKey("h_give_2024_61", today)) // 1er mars : 60 jours
    }

    @Test
    fun `cles etrangeres ou invalides ignorees`() {
        val today = epoch(2025, 6, 1)
        assertFalse(Prefs.isExpiredDayKey("allow_com.foo", today))
        assertFalse(Prefs.isExpiredDayKey("pd_att_com.foo_2020_400", today))
        assertFalse(Prefs.isExpiredDayKey("x_1999_10", today))
    }
}
