package com.atelier.cards

import android.app.Application
import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.atelier.cards.data.Preferences
import com.atelier.cards.data.PerformanceSnapshot
import com.atelier.cards.domain.*
import java.time.LocalTime
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TableState(
    val performance: PerformanceState = PerformanceState(),
    val color: DeckColor = DeckColor.RED,
    val mapping: SuitMapping = SuitMapping(),
    val secretSecond: Int = 11,
    val realHour: Int = LocalTime.now().hour,
    val realMinute: Int = LocalTime.now().minute,
    val settingsOpen: Boolean = false,
    val rehearsal: Boolean = false,
    val round: Int = 0,
) {
    val clockText get() = String.format(Locale.ROOT, "%02d:%02d:%02d", realHour, realMinute, secretSecond)
}

class PerformanceViewModel(application: Application, private val saved: SavedStateHandle) : AndroidViewModel(application) {
    private val preferences = Preferences(application)
    private val mutableState = MutableStateFlow(TableState(
        performance = restore(), color = preferences.color, mapping = preferences.mapping,
    ))
    val state = mutableState.asStateFlow()
    private var epoch = SystemClock.elapsedRealtime()
    private var ticker: Job? = null

    fun resumeClock() {
        if (state.value.performance.phase != Phase.BOXED) return
        if (ticker?.isActive == true) return
        ticker = viewModelScope.launch {
            while (true) {
                val now = LocalTime.now()
                if (state.value.performance.phase == Phase.BOXED) {
                    mutableState.update { it.copy(
                        secretSecond = SecretClock.secondAt(SystemClock.elapsedRealtime() - epoch),
                        realHour = now.hour, realMinute = now.minute,
                    ) }
                }
                // Wake on the next secret-second boundary, rather than polling 25 times/second.
                val elapsed = SystemClock.elapsedRealtime() - epoch
                delay(SecretClock.STEP_MILLIS - elapsed.mod(SecretClock.STEP_MILLIS))
            }
        }
    }

    fun pauseClock() { ticker?.cancel(); ticker = null; persist(state.value.performance) }

    /** The UI passes the currently rendered second, preventing a tick/input boundary race. */
    fun extract(renderedSecond: Int, x: Float, y: Float) {
        val before = state.value
        if (before.settingsOpen || before.performance.phase != Phase.BOXED) return
        val quadrant = Quadrant.at(x, y)
        val result = before.performance.extract(renderedSecond, quadrant, before.mapping)
        mutableState.update { it.copy(performance = result) }
        ticker?.cancel(); ticker = null
        persist(result)
        debug("EXTRACT second=$renderedSecond quadrant=$quadrant target=${result.target?.displayName}")
    }

    fun extractionFinished() = transition { it.extractionFinished() }
    fun reveal(cardId: Int) = transition { it.reveal(cardId) }
    fun bringToFront(cardId: Int) = transition { it.bringToFront(cardId) }
    fun moveCard(cardId: Int, x: Float, y: Float) {
        mutableState.update { it.copy(performance = it.performance.move(cardId, x, y)) }
    }
    fun dropCard() { persist(state.value.performance) }
    fun spreadCards() = transition { it.spread() }

    private fun transition(reducer: (PerformanceState) -> PerformanceState) {
        val before = state.value.performance
        val after = reducer(before)
        if (before == after) return
        mutableState.update { it.copy(performance = after) }
        persist(after)
        debug("PHASE ${after.phase} revealed=${after.cards.count { it.face != null }} first=${after.firstRevealedId} target=${after.target?.displayName}")
    }

    fun reset() {
        epoch = SystemClock.elapsedRealtime()
        mutableState.update { it.copy(performance = PerformanceState(), secretSecond = 11, round = it.round + 1) }
        persist(PerformanceState())
        resumeClock()
        debug("RESET")
    }

    fun openSettings() { mutableState.update { it.copy(settingsOpen = true) } }
    fun closeSettings() { mutableState.update { it.copy(settingsOpen = false) } }
    fun setColor(color: DeckColor) {
        preferences.color = color
        mutableState.update { it.copy(color = color) }
    }
    fun assignSuit(quadrant: Quadrant, suit: Suit) {
        val mapping = state.value.mapping.assigning(quadrant, suit)
        preferences.mapping = mapping
        mutableState.update { it.copy(mapping = mapping) }
    }
    fun setRehearsal(enabled: Boolean) { mutableState.update { it.copy(rehearsal = enabled) } }

    private fun persist(value: PerformanceState) {
        saved["table_v2"] = PerformanceSnapshot.encode(value)
    }

    private fun restore(): PerformanceState = runCatching {
        val snapshot = saved.get<String>("table_v2") ?: return@runCatching PerformanceState()
        PerformanceSnapshot.decode(snapshot).let {
            if (it.phase == Phase.EXTRACTING) it.extractionFinished() else it
        }
    }.getOrDefault(PerformanceState())

    private fun debug(message: String) { if (BuildConfig.DEBUG) Log.d("AtelierMagic", message) }
}
