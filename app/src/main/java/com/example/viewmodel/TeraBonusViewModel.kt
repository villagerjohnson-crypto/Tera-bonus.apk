package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.BytebeatEngine
import com.example.model.GDIPayload
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class TeraBonusUiState(
    val currentPayload: GDIPayload = GDIPayload.PAYLOAD_1,
    val currentSound: BytebeatEngine.SoundFormula = BytebeatEngine.SoundFormula.SOUND_1,
    val isPlaying: Boolean = true,
    val isMuted: Boolean = false,
    val volume: Float = 0.45f,
    val payloadDurationSec: Int = 8, // 5 - 10 seconds as specified by user
    val payloadProgress: Float = 0f,
    val isAutoLoop: Boolean = true,
    val isFullscreen: Boolean = false,
    val showScanlines: Boolean = true,
    val showTelemetry: Boolean = true,
    val showStartMenu: Boolean = false,
    val showAboutDialog: Boolean = false,
    val showPayloadDialog: Boolean = false,
    val currentSampleT: Long = 0L
)

class TeraBonusViewModel : ViewModel() {

    private val bytebeatEngine = BytebeatEngine()

    private val _uiState = MutableStateFlow(TeraBonusUiState())
    val uiState: StateFlow<TeraBonusUiState> = _uiState.asStateFlow()

    // Real-time audio waveform buffer exposed for UI visualizer
    val liveWaveform: FloatArray
        get() = bytebeatEngine.liveWaveform

    private var sequencerJob: Job? = null

    init {
        bytebeatEngine.setVolume(_uiState.value.volume)
        bytebeatEngine.setSound(_uiState.value.currentSound)
        bytebeatEngine.start()
        startSequencer()
    }

    private fun startSequencer() {
        sequencerJob?.cancel()
        sequencerJob = viewModelScope.launch {
            var elapsedInPayloadMs = 0L
            val tickIntervalMs = 50L

            while (isActive) {
                delay(tickIntervalMs)
                if (!_uiState.value.isPlaying) continue

                elapsedInPayloadMs += tickIntervalMs
                val totalDurationMs = _uiState.value.payloadDurationSec * 1000L
                val progress = (elapsedInPayloadMs.toFloat() / totalDurationMs).coerceIn(0f, 1f)

                _uiState.value = _uiState.value.copy(
                    payloadProgress = progress,
                    currentSampleT = bytebeatEngine.currentSampleT
                )

                if (elapsedInPayloadMs >= totalDurationMs) {
                    elapsedInPayloadMs = 0L
                    if (_uiState.value.isAutoLoop) {
                        advanceToNextPayload()
                    }
                }
            }
        }
    }

    fun advanceToNextPayload() {
        val payloads = GDIPayload.entries
        val nextIdx = (_uiState.value.currentPayload.ordinal + 1) % payloads.size
        selectPayload(payloads[nextIdx])
    }

    fun selectPayload(payload: GDIPayload) {
        val sound = payload.defaultBytebeat
        bytebeatEngine.setSound(sound)
        _uiState.value = _uiState.value.copy(
            currentPayload = payload,
            currentSound = sound,
            payloadProgress = 0f,
            showStartMenu = false,
            showPayloadDialog = false
        )
    }

    fun selectSound(sound: BytebeatEngine.SoundFormula) {
        bytebeatEngine.setSound(sound)
        _uiState.value = _uiState.value.copy(
            currentSound = sound
        )
    }

    fun togglePlayPause() {
        val newPlaying = !_uiState.value.isPlaying
        if (newPlaying) {
            bytebeatEngine.start()
        } else {
            bytebeatEngine.stop()
        }
        _uiState.value = _uiState.value.copy(isPlaying = newPlaying)
    }

    fun toggleMute() {
        val newMuted = !_uiState.value.isMuted
        bytebeatEngine.setMuted(newMuted)
        _uiState.value = _uiState.value.copy(isMuted = newMuted)
    }

    fun setVolume(volume: Float) {
        bytebeatEngine.setVolume(volume)
        _uiState.value = _uiState.value.copy(volume = volume)
    }

    fun setPayloadDurationSec(seconds: Int) {
        val clamped = seconds.coerceIn(5, 10)
        _uiState.value = _uiState.value.copy(payloadDurationSec = clamped)
    }

    fun toggleAutoLoop() {
        _uiState.value = _uiState.value.copy(isAutoLoop = !_uiState.value.isAutoLoop)
    }

    fun toggleFullscreen() {
        _uiState.value = _uiState.value.copy(
            isFullscreen = !_uiState.value.isFullscreen,
            showStartMenu = false
        )
    }

    fun toggleScanlines() {
        _uiState.value = _uiState.value.copy(showScanlines = !_uiState.value.showScanlines)
    }

    fun toggleTelemetry() {
        _uiState.value = _uiState.value.copy(showTelemetry = !_uiState.value.showTelemetry)
    }

    fun toggleStartMenu() {
        _uiState.value = _uiState.value.copy(showStartMenu = !_uiState.value.showStartMenu)
    }

    fun dismissStartMenu() {
        if (_uiState.value.showStartMenu) {
            _uiState.value = _uiState.value.copy(showStartMenu = false)
        }
    }

    fun setShowAboutDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(
            showAboutDialog = show,
            showStartMenu = false
        )
    }

    fun setShowPayloadDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(
            showPayloadDialog = show,
            showStartMenu = false
        )
    }

    override fun onCleared() {
        super.onCleared()
        sequencerJob?.cancel()
        bytebeatEngine.stop()
    }
}
