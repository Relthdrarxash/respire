package fr.respire.shared

/**
 * Rythme de cohérence cardiaque 4 s / 6 s (6 cycles par minute), partagé entre Android et iOS.
 */
object CoherenceTiming {
    const val INHALE_MS = 4_000L
    const val EXHALE_MS = 6_000L
    const val CYCLE_MS = INHALE_MS + EXHALE_MS

    enum class Phase { INHALE, EXHALE }

    data class Snapshot(val phase: Phase, val progress: Float, val secondsRemaining: Int)

    fun snapshotAt(elapsedInCycleMs: Long): Snapshot {
        val wrapped = ((elapsedInCycleMs % CYCLE_MS) + CYCLE_MS) % CYCLE_MS
        return if (wrapped < INHALE_MS) {
            val remaining = ((INHALE_MS - wrapped + 999L) / 1000L).toInt().coerceAtLeast(1)
            Snapshot(Phase.INHALE, wrapped.toFloat() / INHALE_MS, remaining)
        } else {
            val pos = wrapped - INHALE_MS
            val remaining = ((EXHALE_MS - pos + 999L) / 1000L).toInt().coerceAtLeast(1)
            Snapshot(Phase.EXHALE, pos.toFloat() / EXHALE_MS, remaining)
        }
    }
}
