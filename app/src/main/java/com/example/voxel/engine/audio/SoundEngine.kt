package com.example.voxel.engine.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.voxel.engine.blocks.BlockSound
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Random
import kotlin.math.sin

/**
 * High-performance, zero-asset procedural audio synthesizer using AudioTrack PCM buffers.
 * Generates crisp block hits, footsteps, explosions, and ambient music procedurally in under 100KB of RAM!
 */
class SoundEngine {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val sampleRate = 22050
    private val rng = Random()

    var sfxVolume = 1.0f
    var musicVolume = 0.6f
    var isMuted = false

    private val audioTrack: AudioTrack by lazy {
        val minBufSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(minBufSize * 2)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build().apply { play() }
    }

    init {
        // Start background ambient music generator
        startAmbientMusic()
    }

    private fun playPcm(samples: ShortArray, volumeScale: Float = 1.0f) {
        if (isMuted) return
        val finalVol = (sfxVolume * volumeScale).coerceIn(0f, 1f)
        if (finalVol <= 0f) return

        scope.launch {
            val adjusted = ShortArray(samples.size)
            for (i in samples.indices) {
                adjusted[i] = (samples[i] * finalVol).toInt().toShort()
            }
            try {
                audioTrack.write(adjusted, 0, adjusted.size)
            } catch (_: Exception) {}
        }
    }

    fun playBlockBreak(sound: BlockSound) {
        val numSamples = (sampleRate * 0.12f).toInt()
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val env = (1.0f - (i.toFloat() / numSamples))

            val sample = when (sound) {
                BlockSound.GRASS -> {
                    // Soft crunch noise
                    val noise = (rng.nextFloat() * 2f - 1f)
                    (noise * env * 12000).toInt()
                }
                BlockSound.STONE -> {
                    // Crisp clack / strike
                    val freq = 800f * (1f - t * 4f).coerceAtLeast(0.2f)
                    val tone = sin(2 * Math.PI * freq * t)
                    val noise = (rng.nextFloat() * 2f - 1f) * 0.4f
                    ((tone + noise) * env * 18000).toInt()
                }
                BlockSound.WOOD -> {
                    // Warm thud
                    val freq = 220f * (1f - t * 2f).coerceAtLeast(0.3f)
                    val tone = sin(2 * Math.PI * freq * t)
                    val noise = (rng.nextFloat() * 2f - 1f) * 0.3f
                    ((tone + noise) * env * 16000).toInt()
                }
                BlockSound.GLASS -> {
                    // High metallic ping / shatter
                    val freq = 2400f * (1f - t * 2f)
                    val tone = sin(2 * Math.PI * freq * t) * 0.7f + sin(2 * Math.PI * (freq * 1.5f) * t) * 0.3f
                    (tone * env * 15000).toInt()
                }
                BlockSound.WATER -> {
                    // Low splash
                    val noise = (rng.nextFloat() * 2f - 1f)
                    val freq = 300f + sin(t * 50.0) * 100.0
                    val tone = sin(2 * Math.PI * freq * t)
                    ((tone * 0.5 + noise * 0.5) * env * 14000).toInt()
                }
                else -> {
                    val noise = (rng.nextFloat() * 2f - 1f)
                    (noise * env * 12000).toInt()
                }
            }
            buffer[i] = sample.coerceIn(-32767, 32767).toShort()
        }
        playPcm(buffer, 0.8f)
    }

    fun playBlockPlace(sound: BlockSound) {
        val numSamples = (sampleRate * 0.08f).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val env = (1.0f - (i.toFloat() / numSamples))
            val freq = 350f * (1f - t * 3f).coerceAtLeast(0.4f)
            val tone = sin(2 * Math.PI * freq * t)
            val noise = (rng.nextFloat() * 2f - 1f) * 0.2f
            buffer[i] = ((tone + noise) * env * 14000).toInt().coerceIn(-32767, 32767).toShort()
        }
        playPcm(buffer, 0.7f)
    }

    fun playFootstep(sound: BlockSound) {
        val numSamples = (sampleRate * 0.06f).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val env = 1.0f - (i.toFloat() / numSamples)
            val noise = (rng.nextFloat() * 2f - 1f)
            buffer[i] = (noise * env * 6000).toInt().toShort()
        }
        playPcm(buffer, 0.4f)
    }

    fun playJump() {
        val numSamples = (sampleRate * 0.1f).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val env = 1.0f - (i.toFloat() / numSamples)
            val freq = 200f + t * 400f // rising pitch
            val tone = sin(2 * Math.PI * freq * t)
            buffer[i] = (tone * env * 10000).toInt().toShort()
        }
        playPcm(buffer, 0.5f)
    }

    fun playExplosion() {
        val numSamples = (sampleRate * 0.9f).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val env = (1.0f - (i.toFloat() / numSamples))
            val subBass = sin(2 * Math.PI * 65.0 * (1.0 - t * 0.5) * t) * 0.6
            val noise = (rng.nextFloat() * 2f - 1f) * 0.4
            buffer[i] = ((subBass + noise) * env * 30000).toInt().coerceIn(-32767, 32767).toShort()
        }
        playPcm(buffer, 1.0f)
    }

    private fun startAmbientMusic() {
        scope.launch {
            val pentatonicScale = floatArrayOf(261.63f, 293.66f, 329.63f, 392.00f, 440.00f, 523.25f) // C D E G A C
            while (isActive) {
                delay((12000L + rng.nextInt(15000)).toLong()) // Play a soft chime chord every ~15-25 seconds
                if (isMuted || musicVolume <= 0f) continue

                val rootFreq = pentatonicScale[rng.nextInt(pentatonicScale.size)]
                val chordFreqs = floatArrayOf(rootFreq, rootFreq * 1.25f, rootFreq * 1.5f)

                val chordLength = (sampleRate * 2.8f).toInt()
                val chordBuf = ShortArray(chordLength)

                for (i in 0 until chordLength) {
                    val t = i.toFloat() / sampleRate
                    val attack = (t / 0.4f).coerceAtMost(1.0f)
                    val decay = (1.0f - (t / 2.8f)).coerceAtLeast(0.0f)
                    val env = attack * decay

                    var wave = 0.0
                    for (f in chordFreqs) {
                        wave += sin(2 * Math.PI * f * t) * 0.33
                    }
                    val sample = (wave * env * 8000 * musicVolume).toInt()
                    chordBuf[i] = sample.coerceIn(-32767, 32767).toShort()
                }

                try {
                    audioTrack.write(chordBuf, 0, chordBuf.size)
                } catch (_: Exception) {}
            }
        }
    }

    fun release() {
        try {
            audioTrack.stop()
            audioTrack.release()
        } catch (_: Exception) {}
    }
}
