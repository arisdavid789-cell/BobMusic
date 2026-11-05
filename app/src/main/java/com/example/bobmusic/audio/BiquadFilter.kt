package com.example.bobmusic.audio.dsp

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.pow
import kotlin.math.PI

class BiquadFilter {
    private var b0 = 1f; private var b1 = 0f; private var b2 = 0f
    private var a1 = 0f; private var a2 = 0f

    // State per channel: [channel][0=x1, 1=x2, 2=y1, 3=y2]
    private var state = Array(2) { FloatArray(4) }

    fun setPeaking(sampleRate: Float, frequency: Float, gainDb: Float, q: Float) {
        val A = 10f.pow(gainDb / 40f)
        val omega = 2f * PI.toFloat() * frequency / sampleRate
        val alpha = sin(omega) / (2f * q)

        val b0n = 1f + alpha * A
        val b1n = -2f * cos(omega)
        val b2n = 1f - alpha * A
        val a0n = 1f + alpha / A
        val a1n = -2f * cos(omega)
        val a2n = 1f - alpha / A

        // Normalize
        b0 = b0n / a0n; b1 = b1n / a0n; b2 = b2n / a0n
        a1 = a1n / a0n; a2 = a2n / a0n
    }

    fun reset() {
        for (ch in state) ch.fill(0f)
    }

    fun process(sample: Float, channel: Int): Float {
        val s = state[channel]
        val x1 = s[0]; val x2 = s[1]; val y1 = s[2]; val y2 = s[3]

        val y = b0 * sample + b1 * x1 + b2 * x2 - a1 * y1 - a2 * y2

        s[1] = x1; s[0] = sample
        s[3] = y1; s[2] = y
        return y
    }
}