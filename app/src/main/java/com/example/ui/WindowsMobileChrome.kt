package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.BytebeatEngine
import com.example.model.GDIPayload
import com.example.viewmodel.TeraBonusUiState
import com.example.viewmodel.TeraBonusViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Authentic Windows Mobile Pocket PC Top Taskbar
 */
@Composable
fun WindowsMobileTopBar(
    uiState: TeraBonusUiState,
    onStartClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val currentTime = remember { timeFormat.format(Date()) }

    // Classic Windows Mobile gradient: light steel blue to dark royal blue
    val barGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF336699),
            Color(0xFF1E395B),
            Color(0xFF112233)
        )
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(34.dp)
            .background(barGradient)
            .border(width = 1.dp, color = Color(0xFF5588BB))
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Start Button with iconic 4-color Windows logo
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(3.dp))
                .background(
                    if (uiState.showStartMenu) Color(0xFF0F2644) else Color(0x33FFFFFF)
                )
                .border(
                    width = 1.dp,
                    color = if (uiState.showStartMenu) Color(0xFFFFCC00) else Color(0x66FFFFFF),
                    shape = RoundedCornerShape(3.dp)
                )
                .clickable(onClick = onStartClick)
                .padding(horizontal = 6.dp, vertical = 3.dp)
                .testTag("start_button"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 4-tile Windows flag
            Column(modifier = Modifier.size(11.dp)) {
                Row {
                    Box(modifier = Modifier.size(5.dp).background(Color(0xFFEE3224)))
                    Spacer(modifier = Modifier.width(1.dp))
                    Box(modifier = Modifier.size(5.dp).background(Color(0xFF00A3E0)))
                }
                Spacer(modifier = Modifier.height(1.dp))
                Row {
                    Box(modifier = Modifier.size(5.dp).background(Color(0xFF7FBA00)))
                    Spacer(modifier = Modifier.width(1.dp))
                    Box(modifier = Modifier.size(5.dp).background(Color(0xFFFFB900)))
                }
            }
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = "Start",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif
            )
        }

        // Title and Status
        Text(
            text = "Tera Bonus.exe",
            color = Color(0xFFEEEEEE),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        // System tray: Signal, Speaker, Battery, Clock, Pocket PC OK/X button
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Signal Bars
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.padding(horizontal = 2.dp)
            ) {
                for (bar in 1..4) {
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height((bar * 2.5).dp)
                            .background(Color.White)
                    )
                    Spacer(modifier = Modifier.width(1.dp))
                }
            }

            Spacer(modifier = Modifier.width(3.dp))

            // Volume Indicator
            Icon(
                imageVector = if (uiState.isMuted) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = "Audio status",
                tint = if (uiState.isMuted) Color(0xFFFF5555) else Color(0xFF66FF66),
                modifier = Modifier.size(13.dp)
            )

            Spacer(modifier = Modifier.width(3.dp))

            // Battery Icon
            Box(
                modifier = Modifier
                    .width(14.dp)
                    .height(8.dp)
                    .border(1.dp, Color.White, RoundedCornerShape(1.dp))
                    .padding(1.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .background(Color(0xFF44FF44))
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Clock
            Text(
                text = currentTime,
                color = Color.White,
                fontSize = 11.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.width(4.dp))

            // Pocket PC Round (ok) / (X) Close Button
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(Color(0x44FFFFFF))
                    .border(1.dp, Color.White, CircleShape)
                    .clickable(onClick = onCloseClick)
                    .testTag("window_close_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close window",
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

/**
 * Authentic Windows Mobile Dropdown Start Menu
 */
@Composable
fun WindowsMobileStartMenu(
    uiState: TeraBonusUiState,
    viewModel: TeraBonusViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(260.dp)
            .shadow(12.dp, RoundedCornerShape(4.dp))
            .border(1.5.dp, Color(0xFF336699), RoundedCornerShape(4.dp))
            .testTag("start_menu_card"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFECE9D8)),
        shape = RoundedCornerShape(4.dp)
    ) {
        Row {
            // Left Blue Branding Sidebar
            Box(
                modifier = Modifier
                    .width(26.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1E395B), Color(0xFF0F2644), Color(0xFF001122))
                        )
                    )
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Text(
                    text = "Windows Mobile",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                )
            }

            // Menu Items List
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(6.dp)
            ) {
                // Header status
                Text(
                    text = "TERA BONUS.EXE (RUNNING)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF003366),
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )

                HorizontalDivider(color = Color(0xFFB0B0A0), modifier = Modifier.padding(vertical = 4.dp))

                // Payloads Quick Jump
                Text(
                    text = "GDI Payloads (5-10s Loop):",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF555555),
                    modifier = Modifier.padding(horizontal = 6.dp)
                )

                GDIPayload.entries.forEach { payload ->
                    val isSelected = uiState.currentPayload == payload
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (isSelected) Color(0xFF316AC5) else Color.Transparent)
                            .clickable {
                                viewModel.selectPayload(payload)
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = payload.shortName,
                            color = if (isSelected) Color.White else Color.Black,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFFB0B0A0), modifier = Modifier.padding(vertical = 4.dp))

                // Tools & Settings
                MenuItemRow(
                    label = "Audio & Settings...",
                    onClick = { viewModel.setShowPayloadDialog(true) }
                )
                MenuItemRow(
                    label = if (uiState.showScanlines) "Disable Scanlines" else "Enable CRT Scanlines",
                    onClick = { viewModel.toggleScanlines() }
                )
                MenuItemRow(
                    label = if (uiState.isFullscreen) "Windowed Mode" else "Fullscreen GDI Only",
                    onClick = { viewModel.toggleFullscreen() }
                )
                MenuItemRow(
                    label = "About Tera Bonus.exe",
                    onClick = { viewModel.setShowAboutDialog(true) }
                )
            }
        }
    }
}

@Composable
private fun MenuItemRow(
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(2.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color.Black,
            fontSize = 12.sp,
            fontFamily = FontFamily.SansSerif
        )
    }
}

/**
 * Windows Mobile Bottom Softkey & Command Bar
 */
@Composable
fun WindowsMobileBottomBar(
    uiState: TeraBonusUiState,
    viewModel: TeraBonusViewModel,
    modifier: Modifier = Modifier
) {
    val barGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF4A6B8A),
            Color(0xFF2C4863),
            Color(0xFF1E354A)
        )
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(barGradient)
            .border(width = 1.dp, color = Color(0xFF6C8DAE))
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left Softkey: Payloads
        TextButton(
            onClick = { viewModel.setShowPayloadDialog(true) },
            colors = ButtonDefaults.textButtonColors(contentColor = Color.White),
            modifier = Modifier.testTag("softkey_payloads")
        ) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = "Payloads",
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Payloads",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif
            )
        }

        // Center Action Controls
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Next Payload
            IconButton(
                onClick = { viewModel.advanceToNextPayload() },
                modifier = Modifier.size(34.dp).testTag("button_next_payload")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Next Payload",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Play / Pause
            IconButton(
                onClick = { viewModel.togglePlayPause() },
                modifier = Modifier.size(34.dp).testTag("button_play_pause")
            ) {
                Icon(
                    imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                    tint = Color(0xFF66FF66),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Mute / Unmute
            IconButton(
                onClick = { viewModel.toggleMute() },
                modifier = Modifier.size(34.dp).testTag("button_mute_toggle")
            ) {
                Icon(
                    imageVector = if (uiState.isMuted) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Mute",
                    tint = if (uiState.isMuted) Color(0xFFFF6666) else Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Scanlines Toggle
            IconButton(
                onClick = { viewModel.toggleScanlines() },
                modifier = Modifier.size(34.dp).testTag("button_toggle_scanlines")
            ) {
                Icon(
                    imageVector = Icons.Default.Tv,
                    contentDescription = "Scanlines",
                    tint = if (uiState.showScanlines) Color(0xFFFFCC00) else Color(0x88FFFFFF),
                    modifier = Modifier.size(18.dp)
                )
            }

            // Fullscreen GDI
            IconButton(
                onClick = { viewModel.toggleFullscreen() },
                modifier = Modifier.size(34.dp).testTag("button_toggle_fullscreen")
            ) {
                Icon(
                    imageVector = if (uiState.isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                    contentDescription = "Fullscreen",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Right Softkey: Menu / Info
        TextButton(
            onClick = { viewModel.setShowAboutDialog(true) },
            colors = ButtonDefaults.textButtonColors(contentColor = Color.White),
            modifier = Modifier.testTag("softkey_about")
        ) {
            Text(
                text = "About",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "About",
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Real-time HUD overlay on top of the GDI canvas
 */
@Composable
fun GDITelemetryHUD(
    uiState: TeraBonusUiState,
    waveform: FloatArray,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        // Payload Progress Bar
        LinearProgressIndicator(
            progress = { uiState.payloadProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = Color(0xFF00FFCC),
            trackColor = Color(0x33000000)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xCC050E18), RoundedCornerShape(4.dp))
                .border(1.dp, Color(0xFF1E395B), RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${uiState.currentPayload.title} [${uiState.payloadDurationSec}s Loop]",
                    color = Color(0xFF00FFCC),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "FORMULA: ${uiState.currentSound.formulaString}",
                    color = Color(0xFFFFFF33),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "8kHz · 8-bit PCM",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "t = ${uiState.currentSampleT}",
                    color = Color(0xFF88CCFF),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

/**
 * Settings & Payload Configuration Dialog
 */
@Composable
fun PayloadsAndSettingsDialog(
    uiState: TeraBonusUiState,
    viewModel: TeraBonusViewModel,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Tera Bonus.exe Configuration",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column {
                Text(
                    text = "GDI Payloads (Looping every 5 - 10s):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(4.dp))

                GDIPayload.entries.forEach { payload ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectPayload(payload) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(
                                    if (uiState.currentPayload == payload) Color(0xFF00A3E0) else Color.Gray,
                                    CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = payload.title,
                            fontSize = 12.sp,
                            fontWeight = if (uiState.currentPayload == payload) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Payload Duration Slider (5 - 10 seconds)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Payload Cycle Duration:", fontSize = 12.sp)
                    Text(
                        text = "${uiState.payloadDurationSec} seconds",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
                Slider(
                    value = uiState.payloadDurationSec.toFloat(),
                    onValueChange = { viewModel.setPayloadDurationSec(it.toInt()) },
                    valueRange = 5f..10f,
                    steps = 4,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF0066CC),
                        activeTrackColor = Color(0xFF0066CC)
                    )
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Sound Selector
                Text(text = "Bytebeat Sound Formula Override:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                BytebeatEngine.SoundFormula.entries.forEach { formula ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectSound(formula) }
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    if (uiState.currentSound == formula) Color(0xFF00CC66) else Color.LightGray,
                                    CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${formula.displayName}: ${formula.formulaString}",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (uiState.currentSound == formula) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Volume Control
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Master 8-bit Volume:", fontSize = 12.sp)
                    Text(
                        text = "${(uiState.volume * 100).toInt()}%",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
                Slider(
                    value = uiState.volume,
                    onValueChange = { viewModel.setVolume(it) },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF0066CC),
                        activeTrackColor = Color(0xFF0066CC)
                    )
                )

                // Auto-loop toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Seamless Background Loop", fontSize = 12.sp)
                    Switch(
                        checked = uiState.isAutoLoop,
                        onCheckedChange = { viewModel.toggleAutoLoop() },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF0066CC))
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

/**
 * Authentic Windows Mobile "About" Dialog
 */
@Composable
fun AboutTeraBonusDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "About Tera Bonus.exe",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Tera Bonus.exe - Mobile Edition (GDI Only)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF003366)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Designed for Windows Mobile Pocket PC environments using legacy GDI graphics raster operations.",
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Procedural Audio Engine:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Text(
                    text = "• 8-bit mono output at 8kHz sampling rate\n" +
                            "• Sound 1: t*(t>>8)*t>>16&63|t>>4)\n" +
                            "• Sound 2: t&t>>7|t&t>>8\n" +
                            "• Sound 3: t*(t&t>>12)/256\n" +
                            "• Sound 4: t&t>>8+t*(t)\n" +
                            "• Sound 5: t%((t&-16|t>>11)&42)<<2|t>>4\n" +
                            "• Sound Finally: t*(t>>8)>>(t>>6)",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "GDI Payloads (5 - 10s seamless loop):\n" +
                            "1. Chaotic glitch-heavy drone & chirps\n" +
                            "2. Swirl & Movings and swirls\n" +
                            "3. Big bounce circles drawing persistent trails\n" +
                            "4. TextOut says: Tera Bonus.apk\n" +
                            "5. Random rectangles to Size and PX\n" +
                            "6. Bouncing Triangles to Move PX (Mocq / Coore32)\n" +
                            "7. Lines Curvy Effect to Random Lines",
                    fontSize = 11.sp
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}
