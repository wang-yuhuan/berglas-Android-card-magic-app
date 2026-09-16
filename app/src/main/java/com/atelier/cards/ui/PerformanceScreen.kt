package com.atelier.cards.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.atelier.cards.R
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.atelier.cards.PerformanceViewModel
import com.atelier.cards.TableState
import com.atelier.cards.domain.*


@Composable
fun AtelierApp(model: PerformanceViewModel = viewModel()) {
    val state by model.state.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { model.resumeClock() }
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) { model.pauseClock() }
    AtelierTheme {
        PerformanceTable(state, model::extract, model::extractionFinished, model::reveal,
            model::bringToFront, model::moveCard, model::dropCard, model::spreadCards, model::reset, model::openSettings)
        if (state.settingsOpen) SettingsSheet(state, model::closeSettings, model::setColor,
            model::assignSuit, model::setRehearsal, { model.reset(); model.closeSettings() })
    }
}

@Composable
fun PerformanceTable(
    state: TableState, onExtract: (Int, Float, Float) -> Unit, onExtractionFinished: () -> Unit,
    onReveal: (Int) -> Unit, onSelect: (Int) -> Unit, onMove: (Int, Float, Float) -> Unit,
    onDrop: () -> Unit, onSpread: () -> Unit, onReset: () -> Unit, onSettings: () -> Unit,
) {
    val current by rememberUpdatedState(state)
    val extract by rememberUpdatedState(onExtract)
    val spread by rememberUpdatedState(onSpread)
    val settings by rememberUpdatedState(onSettings)
    val boxed = state.performance.phase == Phase.BOXED
    val interactive = state.performance.canInteract && !state.settingsOpen
    BoxWithConstraints(Modifier.fillMaxSize().background(Felt).testTag("performance_surface")) {
        Image(painterResource(R.drawable.wood_table), null, Modifier.fillMaxSize().testTag("wood_background"), contentScale = ContentScale.Crop)
        val stageHeight = maxHeight
        val density = LocalDensity.current
        val cutoutTop = WindowInsets.displayCutout.getTop(density)
        val leftHeaderWidth = with(density) { 130.dp.toPx() }
        // A centered camera cutout need not push a left-corner clock down the screen.
        val cornerCutout = ViewCompat.getRootWindowInsets(LocalView.current)?.displayCutout?.boundingRects.orEmpty()
            .any { it.left < leftHeaderWidth && it.top < cutoutTop }
        val headerInset = if (cornerCutout) with(density) { cutoutTop.toDp() } else 0.dp
        val boxCardWidth = minOf(maxWidth * .49f, maxHeight * .27f, 224.dp)
        val freeCardWidth = minOf(maxWidth * .43f, maxHeight * .30f, 205.dp)
        val boxCardHeight = boxCardWidth * 1.4f
        val halfWidth = (boxCardWidth.value + 32f) / (2 * maxWidth.value)
        val halfHeight = boxCardHeight.value * .60f / maxHeight.value
        val zone = ExtractionZone(.5f - halfWidth, .48f - halfHeight, .5f + halfWidth, .48f + halfHeight)
        Box(Modifier.fillMaxSize()
            .extractDownward(boxed && !state.settingsOpen, zone) { x, y -> extract(current.secretSecond, x, y) }
            .blankTableGestures(interactive, { spread() }, { settings() })) {
            if (boxed) {

                Row(Modifier.fillMaxWidth()
                    .padding(top = headerInset)
                    .padding(horizontal = 18.dp, vertical = 6.dp).testTag("branding"),
                    horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(state.clockText, color = Ivory.copy(alpha = .40f), fontSize = 7.sp, lineHeight = 9.sp,
                            fontFamily = FontFamily.Monospace, letterSpacing = .25.sp,
                            modifier = Modifier.testTag("secret_clock").clearAndSetSemantics { })
                        Text("ATELIER  /  TABLE No. 52", color = Ivory.copy(alpha = .24f),
                            fontSize = 5.sp, letterSpacing = 1.sp)
                    }
                    Text("STANDARD EDITION\nEST. MMXXIV", color = Ivory.copy(alpha = .25f),
                        fontFamily = FontFamily.Monospace, fontSize = 5.sp, lineHeight = 10.sp,
                        letterSpacing = .8.sp, textAlign = androidx.compose.ui.text.style.TextAlign.End)
                }
            }
            key(state.round) {
                val phase = state.performance.phase
                val extraction = remember { Animatable(if (phase == Phase.BOXED || phase == Phase.EXTRACTING) 0f else 1f) }
                val finish by rememberUpdatedState(onExtractionFinished)
                LaunchedEffect(phase) {
                    if (phase == Phase.EXTRACTING) {
                        extraction.animateTo(1f, tween(540, easing = FastOutSlowInEasing))
                        finish()
                    }
                }
                val sleeveVisible = phase == Phase.BOXED || phase == Phase.EXTRACTING
                // Read animation values only inside render-layer blocks: no per-frame sizing/layout.
                val sleeveMotion = Modifier.graphicsLayer {
                    val p = extraction.value
                    translationY = stageHeight.toPx() * 1.05f * p
                    alpha = 1f
                    rotationZ = 1.5f * p
                }
                if (sleeveVisible) {
                    Box(Modifier.fillMaxSize().then(sleeveMotion)) {
                        TuckBoxBack(state.color, boxCardHeight, Modifier.align(Alignment.Center)
                            .offset(y = -stageHeight * .02f + boxCardHeight * .08f)
                            .size(boxCardWidth + 32.dp, boxCardHeight))
                    }
                }
                PhysicalCards(state.performance.cards, state.color, freeCardWidth, interactive,
                    onReveal, onSelect, onMove, onDrop,
                    Modifier.fillMaxSize().graphicsLayer {
                        val p = extraction.value
                        val initialScale = boxCardWidth / freeCardWidth
                        scaleX = initialScale + (1f - initialScale) * p
                        scaleY = scaleX
                        transformOrigin = TransformOrigin(.5f, .48f)
                        translationY = -kotlin.math.sin(p * Math.PI).toFloat() * 5.dp.toPx()
                    }.testTag("card_table"))
                if (sleeveVisible) {
                    Box(Modifier.fillMaxSize().then(sleeveMotion)) {
                        BoxFront(state.color, Modifier.align(Alignment.Center)
                            .offset(y = -stageHeight * .02f + boxCardHeight * .255f)
                            .size(boxCardWidth + 32.dp, boxCardHeight * .90f)
                            .testTag("tuck_box"))
                    }
                }
            }
            if (boxed && state.rehearsal) RehearsalOverlay(state, zone, Modifier.fillMaxSize())
            if (boxed) {
                Box(Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 18.dp)
                    .size(104.dp, 52.dp).testTag("hidden_controls").clearAndSetSemantics { }
                    .pointerInput(onReset, onSettings) {
                        detectTapGestures(onDoubleTap = { onReset() }, onLongPress = { onSettings() })
                    }, contentAlignment = Alignment.Center) {
                    Text("—   ◇   —", color = Gold.copy(alpha = .32f), fontSize = 11.sp, letterSpacing = 3.sp)
                }
            }
        }
    }
}

@Composable
private fun RehearsalOverlay(state: TableState, zone: ExtractionZone, modifier: Modifier) {
    Box(modifier.testTag("rehearsal_overlay")) {
        Canvas(Modifier.fillMaxSize()) {
            drawLine(Gold.copy(alpha = .3f), Offset(size.width / 2, 0f), Offset(size.width / 2, size.height), 1.dp.toPx())
            drawLine(Gold.copy(alpha = .3f), Offset(0f, size.height / 2), Offset(size.width, size.height / 2), 1.dp.toPx())
            drawRect(Gold.copy(alpha = .7f), Offset(zone.left * size.width, zone.top * size.height),
                Size((zone.right - zone.left) * size.width, (zone.bottom - zone.top) * size.height), style = Stroke(1.dp.toPx()))
        }
        Quadrant.entries.forEach { quadrant ->
            val alignment = when (quadrant) {
                Quadrant.TOP_LEFT -> Alignment.TopStart; Quadrant.TOP_RIGHT -> Alignment.TopEnd
                Quadrant.BOTTOM_LEFT -> Alignment.BottomStart; Quadrant.BOTTOM_RIGHT -> Alignment.BottomEnd
            }
            Text(state.mapping[quadrant].symbol, color = Gold.copy(alpha = .65f), fontSize = 24.sp,
                modifier = Modifier.align(alignment).padding(horizontal = 32.dp, vertical = 110.dp))
        }
        Text("框内向下滑动取牌  ·  ${SecretClock.rankFor(state.secretSecond).label}", color = Gold, fontSize = 12.sp,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 100.dp))
    }
}




