package com.example

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.model.*
import com.example.ui.screens.FafiFederationScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SquadScreen
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class StoreScreenshotsTest {

  @get:Rule val composeTestRule = createComposeRule()

  private fun getMockClub(): Club {
    val squad = mutableListOf(
      Player(
        firstName = "Luis",
        lastName = "Sosa",
        age = 24,
        country = "Venezuela",
        position = Position.MID,
        traits = listOf(Trait.LIDER),
        attributes = PlayerAttributes(75, 70, 85, 80, 80, 0, 85, 75),
        marketValue = 5_000_000L,
        salary = 15_000L,
        contractYears = 3
      ),
      Player(
        firstName = "Diego",
        lastName = "Maradona",
        age = 28,
        country = "Argentina",
        position = Position.ATT,
        traits = listOf(Trait.TECNICO),
        attributes = PlayerAttributes(95, 45, 90, 88, 85, 0, 92, 70),
        marketValue = 85_000_000L,
        salary = 120_000L,
        contractYears = 4
      ),
      Player(
        firstName = "Lev",
        lastName = "Yashin",
        age = 30,
        country = "Rusia",
        position = Position.GK,
        traits = listOf(Trait.MURO),
        attributes = PlayerAttributes(0, 0, 0, 75, 80, 96, 95, 85),
        marketValue = 40_000_000L,
        salary = 65_000L,
        contractYears = 2
      )
    )

    return Club(
      name = "Fafi Real Madrid",
      country = "España",
      budget = 150_000_000L,
      wageBudget = 2_500_000L,
      fanBaseSize = 12_000_000L,
      stadiumCapacity = 81044,
      ticketPrice = 45,
      trainingFacilities = 5,
      youthAcademy = 5,
      squad = squad
    )
  }

  @Test
  fun capture_store_squad_screen() {
    val club = getMockClub()
    val selectedPlayer = club.squad.first()

    composeTestRule.setContent {
      MyApplicationTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          androidx.compose.foundation.layout.Box(modifier = Modifier.padding(innerPadding)) {
            SquadScreen(
              club = club,
              selectedPlayer = selectedPlayer,
              onPlayerClick = {}
            )
          }
        }
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/store_squad.png")
  }

  @Test
  fun capture_store_federation_screen() {
    val fafi = FAFI(
      president = President(name = "Gianni Infantino", trait = Fafitrait.AMBICIOSO),
      worldCupYear = 2026,
      worldCupHost = "México, USA & Canadá",
      taxRate = 12,
      politicalFavor = 50,
      isWorldCupYear = true
    )
    val manager = Manager(
      name = "Luis Sosa",
      personalWealth = 150_000L,
      currentClubId = "real_madrid",
      currentClubName = "Fafi Real Madrid",
      politicalAffiliation = "EXPANSIVO",
      isSummoned = true
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          androidx.compose.foundation.layout.Box(modifier = Modifier.padding(innerPadding)) {
            FafiFederationScreen(
              fafi = fafi,
              manager = manager,
              onPRCampaignClick = {},
              onAcceptSummonClick = {},
              onResignSummonClick = {}
            )
          }
        }
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/store_federation.png")
  }
}
