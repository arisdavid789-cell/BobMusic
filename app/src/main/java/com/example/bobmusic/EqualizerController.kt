package com.example.bobmusic

import com.example.bobmusic.audio.ParametricEqProcessor

class EqualizerController(private val processor: ParametricEqProcessor) {

    data class Band(val frequency: Float, val gainDb: Float, val q: Float = 1.41f)

    // Fixed 11-band layout matching your spec
    private val fixedFrequencies = listOf(
        40f, 65f, 125f, 250f, 500f,
        1000f, 2400f, 4800f, 8000f, 12000f, 16000f
    )

    val bandCount: Int = fixedFrequencies.size

    private val bands = mutableListOf<Band>()
    private var preampDb = 0f
    private var enabled = true

    init {
        fixedFrequencies.forEachIndexed { i, freq ->
            val band = Band(freq, 0f)
            bands.add(band)
            processor.setBand(i, band.frequency, band.gainDb, band.q)
        }
    }

    fun getBand(index: Int): Band = bands[index]

    fun updateBand(index: Int, frequency: Float? = null, gainDb: Float? = null, q: Float? = null) {
        if (index !in bands.indices) return
        val current = bands[index]
        val updated = current.copy(
            frequency = frequency ?: current.frequency,
            gainDb = gainDb ?: current.gainDb,
            q = q ?: current.q
        )
        bands[index] = updated
        processor.setBand(index, updated.frequency, updated.gainDb, updated.q)
    }

    fun setPreamp(db: Float) {
        preampDb = db.coerceIn(-12f, 12f)
        processor.setPreamp(preampDb)
    }

    fun getPreamp(): Float = preampDb

    fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
        processor.setEqEnabled(enabled)
    }

    fun resetToFlat() {
        bands.indices.forEach { updateBand(it, gainDb = 0f) }
        setPreamp(0f)
    }
}