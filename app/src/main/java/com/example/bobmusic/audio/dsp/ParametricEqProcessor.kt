package com.example.bobmusic.audio

import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.BaseAudioProcessor
import com.example.bobmusic.audio.dsp.BiquadFilter
import java.nio.ByteBuffer
import kotlin.math.pow

class ParametricEqProcessor : BaseAudioProcessor() {

    private val filters = mutableListOf<BiquadFilter>()
    private var preampGain = 1f
    private var channelCount = 2
    private var sampleRate = 44100f
    private var eqEnabled = true
    private var supports16Bit = true

    fun setBand(index: Int, frequency: Float, gainDb: Float, q: Float) {
        while (filters.size <= index) filters.add(BiquadFilter())
        filters[index].setPeaking(sampleRate, frequency, gainDb, q)
    }

    fun setPreamp(db: Float) {
        preampGain = 10f.pow(db / 20f)
    }

    fun setEqEnabled(enabled: Boolean) {
        eqEnabled = enabled
    }

    override fun onConfigure(
        inputAudioFormat: AudioProcessor.AudioFormat
    ): AudioProcessor.AudioFormat {
        supports16Bit = inputAudioFormat.encoding == C.ENCODING_PCM_16BIT
        if (supports16Bit) {
            sampleRate = inputAudioFormat.sampleRate.toFloat()
            channelCount = inputAudioFormat.channelCount
        }
        // Return the same format — we don't change sample rate/channels
        return inputAudioFormat
    }

    override fun isActive(): Boolean {
        return super.isActive() && eqEnabled && supports16Bit
    }

    override fun queueInput(inputBuffer: ByteBuffer) {
        if (!supports16Bit) {
            // Pass-through if we can't handle the format
            val output = replaceOutputBuffer(inputBuffer.remaining())
            output.put(inputBuffer)
            output.flip()
            return
        }

        val frameCount = inputBuffer.remaining() / (2 * channelCount)
        val output = replaceOutputBuffer(inputBuffer.remaining())
        val input = inputBuffer.asShortBuffer()

        for (frame in 0 until frameCount) {
            for (ch in 0 until channelCount) {
                var sample = input.get().toFloat() / 32768f
                sample *= preampGain
                if (eqEnabled) {
                    for (filter in filters) {
                        sample = filter.process(sample, ch)
                    }
                }
                sample = sample.coerceIn(-1f, 1f)
                output.putShort((sample * 32767f).toInt().toShort())
            }
        }
        inputBuffer.position(inputBuffer.limit())
        output.flip()
    }

    override fun onReset() {
        filters.forEach { it.reset() }
    }
}