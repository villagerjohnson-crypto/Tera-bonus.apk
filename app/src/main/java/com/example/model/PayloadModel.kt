package com.example.model

import com.example.audio.BytebeatEngine

enum class GDIPayload(
    val id: Int,
    val title: String,
    val shortName: String,
    val description: String,
    val defaultBytebeat: BytebeatEngine.SoundFormula
) {
    PAYLOAD_1(
        1,
        "Payload 1: Chaotic Glitch Drone",
        "Glitch Drone",
        "Chaotic glitch-heavy drone with randomized high-frequency chirps, screen tearing & BitBlt shred",
        BytebeatEngine.SoundFormula.SOUND_1
    ),
    PAYLOAD_2(
        2,
        "Payload 2: Swirl & Movings",
        "Swirl",
        "Moving vortex tunnels, Archimedean logarithmic spirals, and rotating GDI color distortion",
        BytebeatEngine.SoundFormula.SOUND_2
    ),
    PAYLOAD_3(
        3,
        "Payload 3: Big Bounce Circles",
        "Bounce Circles",
        "Large bouncing GDI circles with collision physics drawing persistent trails across canvas",
        BytebeatEngine.SoundFormula.SOUND_3
    ),
    PAYLOAD_4(
        4,
        "Payload 4: TextOut Tera Bonus.apk",
        "TextOut.apk",
        "Legacy Windows GDI TextOut API simulation drawing 'Tera Bonus.apk' across screen",
        BytebeatEngine.SoundFormula.SOUND_4
    ),
    PAYLOAD_5(
        5,
        "Payload 5: Random Rectangles to Size and PX",
        "Rectangles PX",
        "Random GDI rectangles with varying pixel size, coordinates (PX), PatBlt patterns, and raster inversions",
        BytebeatEngine.SoundFormula.SOUND_MODULO
    ),
    PAYLOAD_6(
        6,
        "Payload 6: Bouncing Triangles to Move PX (Mocq / Coore32)",
        "Triangles PX",
        "Bouncing GDI polygons & triangles with MoveToEx/LineTo kinematics, PX delta telemetry & trails (like Mocq epic.exe and Coore32.exe)",
        BytebeatEngine.SoundFormula.SOUND_FINALLY
    ),
    PAYLOAD_7(
        7,
        "Payload 7: Curvy Lines & Random Lines Effect",
        "Curvy Lines",
        "Curving Bézier and oscillating Sine spline lines with randomized GDI Pen stroke colors and chromatic vibration",
        BytebeatEngine.SoundFormula.SOUND_1
    )
}
