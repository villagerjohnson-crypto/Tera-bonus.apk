package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gdi.GDIRenderer
import com.example.ui.AboutTeraBonusDialog
import com.example.ui.GDITelemetryHUD
import com.example.ui.PayloadsAndSettingsDialog
import com.example.ui.WindowsMobileBottomBar
import com.example.ui.WindowsMobileStartMenu
import com.example.ui.WindowsMobileTopBar
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.TeraBonusViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                TeraBonusApp()
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@Composable
fun TeraBonusApp(viewModel: TeraBonusViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val gdiRenderer = remember { GDIRenderer() }

    // Compose idiomatic 60fps infinite clock
    val infiniteTransition = rememberInfiniteTransition(label = "gdi_clock")
    val animTimeFloat by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 60000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gdi_time_ms"
    )
    val animationTimeMs = animTimeFloat.toLong()

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        color = Color(0xFF000000)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .testTag("app_root_container")
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Windows Mobile Top Taskbar (hidden in fullscreen mode for pure GDI immersion)
                AnimatedVisibility(
                    visible = !uiState.isFullscreen,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    WindowsMobileTopBar(
                        uiState = uiState,
                        onStartClick = { viewModel.toggleStartMenu() },
                        onCloseClick = { viewModel.toggleFullscreen() }
                    )
                }

                // Window Header (Windows Mobile Style)
                AnimatedVisibility(
                    visible = !uiState.isFullscreen,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    WindowHeader(
                        title = "Tera Bonus.exe - [GDI Only Mobile Edition]",
                        subtitle = "${uiState.currentPayload.title} (${uiState.payloadDurationSec}s Loop)",
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Main GDI Canvas Stage
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color.Black)
                        .clickable {
                            viewModel.dismissStartMenu()
                        }
                        .testTag("gdi_canvas_container")
                ) {
                    // Custom GDI Graphics Canvas Rendering Loop
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("gdi_canvas")
                    ) {
                        gdiRenderer.render(
                            drawScope = this,
                            payload = uiState.currentPayload,
                            elapsedTimeMs = animationTimeMs,
                            audioWaveform = viewModel.liveWaveform,
                            showScanlines = uiState.showScanlines
                        )
                    }

                    // Real-time Telemetry HUD (Formula, sample index, loop timer)
                    if (uiState.showTelemetry) {
                        GDITelemetryHUD(
                            uiState = uiState,
                            waveform = viewModel.liveWaveform,
                            modifier = Modifier.align(Alignment.TopCenter)
                        )
                    }
                }

                // Windows Mobile Bottom Softkey Command Bar
                AnimatedVisibility(
                    visible = !uiState.isFullscreen,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    WindowsMobileBottomBar(
                        uiState = uiState,
                        viewModel = viewModel
                    )
                }
            }

            // Dropdown Start Menu (Windows Mobile Pocket PC style)
            if (uiState.showStartMenu && !uiState.isFullscreen) {
                WindowsMobileStartMenu(
                    uiState = uiState,
                    viewModel = viewModel,
                    onDismiss = { viewModel.dismissStartMenu() },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 34.dp, start = 4.dp)
                )
            }

            // Floating Exit Fullscreen Button
            if (uiState.isFullscreen) {
                FloatingActionButton(
                    onClick = { viewModel.toggleFullscreen() },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .testTag("button_exit_fullscreen"),
                    containerColor = Color(0xCC1E395B),
                    contentColor = Color.White
                ) {
                    Icon(
                        imageVector = Icons.Default.FullscreenExit,
                        contentDescription = "Exit Fullscreen"
                    )
                }
            }

            // Dialogs
            if (uiState.showPayloadDialog) {
                PayloadsAndSettingsDialog(
                    uiState = uiState,
                    viewModel = viewModel,
                    onDismiss = { viewModel.setShowPayloadDialog(false) }
                )
            }

            if (uiState.showAboutDialog) {
                AboutTeraBonusDialog(
                    onDismiss = { viewModel.setShowAboutDialog(false) }
                )
            }
        }
    }
}

/**
 * Windows Mobile window caption / titlebar
 */
@Composable
fun WindowHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    val headerGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF003366),
            Color(0xFF1E5B99),
            Color(0xFF002244)
        )
    )

    Row(
        modifier = modifier
            .background(headerGradient)
            .border(width = 1.dp, color = Color(0xFF4488CC))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App icon dot
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(Color(0xFF00FFCC))
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(
                text = title,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = subtitle,
                color = Color(0xFFB0D0FF),
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
