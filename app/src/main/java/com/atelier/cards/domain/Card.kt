package com.atelier.cards.domain

enum class Suit(val symbol: String, val assetName: String, val chinese: String) {
    SPADES("♠\uFE0E", "spades", "黑桃"), HEARTS("♥\uFE0E", "hearts", "红心"),
    CLUBS("♣\uFE0E", "clubs", "梅花"), DIAMONDS("♦\uFE0E", "diamonds", "方块")
}

enum class Rank(val label: String, val assetName: String) {
    ACE("A", "ace"), TWO("2", "2"), THREE("3", "3"), FOUR("4", "4"),
    FIVE("5", "5"), SIX("6", "6"), SEVEN("7", "7"), EIGHT("8", "8"),
    NINE("9", "9"), TEN("10", "10"), JACK("J", "jack"), QUEEN("Q", "queen"), KING("K", "king")
}

data class PlayingCard(val rank: Rank, val suit: Suit) {
    val assetPath get() = "cards/${rank.assetName}_of_${suit.assetName}.png"
    val displayName get() = "${suit.chinese}${rank.label}"
}

enum class DeckColor { RED, BLUE }
enum class Quadrant(val chinese: String) {
    TOP_LEFT("左上"), TOP_RIGHT("右上"), BOTTOM_LEFT("左下"), BOTTOM_RIGHT("右下");

    companion object {
        // Coordinates are relative to the whole performance surface, including its center.
        // Center lines belong to the right / bottom half. Never depend on device pixels.
        fun at(xFraction: Float, yFraction: Float): Quadrant {
            require(xFraction.isFinite() && yFraction.isFinite())
            return when {
                yFraction < .5f && xFraction < .5f -> TOP_LEFT
                yFraction < .5f -> TOP_RIGHT
                xFraction < .5f -> BOTTOM_LEFT
                else -> BOTTOM_RIGHT
            }
        }
    }
}

data class SuitMapping(val suits: List<Suit> = Suit.entries.toList()) {
    init { require(suits.size == 4 && suits.toSet().size == 4) }
    operator fun get(quadrant: Quadrant) = suits[quadrant.ordinal]

    /** Swap assignments so every configuration remains a permutation of all four suits. */
    fun assigning(quadrant: Quadrant, suit: Suit): SuitMapping {
        val updated = suits.toMutableList()
        val previous = updated.indexOf(suit)
        updated[previous] = updated[quadrant.ordinal]
        updated[quadrant.ordinal] = suit
        return SuitMapping(updated)
    }
}
