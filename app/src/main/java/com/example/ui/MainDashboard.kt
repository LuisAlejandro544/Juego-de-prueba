package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.example.engine.GameEngine
import com.example.model.*
import com.example.ui.components.HeaderBar
import com.example.ui.components.NavigationSidebar
import com.example.ui.screens.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun MainDashboard(
    engine: GameEngine,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    
    // UI state collected safely from engine flow state
    val countries by engine.countries.collectAsState()
    val ligas by engine.ligas.collectAsState()
    val clubs by engine.clubs.collectAsState()
    val manager by engine.manager.collectAsState()
    val fafi by engine.fafi.collectAsState()
    val socialFeed by engine.socialFeed.collectAsState()
    val currentLiveMatch by engine.currentLiveMatch.collectAsState()
    val isSimulating by engine.isSimulating.collectAsState()
    val newsLog by engine.newsLog.collectAsState()
    val currentDate by engine.currentDate.collectAsState()

    var activeTab by remember { mutableStateOf<DashboardTab>(DashboardTab.ClubInfo) }
    var selectedPlayerForDetail by remember { mutableStateOf<Player?>(null) }

    // Derived states to prevent unnecessary heavy UI calculations
    val managerClub by remember(clubs, manager) {
        derivedStateOf { clubs.firstOrNull { it.id == manager.currentClubId } }
    }
    val currentLeague by remember(ligas, manager) {
        derivedStateOf { ligas.firstOrNull { it.clubs.any { club -> club.id == manager.currentClubId } } }
    }

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(PitchDarkBg)
    ) {
        // 1. LEFT NAVIGATION PANEL (Compact sidebar navigation optimized for landscape)
        NavigationSidebar(
            activeTab = activeTab,
            onTabSelected = { 
                activeTab = it 
                selectedPlayerForDetail = null
            },
            modifier = Modifier
                .width(96.dp)
                .fillMaxHeight()
                .background(SurfaceCarbon)
                .drawBehind {
                    drawLine(
                        color = DarkSteel,
                        start = Offset(size.width, 0f),
                        end = Offset(size.width, size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                }
        )

        // 2. MAIN DETAILS & PANELS (Right side content area)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(16.dp)
        ) {
            // Header Stats bar
            HeaderBar(
                manager = manager,
                club = managerClub,
                isSimulating = isSimulating,
                currentDate = currentDate,
                onSimulateClick = {
                    coroutineScope.launch {
                        engine.advanceRound()
                        // If our active club has a match in the active round, load its detailed live events
                        val activeClubId = manager.currentClubId
                        if (activeClubId != null) {
                            val activeLg = currentLeague
                            if (activeLg != null) {
                                val currentFixtures = activeLg.fixtures.getOrNull(activeLg.currentRound - 1)
                                val clubMatch = currentFixtures?.matches?.firstOrNull { 
                                    it.homeClubId == activeClubId || it.awayClubId == activeClubId 
                                }
                                if (clubMatch != null) {
                                    // Set live match ticker view active
                                    engine.advanceWeekRecovery()
                                    activeTab = DashboardTab.LiveMatch
                                }
                            }
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Main Dynamic Board
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (activeTab) {
                    DashboardTab.ClubInfo -> {
                        ClubAndStandingsScreen(
                            club = managerClub,
                            league = currentLeague,
                            newsLog = newsLog
                        )
                    }
                    DashboardTab.Squad -> {
                        SquadScreen(
                            club = managerClub,
                            selectedPlayer = selectedPlayerForDetail,
                            onPlayerClick = { selectedPlayerForDetail = it }
                        )
                    }
                    DashboardTab.Calendar -> {
                        CalendarScreen(
                            currentDate = currentDate,
                            manager = manager,
                            ligas = ligas,
                            clubs = clubs
                        )
                    }
                    DashboardTab.LiveMatch -> {
                        LiveMatchTickerScreen(
                            league = currentLeague,
                            managerClubId = manager.currentClubId
                        )
                    }
                    DashboardTab.Social -> {
                        SocialFeedScreen(
                            posts = socialFeed,
                            onDecisionTaken = { postId, choiceIndex ->
                                engine.handleSocialFeedDecision(postId, choiceIndex)
                            }
                        )
                    }
                    DashboardTab.ManagerCareer -> {
                        ManagerCareerScreen(
                            manager = manager,
                            onPurchaseLicense = { licenseType ->
                                engine.purchaseLicense(licenseType)
                            },
                            onHireAgent = {
                                engine.hirePrivateAgent()
                            }
                        )
                    }
                    DashboardTab.FafiFederation -> {
                        FafiFederationScreen(
                            fafi = fafi,
                            manager = manager,
                            onPRCampaignClick = { engine.investInPRCampaign() },
                            onAcceptSummonClick = { engine.acceptNationalSummon() },
                            onResignSummonClick = { engine.resignNationalSummon() }
                        )
                    }
                }
            }
        }
    }
}
