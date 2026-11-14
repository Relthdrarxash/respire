package fr.respire.shared

import kotlin.test.Test
import kotlin.test.assertEquals

class CoherenceModelTest {
    @Test
    fun inhaleAndExhaleBoundaries() {
        val start = CoherenceTiming.snapshotAt(0L)
        assertEquals(CoherenceTiming.Phase.INHALE, start.phase)
        assertEquals(4, start.secondsRemaining)

        val midInhale = CoherenceTiming.snapshotAt(2_000L)
        assertEquals(CoherenceTiming.Phase.INHALE, midInhale.phase)
        assertEquals(0.5f, midInhale.progress)

        val startExhale = CoherenceTiming.snapshotAt(4_000L)
        assertEquals(CoherenceTiming.Phase.EXHALE, startExhale.phase)
        assertEquals(6, startExhale.secondsRemaining)

        val wrapped = CoherenceTiming.snapshotAt(10_000L)
        assertEquals(CoherenceTiming.Phase.INHALE, wrapped.phase)
        assertEquals(4, wrapped.secondsRemaining)
    }
}
