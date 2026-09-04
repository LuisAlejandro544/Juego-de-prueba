package com.example.swat.engine

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Random
import kotlin.math.sin

object TacticalAudio {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val random = Random()
    private const val SAMPLE_RATE = 22050

    private fun playPcm(buffer: ShortArray) {
        scope.launch {
            try {
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(buffer, 0, buffer.size)
                track.play()
                // Let track play out then release
                kotlinx.coroutines.delay((buffer.size.toLong() * 1000L / SAMPLE_RATE) + 50L)
                track.stop()
                track.release()
            } catch (_: Exception) {
                // Audio failure graceful fallback
            }
        }
    }

    fun playGlockShot() {
        val numSamples = (SAMPLE_RATE * 0.08).toInt() // 80ms
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val decay = (1f - progress) * (1f - progress)
            val noise = (random.nextFloat() * 2f - 1f)
            val pop = sin(2.0 * Math.PI * 180.0 * (1.0 - progress) * i / SAMPLE_RATE).toFloat()
            val sample = ((noise * 0.6f + pop * 0.4f) * decay * 28000f).toInt()
            buffer[i] = sample.coerceIn(-32768, 32767).toShort()
        }
        playPcm(buffer)
    }

    fun playRifleShot() {
        val numSamples = (SAMPLE_RATE * 0.12).toInt() // 120ms
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val decay = (1f - progress)
            val noise = (random.nextFloat() * 2f - 1f)
            val thud = sin(2.0 * Math.PI * 90.0 * i / SAMPLE_RATE).toFloat()
            val sample = ((noise * 0.7f + thud * 0.5f) * decay * 30000f).toInt()
            buffer[i] = sample.coerceIn(-32768, 32767).toShort()
        }
        playPcm(buffer)
    }

    fun playEnemyShot() {
        val numSamples = (SAMPLE_RATE * 0.09).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val decay = (1f - progress) * (1f - progress)
            val noise = (random.nextFloat() * 2f - 1f)
            val sample = (noise * decay * 24000f).toInt()
            buffer[i] = sample.coerceIn(-32768, 32767).toShort()
        }
        playPcm(buffer)
    }

    fun playReloadSound() {
        val numSamples = (SAMPLE_RATE * 0.25).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            var sample = 0f
            // Click 1 (mag drop/insert at 0.05s)
            if (t in 0.04f..0.08f) {
                val decay = 1f - ((t - 0.04f) / 0.04f)
                sample += (random.nextFloat() * 2f - 1f) * decay * 0.8f
            }
            // Click 2 (bolt cock at 0.18s)
            if (t in 0.16f..0.21f) {
                val decay = 1f - ((t - 0.16f) / 0.05f)
                sample += (random.nextFloat() * 2f - 1f) * decay * 0.9f
            }
            buffer[i] = (sample * 25000f).toInt().coerceIn(-32768, 32767).toShort()
        }
        playPcm(buffer)
    }

    fun playDoorSound() {
        val numSamples = (SAMPLE_RATE * 0.15).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val decay = (1f - progress)
            val creak = sin(2.0 * Math.PI * (120.0 + progress * 80.0) * i / SAMPLE_RATE).toFloat()
            val sample = ((creak * 0.5f + (random.nextFloat() * 0.3f)) * decay * 20000f).toInt()
            buffer[i] = sample.coerceIn(-32768, 32767).toShort()
        }
        playPcm(buffer)
    }

    fun playRadioPing() {
        val numSamples = (SAMPLE_RATE * 0.07).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val decay = 1f - progress
            val tone = sin(2.0 * Math.PI * 1200.0 * i / SAMPLE_RATE).toFloat()
            buffer[i] = (tone * decay * 18000f).toInt().coerceIn(-32768, 32767).toShort()
        }
        playPcm(buffer)
    }
}
