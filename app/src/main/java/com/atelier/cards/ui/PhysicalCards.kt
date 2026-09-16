package com.atelier.cards.ui

import android.graphics.BitmapFactory
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.*
import androidx.compose.ui.zIndex
import com.atelier.cards.R
import com.atelier.cards.domain.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun PhysicalCards(
    cards: List<TableCard>, color: DeckColor, cardWidth: Dp, interactive: Boolean,
    onReveal: (Int) -> Unit, onSelect: (Int) -> Unit, onMove: (Int, Float, Float) -> Unit,
    onDrop: () -> Unit, modifier: Modifier = Modifier,
) {
    val back = painterResource(if (color == DeckColor.RED) R.drawable.back_red else R.drawable.back_blue)
    var activeId by remember { mutableStateOf<Int?>(null) }
    val activeChanged = remember { { id: Int? -> activeId = id } }
    // Evaluated on pick-up/drop or a face change, never for every pointer movement.
    val coveredIds = remember(cards, activeId) {
        cards.filter { card -> card.id != activeId && card.face == null && cards.any {
            it.id != activeId && it.z > card.z && abs(it.x - card.x) < .002f && abs(it.y - card.y) < .003f
        } }.map { it.id }.toSet()
    }
    val shadowIds = remember(cards, activeId) {
        cards.filter { card -> cards.none {
            it.id != activeId && it.z < card.z && abs(it.x - card.x) < .07f && abs(it.y - card.y) < .07f
        } }.map { it.id }.toSet()
    }
    BoxWithConstraints(modifier) {
        val tableWidth = constraints.maxWidth.toFloat()
        val tableHeight = constraints.maxHeight.toFloat()
        cards.forEach { card -> key(card.id) {
            MovableCard(card, back, cardWidth, tableWidth, tableHeight, interactive,
                card.id in coveredIds, card.id in shadowIds, activeChanged,
                onReveal, onSelect, onMove, onDrop)
        } }
    }
}

@Composable
private fun MovableCard(
    card: TableCard, back: Painter, width: Dp, tableWidth: Float, tableHeight: Float,
    interactive: Boolean, covered: Boolean, castsShadow: Boolean, onActive: (Int?) -> Unit,
    onReveal: (Int) -> Unit, onSelect: (Int) -> Unit, onMove: (Int, Float, Float) -> Unit, onDrop: () -> Unit,
) {
    val widthPx = with(LocalDensity.current) { width.toPx() }
    var dragging by remember { mutableStateOf(false) }
    val localX = remember { mutableFloatStateOf(card.x) }
    val localY = remember { mutableFloatStateOf(card.y) }
    val latest by rememberUpdatedState(card)
    val select by rememberUpdatedState(onSelect)
    val reveal by rememberUpdatedState(onReveal)
    val move by rememberUpdatedState(onMove)
    val drop by rememberUpdatedState(onDrop)
    val active by rememberUpdatedState(onActive)
    val scope = rememberCoroutineScope()
    val departure = remember { Animatable(0f) }
    var departing by remember { mutableStateOf(false) }
    var destination by remember { mutableStateOf(card.x to card.y) }
    // Local pointer state is read only in placement. Commit once on release/cancellation.
    // This keeps the ViewModel, serialization and the other 51 cards out of the drag loop.
    val finish = remember { {
        if (dragging) {
            destination = OffscreenPlacement.destination(localX.floatValue, localY.floatValue,
                widthPx / tableWidth / 2, widthPx * .7f / tableHeight)
            departing = destination != (localX.floatValue to localY.floatValue)
            if (departing) scope.launch {
                departure.snapTo(0f)
                departure.animateTo(1f, tween(160, easing = LinearOutSlowInEasing))
                departing = false
            }
            move(card.id, destination.first, destination.second)
            drop()
            dragging = false
            active(null)
        }
    } }
    DisposableEffect(Unit) { onDispose { finish() } }
    Box(Modifier.offset { IntOffset(
        ((if (departing) localX.floatValue + (destination.first - localX.floatValue) * departure.value
          else if (dragging) localX.floatValue else card.x) * tableWidth - widthPx / 2).roundToInt(),
        ((if (departing) localY.floatValue + (destination.second - localY.floatValue) * departure.value
          else if (dragging) localY.floatValue else card.y) * tableHeight - widthPx * .7f).roundToInt()) }
        .size(width, width * 1.4f).zIndex(card.z.toFloat())
        .testTag("physical_card_${card.id}")
        .semantics { contentDescription = "纸牌 ${card.id + 1}，${card.face?.displayName ?: "背面"}" }
        .pointerInput(card.id, interactive) {
            if (interactive) detectTapGestures(onTap = { select(card.id) },
                onDoubleTap = { select(card.id); reveal(card.id) })
        }
        .pointerInput(card.id, interactive, tableWidth, tableHeight) {
            if (interactive) try {
                detectDragGestures(
                    onDragStart = {
                        localX.floatValue = latest.x; localY.floatValue = latest.y
                        dragging = true; active(card.id); select(card.id)
                    },
                    onDragEnd = finish, onDragCancel = finish,
                    onDrag = { change, amount ->
                        change.consume()
                        localX.floatValue += amount.x / tableWidth
                        localY.floatValue += amount.y / tableHeight
                    })
            } finally { finish() }
        }) {
        if (covered && !dragging) {
            // Only a paper rim is visible: no bitmap, clipping, flip state or shadow layer.
            Canvas(Modifier.fillMaxSize().graphicsLayer { rotationZ = card.angle; rotationX = 6f }) {
                val radius = CornerRadius(6.dp.toPx())
                drawRoundRect(Color(0xFFDDD8CA), cornerRadius = radius)
                drawRoundRect(Color(0xFFAFA999), cornerRadius = radius, style = Stroke(.5.dp.toPx()))
            }
        } else PhysicalCardSurface(card.face, card.angle, back, dragging, castsShadow, Modifier.fillMaxSize())
    }
}

@Composable
private fun PhysicalCardSurface(face: PlayingCard?, angle: Float, back: Painter, dragging: Boolean, castsShadow: Boolean, modifier: Modifier) {
    val context = LocalContext.current
    var bitmap by remember(face) { mutableStateOf<ImageBitmap?>(null) }
    var loadFailed by remember(face) { mutableStateOf(false) }
    LaunchedEffect(face) {
        face?.let {
            bitmap = withContext(Dispatchers.IO) {
                runCatching { context.assets.open(it.assetPath).use { stream ->
                    BitmapFactory.decodeStream(stream, null, BitmapFactory.Options().apply { inSampleSize = 2 })?.asImageBitmap()
                } }.getOrNull()
            }
            loadFailed = bitmap == null
        }
    }
    val flip = remember { Animatable(0f) }
    LaunchedEffect(face, bitmap, loadFailed) {
        if (face != null && (bitmap != null || loadFailed)) flip.animateTo(180f, tween(360, easing = FastOutSlowInEasing))
    }
    val front by remember { derivedStateOf { flip.value >= 90f } }
    val lift = animateFloatAsState(if (dragging) 1f else 0f, tween(100), label = "card lift")
    val shape = remember { RoundedCornerShape(6.dp) }
    Box(modifier.graphicsLayer {
        rotationZ = angle
        rotationX = 6f - lift.value * 8f
        cameraDistance = 24 * density
        scaleX = 1f + lift.value * .02f; scaleY = scaleX
        shadowElevation = (if (dragging) 14f else if (castsShadow) 7f else 0f) * density
        ambientShadowColor = Color.Black.copy(alpha = .22f)
        spotShadowColor = Color.Black.copy(alpha = .28f)
        this.shape = shape
    }) {
        Canvas(Modifier.fillMaxSize()) {
            drawRoundRect(Color(0xFFB6AF9F), topLeft = androidx.compose.ui.geometry.Offset(0f, 1.dp.toPx()),
                cornerRadius = CornerRadius(6.dp.toPx()))
        }
        Box(Modifier.fillMaxSize().graphicsLayer {
            rotationY = flip.value
            cameraDistance = 18 * density
            this.shape = shape; clip = true
        }.background(Color.White)) {
            if (!front) Image(back, null, Modifier.fillMaxSize(), contentScale = ContentScale.FillBounds)
            else Box(Modifier.fillMaxSize().graphicsLayer { rotationY = 180f }) {
                bitmap?.let { Image(it, null, Modifier.fillMaxSize(), contentScale = ContentScale.FillBounds) }
                if (loadFailed) Text(face?.displayName.orEmpty(), color = Felt, modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}
