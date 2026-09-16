package com.atelier.cards.domain

import kotlin.random.Random

/** Pure Kotlin rules: independently testable, with no Android clock or UI dependencies. */
object SecretClock {
    const val FIRST_SECOND = 11
    const val RANK_COUNT = 13
    const val STEP_MILLIS = 1_500L
    fun secondAt(elapsedMillis: Long): Int =
        FIRST_SECOND + ((elapsedMillis.coerceAtLeast(0) / STEP_MILLIS) % RANK_COUNT).toInt()

    fun rankFor(second: Int): Rank {
        require(second in FIRST_SECOND until FIRST_SECOND + RANK_COUNT)
        return Rank.entries[second - FIRST_SECOND]
    }
}

enum class Phase { BOXED, EXTRACTING, READY, REVEALED }

data class PerformanceState(
    val phase: Phase = Phase.BOXED,
    val target: PlayingCard? = null,
    val capturedSecond: Int? = null,
    val quadrant: Quadrant? = null,
    val cards: List<TableCard> = TableCard.freshDeck(),
    val firstRevealedId: Int? = null,
) {
    init {
        require(phase == Phase.BOXED || target != null)
        require(cards.size == TableCard.COUNT && cards.map { it.id }.toSet().size == TableCard.COUNT)
    }

    fun extract(visibleSecond: Int, touch: Quadrant, mapping: SuitMapping): PerformanceState {
        if (phase != Phase.BOXED) return this
        return copy(phase = Phase.EXTRACTING, target = PlayingCard(SecretClock.rankFor(visibleSecond), mapping[touch]),
            capturedSecond = visibleSecond, quadrant = touch)
    }

    fun extractionFinished() = if (phase == Phase.EXTRACTING) copy(phase = Phase.READY) else this
    val canInteract get() = phase == Phase.READY || phase == Phase.REVEALED

    fun reveal(cardId: Int, random: Random = Random.Default): PerformanceState {
        if (!canInteract) return this
        val card = cards.find { it.id == cardId } ?: return this
        if (card.face != null) return this
        val used = cards.mapNotNull { it.face }.toSet()
        val face = if (used.isEmpty()) target!! else {
            val remaining = Rank.entries.flatMap { rank -> Suit.entries.map { PlayingCard(rank, it) } }
                .filter { it != target && it !in used }
            remaining.random(random)
        }
        return copy(phase = Phase.REVEALED, firstRevealedId = firstRevealedId ?: cardId,
            cards = cards.map { if (it.id == cardId) it.copy(face = face) else it })
    }

    fun bringToFront(cardId: Int): PerformanceState {
        if (!canInteract || cards.none { it.id == cardId }) return this
        val order = cards.sortedBy { it.z }.filter { it.id != cardId } + cards.first { it.id == cardId }
        val layers = order.mapIndexed { index, card -> card.id to index }.toMap()
        return copy(cards = cards.map { it.copy(z = layers.getValue(it.id)) })
    }

    fun move(cardId: Int, x: Float, y: Float): PerformanceState {
        if (!canInteract || !x.isFinite() || !y.isFinite()) return this
        return copy(cards = cards.map {
            if (it.id == cardId) it.copy(x = x, y = y) else it
        })
    }

    /** Double-tapping blank table spreads the deck to expose an edge of all 52 cards. */
    fun spread(): PerformanceState {
        if (!canInteract) return this
        return copy(cards = cards.sortedBy { it.z }.mapIndexed { index, card ->
            card.copy(x = .12f + (index % 6) * .152f, y = .14f + (index / 6) * .087f,
                angle = (index % 5 - 2) * 1.25f)
        })
    }
}
