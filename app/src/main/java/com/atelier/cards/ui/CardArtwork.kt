package com.atelier.cards.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atelier.cards.R
import com.atelier.cards.domain.DeckColor

@Composable
fun CardBack(color: DeckColor, modifier: Modifier = Modifier) {
    Image(painterResource(if (color == DeckColor.RED) R.drawable.back_red else R.drawable.back_blue),
        null, modifier, contentScale = ContentScale.FillBounds)
}

/** The insert flap and back wall share one hinge and one coordinate system. */
@Composable
fun TuckBoxBack(color: DeckColor, bodyHeight: androidx.compose.ui.unit.Dp, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier) {
        val flapHeight = bodyHeight * .22f
        val frontWidth = maxWidth - 9.dp // Exclude the right side panel, like BoxFront.
        BoxLid(color, Modifier.offset(y = -flapHeight).size(frontWidth, flapHeight)
            .testTag("tuck_lid"))
        BoxInterior(Modifier.fillMaxSize().testTag("tuck_interior"))
    }
}

@Composable
private fun BoxLid(color: DeckColor, modifier: Modifier = Modifier) {
    val ink = if (color == DeckColor.RED) InkRed else InkBlue
    Canvas(modifier.graphicsLayer {
        // The bottom paper crease stays attached even when the flap is tilted backward.
        transformOrigin = TransformOrigin(.5f, 1f)
        rotationX = -38f
        cameraDistance = 28 * density
    }.drawWithCache {
        val w = size.width; val h = size.height
        val shoulder = w * .055f
        val radius = minOf(9.dp.toPx(), h * .19f)
        val tongue = Path().apply {
            moveTo(0f, h)
            lineTo(0f, h * .43f)
            quadraticTo(0f, h * .35f, shoulder, h * .29f)
            lineTo(shoulder, radius)
            quadraticTo(shoulder, 0f, shoulder + radius, 0f)
            lineTo(w - shoulder - radius, 0f)
            quadraticTo(w - shoulder, 0f, w - shoulder, radius)
            lineTo(w - shoulder, h * .29f)
            quadraticTo(w, h * .35f, w, h * .43f)
            lineTo(w, h); close()
        }
        val paper = Brush.verticalGradient(listOf(Color(0xFFF1E8D5), Color(0xFFE7DCC6), Color(0xFFD6C8AC)))
        onDrawBehind {
            drawPath(tongue, paper)
            drawPath(tongue, Color(0xFFB8A88A), style = Stroke(.55.dp.toPx()))
            drawLine(Color.White.copy(alpha = .65f), Offset(shoulder + radius, .8.dp.toPx()),
                Offset(w - shoulder - radius, .8.dp.toPx()), .55.dp.toPx())
            // Thin scored paper folds, not a dark floating drop shadow.
            drawLine(Color(0xFFB1A082).copy(alpha = .5f), Offset(shoulder, h * .31f),
                Offset(w - shoulder, h * .31f), .4.dp.toPx())
            drawLine(ink.copy(alpha = .16f), Offset(1.dp.toPx(), h - 2.dp.toPx()),
                Offset(w - 1.dp.toPx(), h - 2.dp.toPx()), .5.dp.toPx())
            drawLine(Color(0xFF9D8B6D), Offset(0f, h - .35.dp.toPx()),
                Offset(w, h - .35.dp.toPx()), .7.dp.toPx())
        }
    }) { }
}
@Composable
fun BoxInterior(modifier: Modifier = Modifier) {
    Canvas(modifier.drawWithCache {
        val w = size.width - 9.dp.toPx(); val h = size.height
        val shell = Path().apply {
            moveTo(0f, 0f); lineTo(w, 0f); lineTo(w, h); lineTo(0f, h); close()
        }
        val leftFold = Path().apply {
            moveTo(0f, h * .24f); lineTo(w * .06f, h * .02f); lineTo(w * .17f, h * .08f)
            lineTo(w * .13f, h * .44f); close()
        }
        val rightFold = Path().apply {
            moveTo(w, h * .24f); lineTo(w * .94f, h * .02f); lineTo(w * .83f, h * .08f)
            lineTo(w * .87f, h * .44f); close()
        }
        val inside = Brush.horizontalGradient(listOf(Color(0xFF8E7F65), Color(0xFFD2C5AB), Color(0xFF9F8B6D)))
        onDrawBehind {
            drawPath(shell, inside)
            drawPath(leftFold, Brush.horizontalGradient(listOf(Color(0xFFE0D5BF), Color(0xFFB4A084))))
            drawPath(rightFold, Brush.horizontalGradient(listOf(Color(0xFFAA9679), Color(0xFFDFD3BB))))
            drawLine(Color.White.copy(alpha = .35f), Offset(w * .06f, h * .02f), Offset(w * .13f, h * .44f), .7.dp.toPx())
        }
    }) { }
}

private fun spadePath(w: Float, h: Float) = Path().apply {
    moveTo(w * .5f, h * .05f)
    cubicTo(w * .34f, h * .29f, w * .06f, h * .40f, w * .06f, h * .61f)
    cubicTo(w * .06f, h * .86f, w * .39f, h * .88f, w * .49f, h * .68f)
    cubicTo(w * .47f, h * .82f, w * .42f, h * .89f, w * .34f, h * .94f)
    lineTo(w * .66f, h * .94f)
    cubicTo(w * .58f, h * .89f, w * .53f, h * .82f, w * .51f, h * .68f)
    cubicTo(w * .61f, h * .88f, w * .94f, h * .86f, w * .94f, h * .61f)
    cubicTo(w * .94f, h * .40f, w * .66f, h * .29f, w * .5f, h * .05f)
    close()
}

@Composable
fun BoxFront(color: DeckColor, modifier: Modifier = Modifier) {
    val ink = if (color == DeckColor.RED) InkRed else InkBlue
    Box(modifier.drawWithCache {
        val side = 9.dp.toPx(); val foot = 5.dp.toPx()
        val w = size.width - side; val h = size.height - foot
        val notch = 20.dp.toPx(); val round = 3.dp.toPx()
        val face = Path().apply {
            moveTo(round, 0f); lineTo(w / 2 - notch, 0f)
            cubicTo(w / 2 - notch, 13.dp.toPx(), w / 2 + notch, 13.dp.toPx(), w / 2 + notch, 0f)
            lineTo(w - round, 0f); quadraticTo(w, 0f, w, round)
            lineTo(w, h - round); quadraticTo(w, h, w - round, h)
            lineTo(round, h); quadraticTo(0f, h, 0f, h - round)
            lineTo(0f, round); quadraticTo(0f, 0f, round, 0f); close()
        }
        val flank = Path().apply {
            moveTo(w, 1f); lineTo(size.width, foot); lineTo(size.width, size.height - round)
            quadraticTo(size.width, size.height, size.width - round, size.height)
            lineTo(w - round, h); lineTo(w, h - round); close()
        }
        val paper = Brush.horizontalGradient(listOf(Color(0xFFE0D4BC), Color(0xFFF7F0DF), Color(0xFFEEE3CE)))
        val flankInk = Brush.horizontalGradient(listOf(Color(0xFF9E8969), Color(0xFFC9B797)))
        val inset = 10.dp.toPx()
        onDrawBehind {
            drawPath(flank, flankInk)
            drawPath(face, paper)
            drawPath(face, Color(0xFFB5A78D), style = Stroke(.55.dp.toPx()))
            drawLine(Color.White.copy(alpha = .7f), Offset(w - .7.dp.toPx(), 3.dp.toPx()), Offset(w - .7.dp.toPx(), h - 3.dp.toPx()), .65.dp.toPx())
            drawRect(ink.copy(alpha = .8f), Offset(inset, inset + 4.dp.toPx()), Size(w - 2 * inset, h - 2 * inset - 4.dp.toPx()), style = Stroke(.7.dp.toPx()))
            val inner = inset + 3.dp.toPx()
            drawRect(ink.copy(alpha = .32f), Offset(inner, inner + 4.dp.toPx()), Size(w - 2 * inner, h - 2 * inner - 4.dp.toPx()), style = Stroke(.4.dp.toPx()))
        }
    }) {
        Column(Modifier.fillMaxSize().padding(end = 9.dp, bottom = 5.dp).padding(horizontal = 18.dp, vertical = 25.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text("ATELIER", color = ink, fontFamily = FontFamily.Serif, fontSize = 22.sp, letterSpacing = 3.sp)
            Spacer(Modifier.height(4.dp))
            Text("P L A Y I N G   C A R D S", color = ink.copy(alpha = .75f), fontSize = 6.sp)
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Canvas(Modifier.size(76.dp, 86.dp).drawWithCache {
                    val emblem = spadePath(size.width * .56f, size.height * .56f)
                    onDrawBehind {
                        drawOval(ink.copy(alpha = .65f), Offset.Zero, size, style = Stroke(.6.dp.toPx()))
                        drawOval(ink.copy(alpha = .3f), Offset(3.dp.toPx(), 3.dp.toPx()), Size(size.width - 6.dp.toPx(), size.height - 6.dp.toPx()), style = Stroke(.45.dp.toPx()))
                        with(drawContext.canvas) {
                            save(); translate(size.width * .22f, size.height * .20f)
                            drawPath(emblem, Paint().apply { this.color = ink }); restore()
                        }
                        drawLine(ink.copy(alpha = .5f), Offset(size.width * .35f, size.height * .84f), Offset(size.width * .65f, size.height * .84f), .5.dp.toPx())
                    }
                }) { }
            }
            Text("STANDARD  No. 52", color = ink, fontFamily = FontFamily.Serif, fontSize = 9.sp, letterSpacing = 1.sp)
            Spacer(Modifier.height(5.dp))
            Text("PREMIUM LINEN FINISH", color = ink.copy(alpha = .55f), fontSize = 5.sp, letterSpacing = 1.sp)
        }
    }
}

