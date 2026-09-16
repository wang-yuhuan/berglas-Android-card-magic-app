package com.atelier.cards.data

import android.content.Context
import androidx.core.content.edit
import com.atelier.cards.domain.DeckColor
import com.atelier.cards.domain.Suit
import com.atelier.cards.domain.SuitMapping

class Preferences(context: Context) {
    private val storage = context.getSharedPreferences("atelier_preferences", Context.MODE_PRIVATE)
    var color: DeckColor
        get() = runCatching { DeckColor.valueOf(storage.getString("color", "RED")!!) }.getOrDefault(DeckColor.RED)
        set(value) { storage.edit { putString("color", value.name) } }
    var mapping: SuitMapping
        get() = runCatching {
            SuitMapping(storage.getString("mapping", null)!!.split(",").map(Suit::valueOf))
        }.getOrDefault(SuitMapping())
        set(value) { storage.edit { putString("mapping", value.suits.joinToString(",") { it.name }) } }
}
