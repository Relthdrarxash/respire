package fr.respire.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingTest {

    @Test
    fun stepsFollowTheExpectedOrder() {
        assertEquals(
            listOf(OnboardingStep.WELCOME, OnboardingStep.APPS, OnboardingStep.DISCLOSURE, OnboardingStep.ACTIVATION),
            OnboardingStep.entries.toList(),
        )
        assertEquals(OnboardingStep.APPS, OnboardingStep.WELCOME.next())
        assertNull(OnboardingStep.ACTIVATION.next())
        assertNull(OnboardingStep.WELCOME.previous())
        assertEquals(OnboardingStep.DISCLOSURE, OnboardingStep.ACTIVATION.previous())
    }

    @Test
    fun noMoreThanFiveScreens() {
        assertTrue(OnboardingStep.entries.size <= 5)
    }

    @Test
    fun shownOnFirstLaunchOnly() {
        assertTrue(Onboarding.shouldShow(done = false, hasTargets = false, consent = false, serviceSeenOn = false))
        assertFalse(Onboarding.shouldShow(done = true, hasTargets = false, consent = false, serviceSeenOn = false))
    }

    @Test
    fun notShownToExistingUsers() {
        assertFalse(Onboarding.shouldShow(done = false, hasTargets = true, consent = false, serviceSeenOn = false))
        assertFalse(Onboarding.shouldShow(done = false, hasTargets = false, consent = true, serviceSeenOn = false))
        assertFalse(Onboarding.shouldShow(done = false, hasTargets = false, consent = false, serviceSeenOn = true))
    }

    @Test
    fun activationStatusAfterReturnFromSettings() {
        assertEquals(ActivationStatus.NOT_TRIED, Onboarding.activationStatus(serviceOn = false, returnedFromSettings = false))
        assertEquals(ActivationStatus.STILL_OFF, Onboarding.activationStatus(serviceOn = false, returnedFromSettings = true))
        assertEquals(ActivationStatus.ENABLED, Onboarding.activationStatus(serviceOn = true, returnedFromSettings = true))
        assertEquals(ActivationStatus.ENABLED, Onboarding.activationStatus(serviceOn = true, returnedFromSettings = false))
    }

    @Test
    fun refusingTheDisclosureEndsTheOnboardingWithoutBlocking() {
        assertEquals(OnboardingStep.ACTIVATION, Onboarding.afterDisclosure(accepted = true))
        assertNull(Onboarding.afterDisclosure(accepted = false))
    }

    @Test
    fun restrictedSettingsHelpFromAndroid13() {
        assertFalse(Onboarding.needsRestrictedHelp(32))
        assertTrue(Onboarding.needsRestrictedHelp(33))
        assertTrue(Onboarding.needsRestrictedHelp(35))
    }
}
