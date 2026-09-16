package com.atelier.cards.domain

import com.atelier.cards.data.PerformanceSnapshot
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class DeckRulesTest {
    private fun ready(second: Int = 22, quadrant: Quadrant = Quadrant.TOP_LEFT) =
        PerformanceState().extract(second, quadrant, SuitMapping()).extractionFinished()

    @Test fun clockBoundariesAndRanksAreExact() {
        assertEquals(11, SecretClock.secondAt(1499))
        assertEquals(12, SecretClock.secondAt(1500))
        assertEquals(23, SecretClock.secondAt(19499))
        assertEquals(11, SecretClock.secondAt(19500))
        assertEquals(11, SecretClock.secondAt(-1))
        assertEquals(listOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"),
            (11..23).map { SecretClock.rankFor(it).label })
    }

    @Test fun all52TargetsWorkOnEveryPossibleFirstPhysicalCard() {
        for (second in 11..23) for (quadrant in Quadrant.entries) for (id in 0..51) {
            val deck = ready(second, quadrant).reveal(id)
            assertEquals(PlayingCard(Rank.entries[second - 11], Suit.entries[quadrant.ordinal]), deck.cards.first { it.id == id }.face)
            assertEquals(id, deck.firstRevealedId)
            assertEquals(1, deck.cards.count { it.face != null })
        }
    }

    @Test fun allCardsGetUniqueFacesInManyDifferentRevealOrders() {
        repeat(40) { seed ->
            val random = Random(seed)
            val order = (0..51).shuffled(random)
            var state = ready().spread()
            order.forEach { id -> state = state.reveal(id, random) }
            val faces = state.cards.mapNotNull { it.face }
            assertEquals(52, faces.size)
            assertEquals(52, faces.toSet().size)
            assertEquals(1, faces.count { it == state.target })
            assertEquals(state.target, state.cards.first { it.id == order.first() }.face)
            assertEquals(state, state.reveal(order.first()))
        }
    }

    @Test fun draggingAndFlippingNeverResetEachOthersState() {
        var state = ready().move(17, .1f, .82f).bringToFront(17)
        val before = state.cards.first { it.id == 17 }
        state = state.reveal(17)
        val flipped = state.cards.first { it.id == 17 }
        assertEquals(before.x, flipped.x)
        assertEquals(before.y, flipped.y)
        assertEquals(before.z, flipped.z)
        state = state.move(17, .8f, .2f)
        assertEquals(flipped.face, state.cards.first { it.id == 17 }.face)
        assertEquals(51, state.cards.first { it.id == 17 }.z)
        assertEquals(52, state.cards.map { it.id }.toSet().size)
        assertEquals(51, state.cards.count { it.id != 17 && it.face == null })
    }

    @Test fun bringToFrontPreservesFacesAndLocations() {
        val state = ready().reveal(3).move(3, .18f, .24f)
        val raised = state.bringToFront(40)
        assertEquals(51, raised.cards.first { it.id == 40 }.z)
        state.cards.forEach { old ->
            val card = raised.cards.first { it.id == old.id }
            assertEquals(old.x, card.x); assertEquals(old.y, card.y); assertEquals(old.face, card.face)
        }
        assertEquals((0..51).toSet(), raised.cards.map { it.z }.toSet())
    }

    @Test fun onlyInsideDownwardDominantDragsExtract() {
        val zone = ExtractionZone(.2f, .3f, .8f, .7f)
        assertTrue(ExtractionGesture.accepts(zone, .4f, .4f, 15f, 64f))
        assertFalse(ExtractionGesture.accepts(zone, .4f, .4f, 0f, 0f))
        assertFalse(ExtractionGesture.accepts(zone, .4f, .4f, 0f, -120f))
        assertFalse(ExtractionGesture.accepts(zone, .4f, .4f, 0f, 63f))
        assertFalse(ExtractionGesture.accepts(zone, .4f, .4f, 100f, 70f))
        assertFalse(ExtractionGesture.accepts(zone, .1f, .4f, 0f, 120f))
    }

    @Test fun noRevealOrMovementUntilExtractionFinishes() {
        val boxed = PerformanceState()
        assertEquals(boxed, boxed.reveal(12)); assertEquals(boxed, boxed.move(12, .1f, .1f))
        val extracting = boxed.extract(23, Quadrant.BOTTOM_RIGHT, SuitMapping())
        assertEquals(extracting, extracting.reveal(12))
        assertEquals(extracting, extracting.extract(11, Quadrant.TOP_LEFT, SuitMapping()))
    }

    @Test fun snapshotRestoresAllPositionsFacesAndOrdering() {
        var state = ready().spread().reveal(42).reveal(5, Random(1)).move(42, .16f, .78f).bringToFront(5)
        state = PerformanceSnapshot.decode(PerformanceSnapshot.encode(state))
        assertEquals(.16f, state.cards.first { it.id == 42 }.x)
        assertEquals(state.target, state.cards.first { it.id == 42 }.face)
        assertEquals(42, state.firstRevealedId)
        assertEquals(51, state.cards.first { it.id == 5 }.z)
        val next = state.reveal(8)
        assertEquals(3, next.cards.mapNotNull { it.face }.toSet().size)
        assertNotEquals(state.target, next.cards.first { it.id == 8 }.face)
    }

    @Test fun snapshotRoundTripAndResetForEveryPhase() {
        val boxed = PerformanceState()
        val extracting = boxed.extract(21, Quadrant.BOTTOM_LEFT, SuitMapping())
        for (state in listOf(boxed, extracting, extracting.extractionFinished(), extracting.extractionFinished().reveal(0))) {
            assertEquals(state, PerformanceSnapshot.decode(PerformanceSnapshot.encode(state)))
        }
        assertTrue(PerformanceState().cards.all { it.face == null })
        assertNull(PerformanceState().firstRevealedId)
    }

    @Test fun offscreenCardsKeepIdentityAndCanBeRecovered() {
        val state = ready().move(6, -2f, 3f)
        assertEquals(-2f, state.cards[6].x); assertEquals(3f, state.cards[6].y)
        assertEquals(state, PerformanceSnapshot.decode(PerformanceSnapshot.encode(state)))
        assertTrue(state.spread().cards.all { it.x in 0f..1f && it.y in 0f..1f })
        assertEquals(state, state.move(6, Float.NaN, 0f))
    }

    @Test fun edgeDropContinuesFullyBeyondEveryScreenEdge() {
        assertEquals(-.26f, OffscreenPlacement.destination(.02f, .5f, .2f, .16f).first, .0001f)
        assertEquals(1.26f, OffscreenPlacement.destination(.98f, .5f, .2f, .16f).first, .0001f)
        assertEquals(-.22f, OffscreenPlacement.destination(.5f, .02f, .2f, .16f).second, .0001f)
        assertEquals(1.22f, OffscreenPlacement.destination(.5f, .98f, .2f, .16f).second, .0001f)
        assertEquals(.5f to .5f, OffscreenPlacement.destination(.5f, .5f, .2f, .16f))
    }

    @Test fun quadrantBoundariesAndMappingRemainConfigurable() {
        assertEquals(Quadrant.BOTTOM_RIGHT, Quadrant.at(.5f, .5f))
        assertEquals(Quadrant.TOP_RIGHT, Quadrant.at(.5f, .49f))
        assertEquals(Quadrant.BOTTOM_LEFT, Quadrant.at(.49f, .5f))
        val mapping = SuitMapping().assigning(Quadrant.TOP_LEFT, Suit.DIAMONDS)
        assertEquals(Suit.DIAMONDS, mapping[Quadrant.TOP_LEFT])
        assertEquals(Suit.SPADES, mapping[Quadrant.BOTTOM_RIGHT])
        assertEquals(4, mapping.suits.toSet().size)
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidSecondRejected() { SecretClock.rankFor(24) }

    @Test(expected = IllegalArgumentException::class)
    fun duplicateMappingRejected() { SuitMapping(List(4) { Suit.SPADES }) }
}

