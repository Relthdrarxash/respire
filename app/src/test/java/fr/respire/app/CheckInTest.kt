package fr.respire.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CheckInTest {

    private val now = 1_800_000_000_000L

    @Test
    fun warnsWhenServiceStoppedAfterBeingOn() {
        assertTrue(CheckIn.shouldWarnStopped(hasTargets = true, seenOn = true, running = false, now = now, lastWarning = 0L))
    }

    @Test
    fun noWarningBeforeFirstActivationOrWithoutApps() {
        assertFalse(CheckIn.shouldWarnStopped(hasTargets = true, seenOn = false, running = false, now = now, lastWarning = 0L))
        assertFalse(CheckIn.shouldWarnStopped(hasTargets = false, seenOn = true, running = false, now = now, lastWarning = 0L))
        assertFalse(CheckIn.shouldWarnStopped(hasTargets = true, seenOn = true, running = true, now = now, lastWarning = 0L))
    }

    @Test
    fun atMostOneWarningPerDay() {
        assertFalse(CheckIn.shouldWarnStopped(true, true, false, now, lastWarning = now - CheckIn.DAY_MS / 2))
        assertTrue(CheckIn.shouldWarnStopped(true, true, false, now, lastWarning = now - CheckIn.DAY_MS))
    }

}
