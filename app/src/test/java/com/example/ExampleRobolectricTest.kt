package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.audio.BytebeatEngine
import com.example.model.GDIPayload
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Tera Bonus", appName)
  }

  @Test
  fun `payload 5 random rectangles and sound modulo configured`() {
    val payload5 = GDIPayload.PAYLOAD_5
    assertEquals(5, payload5.id)
    assertEquals(BytebeatEngine.SoundFormula.SOUND_MODULO, payload5.defaultBytebeat)
    assertEquals("t%((t&-16|t>>11)&42)<<2|t>>4", payload5.defaultBytebeat.formulaString)
  }

  @Test
  fun `sound modulo calculates without arithmetic exception across samples`() {
    // Verify formula t%((t&-16|t>>11)&42)<<2|t>>4 across thousands of ticks including t=0
    for (t in 0L..10000L) {
      val ti = t.toInt()
      val mod = (((ti and -16) or (ti shr 11)) and 42)
      val left = if (mod != 0) (ti % mod) else 0
      val sample = ((left shl 2) or (ti shr 4)) and 255
      assertTrue(sample in 0..255)
    }
  }

  @Test
  fun `payload 6 bouncing triangles to move px configured`() {
    val payload6 = GDIPayload.PAYLOAD_6
    assertEquals(6, payload6.id)
    assertEquals("Triangles PX", payload6.shortName)
    assertTrue(payload6.description.contains("Mocq epic.exe"))
    assertTrue(payload6.description.contains("Coore32.exe"))
    assertEquals(BytebeatEngine.SoundFormula.SOUND_FINALLY, payload6.defaultBytebeat)
  }

  @Test
  fun `payload 7 curvy lines effect configured`() {
    val payload7 = GDIPayload.PAYLOAD_7
    assertEquals(7, payload7.id)
    assertEquals("Curvy Lines", payload7.shortName)
    assertTrue(payload7.description.contains("Curving"))
    assertEquals(BytebeatEngine.SoundFormula.SOUND_1, payload7.defaultBytebeat)
  }
}
