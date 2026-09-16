package com.atelier.cards.ui

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import com.atelier.cards.domain.ExtractionGesture
import com.atelier.cards.domain.ExtractionZone

fun Modifier.extractDownward(
    enabled: Boolean, zone: ExtractionZone, onExtract: (Float, Float) -> Unit,
) = pointerInput(enabled, zone) {
    if (!enabled) return@pointerInput
    awaitEachGesture {
        val down = awaitFirstDown()
        val start = down.position
        val x = start.x / size.width
        val y = start.y / size.height
        if (!zone.contains(x, y)) return@awaitEachGesture
        var cancelled = false
        var committed = false
        do {
            val event = awaitPointerEvent()
            val pointer = event.changes.firstOrNull { it.id == down.id } ?: break
            if (pointer.isConsumed || event.changes.count { it.pressed } > 1) cancelled = true
            val delta = pointer.position - start
            if (!cancelled && !committed && pointer.pressed && ExtractionGesture.accepts(
                    zone, x, y, delta.x / density, delta.y / density)) {
                committed = true
                onExtract(x, y)
            }
            pointer.consume()
        } while (event.changes.any { it.pressed })
    }
}

/** Card gesture handlers consume their own touches; only blank table touches reach this. */
fun Modifier.blankTableGestures(enabled: Boolean, onSpread: () -> Unit, onSettings: () -> Unit) =
    pointerInput(enabled) {
        if (!enabled) return@pointerInput
        detectTapGestures(onDoubleTap = { onSpread() }, onLongPress = { onSettings() })
    }
