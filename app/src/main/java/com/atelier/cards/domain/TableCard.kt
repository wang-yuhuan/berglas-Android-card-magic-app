package com.atelier.cards.domain

/** Stable physical identity; its face is assigned only when this particular card is revealed. */
data class TableCard(
    val id: Int,
    val x: Float,
    val y: Float,
    val angle: Float,
    val z: Int,
    val face: PlayingCard? = null,
) {
    companion object {
        const val COUNT = 52
        fun freshDeck() = List(COUNT) { id ->
            val depth = COUNT - 1 - id
            TableCard(id, .5f + depth * .00024f, .48f + depth * .00055f,
                -3f + (id % 5 - 2) * .10f, id)
        }
    }
}

/** Invisible extraction zone, expressed in full-table fractions for any phone size. */
data class ExtractionZone(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    fun contains(x: Float, y: Float) = x in left..right && y in top..bottom
}

object ExtractionGesture {
    const val DISTANCE_DP = 64f
    const val VERTICAL_DOMINANCE = 1.5f
    fun accepts(zone: ExtractionZone, startX: Float, startY: Float, dxDp: Float, dyDp: Float): Boolean =
        zone.contains(startX, startY) && dyDp >= DISTANCE_DP &&
            dyDp >= kotlin.math.abs(dxDp) * VERTICAL_DOMINANCE
}

/** Continue an edge-directed drop beyond the glass; a finger cannot travel beyond a real screen. */
object OffscreenPlacement {
    fun destination(x: Float, y: Float, halfWidth: Float, halfHeight: Float): Pair<Float, Float> {
        val outX = when {
            x < .045f -> minOf(x, -halfWidth - .06f)
            x > .955f -> maxOf(x, 1f + halfWidth + .06f)
            else -> x
        }
        val outY = when {
            y < .045f -> minOf(y, -halfHeight - .06f)
            y > .955f -> maxOf(y, 1f + halfHeight + .06f)
            else -> y
        }
        return outX to outY
    }
}
