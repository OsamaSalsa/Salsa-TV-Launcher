package com.example.ui.home

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Process
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.news.NewsVoicePlaybackTracker
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.tanh

internal object DashboardAmbientSoundPresets {
    const val OFF = "off"
    const val STUDIO_PULSE = "studio_pulse"
    const val WARM_PIANO = "warm_piano"
    const val SOFT_RAIN = "soft_rain"
    const val NIGHT_HORIZON = "night_horizon"
    const val SOFT_CHIME = "soft_chime"
    const val CUSTOM_FILE = "custom_file"

    val BUILT_IN_MODES = setOf(
        STUDIO_PULSE,
        WARM_PIANO,
        SOFT_RAIN,
        NIGHT_HORIZON,
        SOFT_CHIME
    )
}

/**
 * Pure Kotlin real-time PCM ambient soundscape synthesizer for Info & Dashboard screensavers.
 * Generates zero-bloat, gapless, offline-ready gentle audio beds with soft smoothing and voice ducking.
 */
internal object DashboardAmbientSoundEngine {
    const val SAMPLE_RATE = 22050
    private const val TWO_PI = 2.0 * PI

    // Warm Cmaj9 -> Am9 -> Fmaj9 -> G6 chord progression (Hz)
    private val PIANO_CHORDS = arrayOf(
        doubleArrayOf(130.81, 196.00, 246.94, 293.66), // C3 G3 B3 D4
        doubleArrayOf(110.00, 164.81, 246.94, 293.66), // A2 E3 B3 D4
        doubleArrayOf(87.31, 174.61, 220.00, 261.63),  // F2 F3 A3 C4
        doubleArrayOf(98.00, 196.00, 246.94, 329.63)   // G2 G3 B3 E4
    )

    // Pentatonic gentle chime frequencies (D major pentatonic)
    private val CHIME_NOTES = doubleArrayOf(
        587.33, 739.99, 880.00, 659.25, 493.88, 739.99, 880.00, 587.33
    )

    internal class SynthState {
        var sampleIndex: Long = 0L
        var currentGain: Float = 0f
        var rngState: Int = 0x5A17C931
        // Pink noise + 2-stage lowpass state
        var b0: Double = 0.0
        var b1: Double = 0.0
        var b2: Double = 0.0
        var lp1: Double = 0.0
        var lp2: Double = 0.0

        private fun nextWhiteNoise(): Double {
            var x = rngState
            x = x xor (x shl 13)
            x = x xor (x ushr 17)
            x = x xor (x shl 5)
            rngState = if (x == 0) 0x5A17C931 else x
            return (rngState.toDouble() / Int.MAX_VALUE.toDouble())
        }

        fun nextFilteredRainSample(tSec: Double): Double {
            val white = nextWhiteNoise()
            b0 = 0.99765 * b0 + white * 0.0990460
            b1 = 0.96300 * b1 + white * 0.2965164
            b2 = 0.57000 * b2 + white * 1.0526913
            val pink = (b0 + b1 + b2 + white * 0.1848) * 0.11
            // Two-pole gentle low-pass smoothing (~680 Hz rain hush)
            lp1 += 0.16 * (pink - lp1)
            lp2 += 0.12 * (lp1 - lp2)
            val swell = 0.72 + 0.28 * (0.5 + 0.5 * sin(TWO_PI * 0.075 * tSec))
            return lp2 * swell
        }
    }

    fun renderBlock(
        mode: String,
        targetAmplitude: Float,
        output: ShortArray,
        state: SynthState,
        offset: Int = 0,
        length: Int = output.size
    ) {
        if (mode !in DashboardAmbientSoundPresets.BUILT_IN_MODES) {
            for (i in offset until (offset + length)) {
                output[i] = 0
            }
            return
        }

        val sr = SAMPLE_RATE.toDouble()
        for (i in offset until (offset + length)) {
            val idx = state.sampleIndex++
            val t = idx / sr

            // Smooth exponential gain ramp (~60ms smoothing prevents any click or zipper noise)
            state.currentGain += (targetAmplitude - state.currentGain) * 0.0014f

            val rawSample: Double = when (mode) {
                DashboardAmbientSoundPresets.STUDIO_PULSE -> {
                    // Warm newsroom studio pad with subtle 72 BPM pillowy pulse
                    val pad = 0.36 * sin(TWO_PI * 65.41 * t) +
                        0.30 * sin(TWO_PI * 130.81 * t) +
                        0.20 * sin(TWO_PI * 196.00 * t + 0.3 * sin(TWO_PI * 0.12 * t)) +
                        0.12 * sin(TWO_PI * 293.66 * t)
                    val beatPhase = 0.5 + 0.5 * sin(TWO_PI * 1.2 * t - PI * 0.5)
                    val studioPulse = 0.70 + 0.30 * beatPhase * beatPhase
                    pad * studioPulse * 0.65
                }

                DashboardAmbientSoundPresets.WARM_PIANO -> {
                    // Gentle 4.5-second cycling warm Rhodes-style ambient chords
                    val chordDur = 4.5
                    val chordIdx = ((t / chordDur).toInt()) % PIANO_CHORDS.size
                    val localT = t - ((t / chordDur).toInt() * chordDur)
                    val fadeIn = (localT / 0.85).coerceIn(0.0, 1.0)
                    val fadeOut = ((chordDur - localT) / 1.15).coerceIn(0.0, 1.0)
                    val env = 0.5 * (1.0 - cos(PI * fadeIn)) * 0.5 * (1.0 - cos(PI * fadeOut))
                    val freqs = PIANO_CHORDS[chordIdx]
                    var sum = 0.0
                    for (f in freqs) {
                        sum += sin(TWO_PI * f * t) * 0.24 + sin(TWO_PI * f * 2.0 * t) * 0.05
                    }
                    sum * env * 0.85
                }

                DashboardAmbientSoundPresets.SOFT_RAIN -> {
                    state.nextFilteredRainSample(t) * 1.15
                }

                DashboardAmbientSoundPresets.NIGHT_HORIZON -> {
                    // Deep serene meditative shimmer
                    val breathe = 0.78 + 0.22 * sin(TWO_PI * 0.06 * t)
                    val base = 0.35 * sin(TWO_PI * 110.0 * t) +
                        0.26 * sin(TWO_PI * 164.81 * t) +
                        0.22 * sin(TWO_PI * 220.0 * t + 0.25 * sin(TWO_PI * 0.09 * t)) +
                        0.14 * sin(TWO_PI * 329.63 * t)
                    base * breathe * 0.68
                }

                DashboardAmbientSoundPresets.SOFT_CHIME -> {
                    // Whisper-quiet warm bed + soft periodic pentatonic chime
                    val warmBed = (0.30 * sin(TWO_PI * 146.83 * t) + 0.20 * sin(TWO_PI * 220.0 * t)) *
                        (0.75 + 0.25 * sin(TWO_PI * 0.08 * t))
                    val noteDur = 3.2
                    val noteIdx = ((t / noteDur).toInt()) % CHIME_NOTES.size
                    val noteT = t - ((t / noteDur).toInt() * noteDur)
                    val chimeFreq = CHIME_NOTES[noteIdx]
                    val chimeAttack = (noteT / 0.025).coerceIn(0.0, 1.0)
                    val chimeEnv = chimeAttack * exp(-noteT * 1.55)
                    val chime = (sin(TWO_PI * chimeFreq * t) + 0.22 * sin(TWO_PI * chimeFreq * 2.0 * t)) * chimeEnv * 0.35
                    (warmBed * 0.48) + chime
                }

                else -> 0.0
            }

            // Soft saturation ensures zero digital clipping even if waves align in phase
            val scaled = tanh(rawSample * state.currentGain.toDouble()) * Short.MAX_VALUE
            val clamped = scaled.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            output[i] = clamped.toShort()
        }
    }
}

/**
 * Plays soft background soundscapes tailored for News & Dashboard screensavers.
 * - Uses an offline procedural AudioTrack synth for built-in studio/ambient options (zero APK bloat, zero network failures).
 * - Delegates "custom_file" to ExoPlayer via [ScreensaverAmbientAudioEffect].
 * - Automatically ducks volume smoothly whenever [NewsVoicePlaybackTracker] is actively reading news aloud.
 */
@Composable
internal fun DashboardAmbientSoundEffect(
    soundMode: String,
    customSoundUri: String,
    volumePercent: Int
) {
    if (soundMode == DashboardAmbientSoundPresets.OFF) return

    val isVoiceSpeaking by NewsVoicePlaybackTracker.isSpeakingFlow.collectAsState()

    if (soundMode == DashboardAmbientSoundPresets.CUSTOM_FILE) {
        val effectiveCustomVolume = remember(volumePercent, isVoiceSpeaking) {
            val base = volumePercent.coerceIn(5, 100)
            if (isVoiceSpeaking) (base * 0.25f).toInt().coerceAtLeast(3) else base
        }
        ScreensaverAmbientAudioEffect(
            musicMode = "custom_file",
            customMusicUri = customSoundUri,
            volumePercent = effectiveCustomVolume
        )
        return
    }

    if (soundMode !in DashboardAmbientSoundPresets.BUILT_IN_MODES) return

    val modeRef = remember { AtomicReference(soundMode) }
    val gainRef = remember { AtomicReference(0f) }
    val pausedByLifecycle = remember { AtomicBoolean(false) }

    LaunchedEffect(soundMode) {
        modeRef.set(soundMode)
    }

    LaunchedEffect(volumePercent, isVoiceSpeaking) {
        // Cap built-in synth headroom (~0.46f) so dashboard audio stays pleasantly soft and never harsh
        val base = (volumePercent.coerceIn(5, 100) / 100f) * 0.46f
        val duck = if (isVoiceSpeaking) 0.26f else 1.0f
        gainRef.set(base * duck)
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val stopSignal = AtomicBoolean(false)
        val bufferSamples = 2048

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> pausedByLifecycle.set(true)
                Lifecycle.Event.ON_START -> pausedByLifecycle.set(false)
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        val audioThread = thread(
            start = true,
            isDaemon = true,
            name = "salsa-dashboard-ambient-audio"
        ) {
            try {
                Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO)
            } catch (_: Exception) {}

            val minBufBytes = AudioTrack.getMinBufferSize(
                DashboardAmbientSoundEngine.SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(bufferSamples * 2)

            var track: AudioTrack? = null
            try {
                track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(DashboardAmbientSoundEngine.SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(minBufBytes * 2)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                track.play()
                val pcmBuffer = ShortArray(bufferSamples)
                val synthState = DashboardAmbientSoundEngine.SynthState()
                var wasPaused = false

                while (!stopSignal.get()) {
                    if (pausedByLifecycle.get()) {
                        if (!wasPaused) {
                            wasPaused = true
                            try { track.pause() } catch (_: Exception) {}
                        }
                        try { Thread.sleep(120) } catch (_: InterruptedException) { break }
                        continue
                    } else if (wasPaused) {
                        wasPaused = false
                        synthState.currentGain = 0f
                        try { track.play() } catch (_: Exception) {}
                    }

                    val targetGain = gainRef.get()
                    DashboardAmbientSoundEngine.renderBlock(
                        mode = modeRef.get(),
                        targetAmplitude = targetGain,
                        output = pcmBuffer,
                        state = synthState
                    )
                    val written = track.write(pcmBuffer, 0, pcmBuffer.size)
                    if (written < 0) {
                        break
                    }
                }
            } catch (_: Exception) {
            } finally {
                try {
                    track?.pause()
                    track?.flush()
                    track?.stop()
                } catch (_: Exception) {}
                try {
                    track?.release()
                } catch (_: Exception) {}
            }
        }

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            stopSignal.set(true)
            audioThread.interrupt()
        }
    }
}
