package com.atelier.cards.data

import com.atelier.cards.domain.*

/** Small versioned snapshot. Positions, ordering and assigned faces survive activity/process recreation. */
object PerformanceSnapshot {
    fun encode(state: PerformanceState): String = buildString {
        appendLine(listOf("2", state.phase.name, state.target?.rank?.ordinal ?: -1,
            state.target?.suit?.ordinal ?: -1, state.capturedSecond ?: -1,
            state.quadrant?.ordinal ?: -1, state.firstRevealedId ?: -1).joinToString(","))
        state.cards.forEach { card ->
            appendLine(listOf(card.id, card.x, card.y, card.angle, card.z,
                card.face?.rank?.ordinal ?: -1, card.face?.suit?.ordinal ?: -1).joinToString(","))
        }
    }

    fun decode(text: String): PerformanceState {
        val lines = text.trim().lines()
        require(lines.size == 53)
        val header = lines[0].split(",")
        require(header.size == 7 && header[0] == "2")
        fun face(rank: String, suit: String): PlayingCard? =
            if (rank == "-1" && suit == "-1") null else PlayingCard(Rank.entries[rank.toInt()], Suit.entries[suit.toInt()])
        val cards = lines.drop(1).map { line ->
            val fields = line.split(",")
            require(fields.size == 7)
            TableCard(fields[0].toInt(), fields[1].toFloat(), fields[2].toFloat(), fields[3].toFloat(),
                fields[4].toInt(), face(fields[5], fields[6]))
        }
        require(cards.map { it.id }.toSet() == (0..51).toSet())
        require(cards.map { it.z }.toSet() == (0..51).toSet())
        require(cards.all { it.x.isFinite() && it.y.isFinite() && it.angle.isFinite() })
        val faces = cards.mapNotNull { it.face }
        require(faces.size == faces.toSet().size)
        val phase = Phase.valueOf(header[1])
        val target = face(header[2], header[3])
        val firstId = header[6].toInt().takeIf { it >= 0 }
        require(if (faces.isEmpty()) firstId == null else cards.find { it.id == firstId }?.face == target)
        require((phase == Phase.REVEALED) == faces.isNotEmpty())
        return PerformanceState(phase, target, header[4].toInt().takeIf { it >= 0 },
            header[5].toInt().takeIf { it >= 0 }?.let { Quadrant.entries[it] }, cards, firstId)
    }
}
