package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

/**
 * Procedural 8-bit mono real-time Bytebeat audio synthesizer running at 8,000 Hz.
 * Implements the user's requested mathematical bytebeats:
 * - Sound 1: t*(t>>8)*t>>16&63|t>>4 (with chaotic glitch drone & high-frequency chirps)
 * - Sound 2: t&t>>7|t&t>>8
 * - Sound 3: t*(t&t>>12)/256
 * - Sound 4: t&t>>8+t*(t)
 * - Sound Finally: t*(t>>8)>>(t>>6)
 */
class BytebeatEngine {

    companion object {
        const val SAMPLE_RATE = 8000
        const val BUFFER_SIZE = 1024
        const val WAVEFORM_BUFFER_SIZE = 128
    }

    enum class SoundFormula(
        val displayName: String,
        val formulaString: String,
        val description: String
    ) {
        SOUND_1(
            "Sound 1",
            "t*(t>>8)*t>>16&63|t>>4",
            "Chaotic Glitch Drone & Chirps"
        ),
        SOUND_2(
            "Sound 2",
            "t&t>>7|t&t>>8",
            "Swirl Rhythmic Beat"
        ),
        SOUND_3(
            "Sound 3",
            "t*(t&t>>12)/256",
            "Staircase Bouncing Arpeggio"
        ),
        SOUND_4(
            "Sound 4",
            "t&t>>8+t*(t)",
            "TextOut Algorithmic Drive"
        ),
        SOUND_MODULO(
            "Sound 5",
            "t%((t&-16|t>>11)&42)<<2|t>>4",
            "Pixel Modulo & Bitshift Pattern"
        ),
        SOUND_FINALLY(
            "Sound Finally",
            "t*(t>>8)>>(t>>6)",
            "Finale Bitshift Harmony"
        )
    }

    private var audioTrack: AudioTrack? = null
    private var synthJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    private val isRunning = AtomicBoolean(false)
    private val isMuted = AtomicBoolean(false)
    private val currentSoundIndex = AtomicInteger(0)
    private var volume = 0.5f // Safe default volume for 8-bit audio

    // Shared waveform buffer for live UI visualizer (8-bit samples centered around 128)
    val liveWaveform = FloatArray(WAVEFORM_BUFFER_SIZE)
    var currentSampleT: Long = 0
        private set

    fun start() {
        if (isRunning.getAndSet(true)) return

        synthJob = scope.launch {
            try {
                val minBufSize = AudioTrack.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_8BIT
                ).coerceAtLeast(BUFFER_SIZE * 2)

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_8BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(minBufSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                audioTrack = track
                track.play()

                val audioBuffer = ByteArray(BUFFER_SIZE)
                var t = 0L

                while (isActive && isRunning.get()) {
                    val sound = SoundFormula.entries[currentSoundIndex.get() % SoundFormula.entries.size]
                    val muted = isMuted.get()
                    val currentVol = if (muted) 0f else volume

                    for (i in 0 until BUFFER_SIZE) {
                        val rawByte = calculateBytebeat(sound, t)
                        // Scale around 128 (silence in unsigned 8-bit PCM)
                        val scaled = if (currentVol == 0f) {
                            128
                        } else {
                            val centered = rawByte - 128
                            (128 + centered * currentVol).toInt().coerceIn(0, 255)
                        }
                        audioBuffer[i] = scaled.toByte()

                        if (i % (BUFFER_SIZE / WAVEFORM_BUFFER_SIZE) == 0) {
                            val waveIdx = i / (BUFFER_SIZE / WAVEFORM_BUFFER_SIZE)
                            if (waveIdx in liveWaveform.indices) {
                                liveWaveform[waveIdx] = (scaled - 128) / 128f
                            }
                        }
                        t++
                    }

                    currentSampleT = t
                    track.write(audioBuffer, 0, BUFFER_SIZE)
                }
            } catch (e: Throwable) {
                Log.e("BytebeatEngine", "Error generating audio", e)
            } finally {
                cleanUpTrack()
            }
        }
    }

    private fun calculateBytebeat(sound: SoundFormula, t: Long): Int {
        val ti = t.toInt()
        val raw = when (sound) {
            SoundFormula.SOUND_1 -> {
                // Sound 1: t*(t>>8)*t>>16&63|t>>4) with randomized high-frequency chirps / glitch drone
                val base = (((ti * (ti shr 8) * ti) shr 16) and 63) or (ti shr 4)
                // Procedural high-frequency chirp injection for Payload 1 glitch drone:
                val chirp = if ((ti and 2047) < 256) {
                    ((ti * 73) xor (ti shr 3)) and 127
                } else if ((ti and 4095) > 3800) {
                    (Random.nextInt(0, 64) + (ti * 41)) and 255
                } else {
                    0
                }
                (base + chirp) and 255
            }
            SoundFormula.SOUND_2 -> {
                // Sound 2: t&t>>7|t&t>>8
                ((ti and (ti shr 7)) or (ti and (ti shr 8))) and 255
            }
            SoundFormula.SOUND_3 -> {
                // Sound 3: t*(t&t>>12)/256
                ((ti * (ti and (ti shr 12))) / 256) and 255
            }
            SoundFormula.SOUND_4 -> {
                // Sound 4: t&t>>8+t*(t)
                ((ti and (ti shr 8)) + (ti * ti)) and 255
            }
            SoundFormula.SOUND_MODULO -> {
                // Sound 5: t%((t&-16|t>>11)&42)<<2|t>>4
                val mod = (((ti and -16) or (ti shr 11)) and 42)
                val left = if (mod != 0) (ti % mod) else 0
                ((left shl 2) or (ti shr 4)) and 255
            }
            SoundFormula.SOUND_FINALLY -> {
                // Sound Finally: t*(t>>8)>>(t>>6)
                val shift = (ti shr 6) and 31
                ((ti * (ti shr 8)) shr shift) and 255
            }
        }
        return raw and 255
    }

    fun setSound(sound: SoundFormula) {
        currentSoundIndex.set(sound.ordinal)
    }

    fun setSoundByIndex(index: Int) {
        val safeIndex = index.coerceIn(0, SoundFormula.entries.size - 1)
        currentSoundIndex.set(safeIndex)
    }

    fun getSound(): SoundFormula {
        return SoundFormula.entries[currentSoundIndex.get() % SoundFormula.entries.size]
    }

    fun setVolume(newVolume: Float) {
        volume = newVolume.coerceIn(0f, 1f)
    }

    fun getVolume(): Float = volume

    fun setMuted(muted: Boolean) {
        isMuted.set(muted)
    }

    fun isMuted(): Boolean = isMuted.get()

    fun isPlaying(): Boolean = isRunning.get()

    fun stop() {
        isRunning.set(false)
        synthJob?.cancel()
        synthJob = null
        cleanUpTrack()
    }

    private fun cleanUpTrack() {
        try {
            audioTrack?.apply {
                if (playState == AudioTrack.PLAYSTATE_PLAYING) {
                    stop()
                }
                release()
            }
        } catch (e: Exception) {
            Log.e("BytebeatEngine", "Error releasing AudioTrack", e)
        } finally {
            audioTrack = null
        }
    }
}
