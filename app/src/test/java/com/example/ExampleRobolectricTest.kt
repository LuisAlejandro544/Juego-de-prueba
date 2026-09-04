package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.swat.engine.FieldOfView
import com.example.swat.model.Door
import com.example.swat.model.DoorOrientation
import com.example.swat.model.Operator
import com.example.swat.model.Vector2D
import com.example.swat.model.Wall
import com.example.swat.model.Weapon
import com.example.swat.model.WeaponType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    assertEquals("Tactical SWAT", appName)
  }

  @Test
  fun `operator weapon switch between Glock and Fusil`() {
    val op = Operator(
      id = "op_1",
      callsign = "Bravo-1",
      role = "Assault",
      position = Vector2D(0f, 0f),
      inventory = listOf(Weapon.createRifle(), Weapon.createGlock()),
      equippedWeaponIndex = 0
    )

    assertEquals(WeaponType.RIFLE, op.equippedWeapon.type)
    assertEquals(30, op.equippedWeapon.currentAmmo)

    val switched = op.switchWeapon(1)
    assertEquals(WeaponType.GLOCK, switched.equippedWeapon.type)
    assertEquals(17, switched.equippedWeapon.currentAmmo)
  }

  @Test
  fun `door blocks and unblocks line of sight`() {
    val door = Door(
      id = "door_1",
      name = "Test Door",
      center = Vector2D(100f, 100f),
      length = 60f,
      orientation = DoorOrientation.VERTICAL,
      isOpen = false
    )

    val origin = Vector2D(50f, 100f)
    val target = Vector2D(150f, 100f)

    // Closed door blocks sight
    assertFalse(FieldOfView.hasLineOfSight(origin, target, emptyList(), listOf(door)))

    // Open door allows sight
    val openDoor = door.copy(isOpen = true)
    assertTrue(FieldOfView.hasLineOfSight(origin, target, emptyList(), listOf(openDoor)))
  }

  @Test
  fun `native engine bridge is available and has safe fallback`() {
    val info = com.example.swat.engine.NativeEngineBridge.safeGetInfo()
    assertTrue(info.isNotEmpty())
  }
}
