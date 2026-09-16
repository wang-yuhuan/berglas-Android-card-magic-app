package com.atelier.cards

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.atelier.cards.domain.*
import com.atelier.cards.ui.AtelierTheme
import com.atelier.cards.ui.PerformanceTable
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TableInteractionTest {
    @get:Rule val compose = createComposeRule()
    private var state by mutableStateOf(TableState(secretSecond = 22))
    private var positionCommits = 0
    private fun ready() = PerformanceState().extract(22, Quadrant.TOP_LEFT, SuitMapping()).extractionFinished()
    private fun setTable(initial: PerformanceState = PerformanceState()) {
        state = state.copy(performance = initial)
        compose.setContent { AtelierTheme {
            PerformanceTable(state,
                onExtract = { second, x, y -> state = state.copy(performance = state.performance.extract(second, Quadrant.at(x, y), state.mapping)) },
                onExtractionFinished = { state = state.copy(performance = state.performance.extractionFinished()) },
                onReveal = { id -> state = state.copy(performance = state.performance.reveal(id)) },
                onSelect = { id -> state = state.copy(performance = state.performance.bringToFront(id)) },
                onMove = { id, x, y -> positionCommits++; state = state.copy(performance = state.performance.move(id, x, y)) },
                onDrop = {}, onSpread = { state = state.copy(performance = state.performance.spread()) },
                onReset = { state = state.copy(performance = PerformanceState(), round = state.round + 1) },
                onSettings = { state = state.copy(settingsOpen = true) })
        } }
    }

    @Test fun tapsUpwardAndOutsideDragsCannotExtract() {
        setTable()
        val surface = compose.onNodeWithTag("performance_surface")
        surface.performTouchInput { click(center) }
        assertEquals(Phase.BOXED, state.performance.phase)
        surface.performTouchInput { swipe(center, Offset(center.x, height * .2f), 400) }
        assertEquals(Phase.BOXED, state.performance.phase)
        surface.performTouchInput { swipe(Offset(width * .05f, height * .4f), Offset(width * .05f, height * .8f), 400) }
        compose.onNodeWithTag("secret_clock").assertExists()
    }

    @Test fun downwardExtractionLeavesOnly52Cards() {
        state = state.copy(rehearsal = true)
        setTable()
        compose.onNodeWithTag("performance_surface").performTouchInput {
            swipe(Offset(width * .42f, height * .42f), Offset(width * .42f, height * .70f), 450)
        }
        compose.waitForIdle()
        assertEquals(Phase.READY, state.performance.phase)
        assertEquals(PlayingCard(Rank.QUEEN, Suit.SPADES), state.performance.target)
        for (tag in listOf("secret_clock", "tuck_box", "branding", "table_decoration", "hidden_controls", "rehearsal_overlay")) {
            compose.onNodeWithTag(tag).assertDoesNotExist()
        }
        for (id in 0..51) compose.onNodeWithTag("physical_card_$id").assertExists()
        assertTrue(state.performance.cards.all { it.face == null })
    }

    @Test fun singleTapDoesNotFlipAndNonTopFirstDoubleTapIsTarget() {
        setTable(ready().spread())
        val selected = compose.onNodeWithTag("physical_card_17")
        selected.performTouchInput { click(Offset(22f, 22f)) }
        compose.waitForIdle()
        assertTrue(state.performance.cards.all { it.face == null })
        selected.performTouchInput { doubleClick(Offset(22f, 22f)) }
        compose.waitForIdle()
        assertEquals(17, state.performance.firstRevealedId)
        assertEquals(state.performance.target, state.performance.cards.first { it.id == 17 }.face)
        selected.performTouchInput { doubleClick(center) }
        assertEquals(1, state.performance.cards.count { it.face != null })
    }

    @Test fun bothBackAndFaceDragWithoutResettingPoseOrIdentity() {
        setTable(ready())
        val card = compose.onNodeWithTag("physical_card_51")
        val original = state.performance.cards.first { it.id == 51 }
        card.performTouchInput { swipe(center, center + Offset(-140f, -230f), 500) }
        compose.waitForIdle()
        val moved = state.performance.cards.first { it.id == 51 }
        assertTrue(moved.x < original.x - .05f)
        assertTrue(moved.y < original.y - .05f)
        assertNull(moved.face)
        card.performTouchInput { doubleClick(center) }
        compose.waitForIdle()
        val flipped = state.performance.cards.first { it.id == 51 }
        assertEquals(moved.x, flipped.x)
        assertEquals(moved.y, flipped.y)
        assertEquals(state.performance.target, flipped.face)
        card.performTouchInput { swipe(center, center + Offset(160f, 100f), 500) }
        val placed = state.performance.cards.first { it.id == 51 }
        assertEquals(flipped.face, placed.face)
        assertTrue(placed.x > flipped.x)
        assertEquals(51, placed.z)
    }

    @Test fun blankDoubleTapSpreadsButDoesNotReveal() {
        setTable(ready())
        compose.onNodeWithTag("performance_surface").performTouchInput { doubleClick(Offset(width * .5f, height * .1f)) }
        compose.waitForIdle()
        assertEquals(52, state.performance.cards.map { it.x to it.y }.toSet().size)
        assertTrue(state.performance.cards.all { it.face == null })
        assertNotEquals(.48f, state.performance.cards.first { it.id == 51 }.y)
    }

    @Test fun blankLongPressOpensSettingsWithoutVisibleControls() {
        setTable(ready())
        compose.onNodeWithTag("hidden_controls").assertDoesNotExist()
        compose.onNodeWithTag("performance_surface").performTouchInput { longClick(Offset(width * .5f, height * .1f)) }
        assertTrue(state.settingsOpen)
        assertTrue(state.performance.cards.all { it.face == null })
    }

    @Test fun allLocalFacesDecode() {
        val assets = InstrumentationRegistry.getInstrumentation().targetContext.assets
        for (rank in Rank.entries) for (suit in Suit.entries) {
            assets.open(PlayingCard(rank, suit).assetPath).use {
                val bitmap = android.graphics.BitmapFactory.decodeStream(it)
                assertNotNull(bitmap); assertTrue(bitmap.width >= 800)
                // The native SVG's printed outside stroke and transparent surround were removed.
                assertEquals(android.graphics.Color.WHITE, bitmap.getPixel(bitmap.width / 2, 0))
                assertEquals(android.graphics.Color.WHITE, bitmap.getPixel(0, bitmap.height / 2))
                bitmap.recycle()
            }
        }
    }

    @Test fun dragCommitsOnceAndCancellationRetainsPosition() {
        setTable(ready())
        val card = compose.onNodeWithTag("physical_card_51")
        card.performTouchInput { swipe(center, center + Offset(-180f, -300f), 700) }
        assertEquals(1, positionCommits)
        val before = state.performance.cards.first { it.id == 51 }
        card.performTouchInput {
            down(center)
            moveBy(Offset(120f, 150f), 100)
            cancel()
        }
        assertEquals(2, positionCommits)
        assertTrue(state.performance.cards.first { it.id == 51 }.x > before.x)
    }

    @Test fun backgroundPixelsAreIdenticalBeforeAndAfterExtraction() {
        setTable()
        val surface = compose.onNodeWithTag("performance_surface")
        val before = surface.captureToImage().toPixelMap()
        surface.performTouchInput { swipe(center, center + Offset(0f, height * .25f), 450) }
        compose.waitForIdle()
        val after = surface.captureToImage().toPixelMap()
        for (fraction in listOf(.25f, .5f, .75f)) {
            val y = (before.height * fraction).toInt()
            assertEquals(before[8, y], after[8, y])
            assertEquals(before[before.width - 9, y], after[after.width - 9, y])
        }
    }
    @Test fun edgeDropCanCompletelyLeaveScreenThenSpreadRecoversIt() {
        setTable(ready())
        val surface = compose.onNodeWithTag("performance_surface")
        val stageWidth = surface.fetchSemanticsNode().boundsInRoot.width
        val card = compose.onNodeWithTag("physical_card_51")
        card.performTouchInput { swipe(center, center + Offset(-stageWidth * .49f, 0f), 500) }
        compose.waitForIdle()
        assertTrue(state.performance.cards.first { it.id == 51 }.x < -.2f)
        card.assertIsNotDisplayed()
        surface.performTouchInput { doubleClick(Offset(width * .5f, height * .95f)) }
        compose.waitForIdle()
        assertTrue(state.performance.cards.all { it.x in 0f..1f && it.y in 0f..1f })
        card.assertIsDisplayed()
    }

    @Test fun secretClockIsSmallAndAtTheUpperLeft() {
        setTable()
        val surface = compose.onNodeWithTag("performance_surface").fetchSemanticsNode().boundsInRoot
        val clock = compose.onNodeWithTag("secret_clock").fetchSemanticsNode().boundsInRoot
        assertTrue(clock.left < surface.width * .1f)
        assertTrue("clock=$clock surface=$surface", clock.top - surface.top < surface.height * .06f)
        assertTrue(clock.height < surface.height * .025f)
    }

    @Test fun sleeveMovesDownwardDuringExtraction() {
        setTable()
        val box = compose.onNodeWithTag("tuck_box")
        val lid = compose.onNodeWithTag("tuck_lid").getUnclippedBoundsInRoot()
        val interior = compose.onNodeWithTag("tuck_interior").getUnclippedBoundsInRoot()
        assertEquals("Lid base must share the back-wall hinge", interior.top.value, lid.bottom.value, 1f)
        val before = box.fetchSemanticsNode().boundsInRoot.top
        compose.mainClock.autoAdvance = false
        compose.runOnIdle {
            state = state.copy(performance = state.performance.extract(22, Quadrant.TOP_LEFT, state.mapping))
        }
        compose.mainClock.advanceTimeBy(160)
        val during = box.fetchSemanticsNode().boundsInRoot.top
        assertTrue("The sleeve must travel down the table", during > before + 20f)
        compose.mainClock.autoAdvance = true
        compose.waitForIdle()
        box.assertDoesNotExist()
    }
}

