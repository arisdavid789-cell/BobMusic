package com.example.bobmusic.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bobmusic.EqualizerController
import java.util.Locale
import kotlin.math.log10
import kotlin.math.pow

@Composable
fun BeautifulEqualizer(
    controller: EqualizerController,
    modifier: Modifier = Modifier
) {
    // These variables are required for the Switch below
    var isEqEnabled by remember { mutableStateOf(true) }
    var preamp by remember { mutableFloatStateOf(controller.getPreamp()) }
    var editingBand by remember { mutableStateOf<Int?>(null) }
    var editFreq by remember { mutableFloatStateOf(1000f) }
    var editQ by remember { mutableFloatStateOf(1.41f) }

    val gains = remember { mutableStateListOf<Float>().apply {
        for (i in 0 until controller.bandCount) add(controller.getBand(i).gainDb)
    } }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Equalizer",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    controller.resetToFlat()
                    for (i in gains.indices) gains[i] = 0f
                    preamp = 0f
                }) {
                    Icon(
                        Icons.Filled.Refresh,
                        contentDescription = "Reset",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isEqEnabled,
                    onCheckedChange = {
                        isEqEnabled = it
                        controller.setEnabled(it)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF00FF07),
                        checkedBorderColor = Color.Transparent
                    )
                )
            }
        }

        // Preamp
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                "Preamp",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Slider(
                    value = preamp,
                    onValueChange = {
                        preamp = it
                        controller.setPreamp(it)
                    },
                    valueRange = -12f..12f,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    formatGain(preamp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        // Vertical band sliders
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until controller.bandCount) {
                val band = controller.getBand(i)
                BandSlider(
                    frequencyLabel = formatFreq(band.frequency),
                    gain = gains[i],
                    isEnabled = isEqEnabled,
                    onGainChange = { newGain ->
                        gains[i] = newGain
                        controller.updateBand(i, gainDb = newGain)
                    },
                    onEditTap = {
                        editFreq = band.frequency
                        editQ = band.q
                        editingBand = i
                    }
                )
            }
        }
    }

    // Frequency & Q editor
    editingBand?.let { idx ->
        val logMin = log10(20f).toFloat()
        val logMax = log10(20_000f).toFloat()

        AlertDialog(
            onDismissRequest = { editingBand = null },
            title = { Text("Band ${idx + 1}") },
            text = {
                Column {
                    Text("Frequency: ${formatFreq(editFreq)}")
                    Slider(
                        value = log10(editFreq),
                        onValueChange = { editFreq = 10f.pow(it) },
                        valueRange = logMin..logMax
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("Q: ${String.format(Locale.US, "%.2f", editQ)}")
                    Slider(
                        value = editQ,
                        onValueChange = { editQ = it },
                        valueRange = 0.1f..10f
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    controller.updateBand(idx, frequency = editFreq, q = editQ)
                    editingBand = null
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { editingBand = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun BandSlider(
    frequencyLabel: String,
    gain: Float,
    isEnabled: Boolean,
    onGainChange: (Float) -> Unit,
    onEditTap: () -> Unit
) {
    val activeColor = if (isEnabled) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(48.dp)
    ) {
        Text(
            formatGain(gain),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = activeColor
        )
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            contentAlignment = Alignment.Center
        ) {
            Slider(
                value = gain,
                onValueChange = { if (isEnabled) onGainChange(it) },
                valueRange = -12f..12f,
                enabled = isEnabled,
                modifier = Modifier
                    .requiredWidth(250.dp)
                    .requiredHeight(48.dp)
                    .rotate(-90f),
                colors = SliderDefaults.colors(
                    thumbColor = activeColor,
                    activeTrackColor = activeColor.copy(alpha = 0.6f),
                    inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                )
            )
        }
        Spacer(Modifier.height(6.dp))
        TextButton(
            onClick = onEditTap,
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.height(24.dp)
        ) {
            Text(
                frequencyLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatFreq(hz: Float): String =
    if (hz >= 1000f) String.format(Locale.US, "%.1f kHz", hz / 1000f)
    else "${hz.toInt()} Hz"

private fun formatGain(gain: Float): String {
    val sign = if (gain > 0) "+" else ""
    return "$sign${String.format(Locale.US, "%.1f", gain)}"
}