package com.example.engine

import android.content.Context
import android.util.Log
import com.example.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import kotlin.random.Random

class GameEngine(private val context: Context) {

    private val storage = GameStorage(context)
    private val scope = CoroutineScope(Dispatchers.IO)

    // Calendar State
    private val _currentDate = MutableStateFlow(LocalDate.of(2025, 1, 1))
    val currentDate: StateFlow<LocalDate> = _currentDate

    // Game state observables
    private val _countries = MutableStateFlow<List<Country>>(emptyList())
    val countries: StateFlow<List<Country>> = _countries

    private val _ligas = MutableStateFlow<List<League>>(emptyList())
    val ligas: StateFlow<List<League>> = _ligas

    private val _clubs = MutableStateFlow<List<Club>>(emptyList())
    val clubs: StateFlow<List<Club>> = _clubs

    private val _manager = MutableStateFlow(Manager("Mánager Anónimo"))
    val manager: StateFlow<Manager> = _manager

    private val _fafi = MutableStateFlow(FAFI.createDefault())
    val fafi: StateFlow<FAFI> = _fafi

    private val _socialFeed = MutableStateFlow<List<SocialPost>>(emptyList())
    val socialFeed: StateFlow<List<SocialPost>> = _socialFeed

    private val _currentLiveMatch = MutableStateFlow<Match?>(null)
    val currentLiveMatch: StateFlow<Match?> = _currentLiveMatch

    private val _isSimulating = MutableStateFlow(false)
    val isSimulating: StateFlow<Boolean> = _isSimulating

    private val _newsLog = MutableStateFlow<List<String>>(emptyList())
    val newsLog: StateFlow<List<String>> = _newsLog

    private val _isOnboardingFinished = MutableStateFlow(false)
    val isOnboardingFinished: StateFlow<Boolean> = _isOnboardingFinished

    // Sub-systems modular managers
    private val careerManager = CareerManager(
        managerFlow = _manager,
        clubsFlow = _clubs,
        storage = storage,
        scope = scope,
        addNews = { addNews(it) }
    )

    private val socialFeedManager = SocialFeedManager(
        socialFeedFlow = _socialFeed,
        managerFlow = _manager,
        clubsFlow = _clubs,
        addNews = { addNews(it) }
    )

    // Initialize procedural universe
    suspend fun initializeUniverse(managerName: String) = withContext(Dispatchers.Default) {
        _isSimulating.value = true
        _isOnboardingFinished.value = false
        addNews("Inicializando generación de universo de fútbol procedural...")

        // Clear previous state
        storage.clearAll()

        // Generate countries (Latin America + France)
        val generatedCountries = Country.generateUniverse()
        _countries.value = generatedCountries

        val generatedLigas = mutableListOf<League>()
        val allClubs = mutableListOf<Club>()

        // Generate 6 clubs for each country
        generatedCountries.forEachIndexed { countryIndex, country ->
            val clubList = mutableListOf<Club>()
            addNews("Generando estructura deportiva para ${country.name}...")

            // 6 clubs per country
            repeat(6) { clubIndex ->
                val club = Club.generateProcedural(country, clubIndex, minStars = 2, maxStars = 4)
                clubList.add(club)
                allClubs.add(club)
            }

            val leagueName = "Liga Profesional de ${country.name}"
            val league = League(
                name = leagueName,
                country = country.name,
                visibility = LeagueVisibility.ZERO_DETAIL, // Set default detail to zero; only active selected league gets MAX_DETAIL
                clubs = clubList
            )
            league.generateSchedule()
            generatedLigas.add(league)
        }

        _ligas.value = generatedLigas
        _clubs.value = allClubs

        // Initial placeholder manager
        _manager.value = Manager(name = managerName, personalWealth = 12_000L)

        _isSimulating.value = false
        addNews("¡Universo procedural generado exitosamente! Selecciona tu equipo o crea uno desde cero.")
    }

    // Connects manager to an existing club and starts career
    suspend fun startCareerWithSelectedClub(managerName: String, selectedClubId: String) = withContext(Dispatchers.Default) {
        _isSimulating.value = true
        addNews("Asignando cargo directivo al mánager...")

        val allClubs = _clubs.value
        val selectedClub = allClubs.firstOrNull { it.id == selectedClubId }
        if (selectedClub != null) {
            val mgr = Manager(
                name = managerName,
                personalWealth = 15_000L,
                currentClubId = selectedClub.id,
                currentClubName = selectedClub.name
            )
            _manager.value = mgr

            // Set chosen country's league as MAX_DETAIL
            _ligas.value.forEach { league ->
                if (league.country == selectedClub.country) {
                    league.visibility = LeagueVisibility.MAX_DETAIL
                } else {
                    league.visibility = LeagueVisibility.ZERO_DETAIL
                }
            }

            // Generate initial Social Feed
            val posts = mutableListOf<SocialPost>()
            posts.add(SocialPost(handle = "@FEDEBOL_Oficial", authorName = "FEDEBOL", content = "Bienvenidos a una nueva temporada del fútbol profesional latinoamericano bajo la regulación de FEDEBOL. Juego Limpio ante todo.", likes = 1200, reposts = 340, timeAgo = "1h"))
            posts.add(SocialPost(handle = "@Hinchas_${selectedClub.name.replace(" ", "")}", authorName = "Fans", content = "¡Bienvenido nuestro nuevo director técnico ${mgr.name}! Esperamos grandes resultados.", likes = 750, reposts = 220, timeAgo = "5m"))
            _socialFeed.value = posts

            // Save batch to disk
            addNews("Guardando estado del universo deportivo en disco seguro...")
            _currentDate.value = LocalDate.of(2025, 1, 1)
            storage.saveManager(mgr)
            storage.saveCalendarDate("2025-01-01")
            storage.saveClubsBatch(allClubs)
            storage.saveLigasBatch(_ligas.value)
            storage.savePlayersBatch(allClubs.flatMap { it.squad })

            _isOnboardingFinished.value = true
        }
        _isSimulating.value = false
    }

    // Creates a custom club from scratch, replaces the last club in that country, re-schedules, and starts career
    suspend fun startCareerWithCustomClub(
        managerName: String,
        customClubName: String,
        countryName: String,
        stadiumCapacity: Int,
        budget: Long
    ) = withContext(Dispatchers.Default) {
        _isSimulating.value = true
        addNews("Fundando club deportivo: ${customClubName}...")

        val random = Random
        val starPower = 3
        val baseWage = (starPower * 90_000L).toLong()
        val fanBase = (stadiumCapacity * 1.8).toLong()
        val ticketPrice = 12

        val customClub = Club(
            name = customClubName,
            country = countryName,
            budget = budget,
            wageBudget = baseWage,
            stadiumCapacity = stadiumCapacity,
            fanBaseSize = fanBase,
            ticketPrice = ticketPrice,
            trainingFacilities = starPower,
            youthAcademy = starPower
        )

        // Generate squad of 18 players
        val minRating = 45
        val maxRating = 72
        repeat(2) { customClub.squad.add(Player.generateProcedural(countryName, Position.GK, minRating, maxRating)) }
        repeat(6) { customClub.squad.add(Player.generateProcedural(countryName, Position.DEF, minRating, maxRating)) }
        repeat(6) { customClub.squad.add(Player.generateProcedural(countryName, Position.MID, minRating, maxRating)) }
        repeat(4) { customClub.squad.add(Player.generateProcedural(countryName, Position.ATT, minRating, maxRating)) }

        // Find country league and replace its 6th club with this custom club to maintain structure
        val league = _ligas.value.firstOrNull { it.country == countryName }
        if (league != null) {
            val updatedLeagueClubs = league.clubs.toMutableList()
            if (updatedLeagueClubs.isNotEmpty()) {
                val replacedClub = updatedLeagueClubs.removeAt(updatedLeagueClubs.size - 1)
                _clubs.value = _clubs.value.filter { it.id != replacedClub.id } + customClub
            } else {
                _clubs.value = _clubs.value + customClub
            }
            updatedLeagueClubs.add(customClub)

            // Modify league properties directly
            val index = _ligas.value.indexOf(league)
            val updatedLigas = _ligas.value.toMutableList()
            updatedLigas[index] = league.copy(clubs = updatedLeagueClubs)
            updatedLigas[index].generateSchedule() // Re-generate schedule to match the new custom club!
            _ligas.value = updatedLigas
        } else {
            _clubs.value = _clubs.value + customClub
        }

        // Set chosen country's league as MAX_DETAIL
        _ligas.value.forEach { l ->
            if (l.country == countryName) {
                l.visibility = LeagueVisibility.MAX_DETAIL
            } else {
                l.visibility = LeagueVisibility.ZERO_DETAIL
            }
        }

        val mgr = Manager(
            name = managerName,
            personalWealth = 15_000L,
            currentClubId = customClub.id,
            currentClubName = customClub.name
        )
        _manager.value = mgr

        // Social feed setup
        val posts = mutableListOf<SocialPost>()
        posts.add(SocialPost(handle = "@FEDEBOL_Oficial", authorName = "FEDEBOL", content = "El nuevo club ${customClub.name} ha sido formalmente admitido en la Liga de ${countryName} por FEDEBOL.", likes = 1800, reposts = 420, timeAgo = "1h"))
        posts.add(SocialPost(handle = "@Hinchas_${customClub.name.replace(" ", "")}", authorName = "Fundadores", content = "¡Un hito histórico! Iniciamos nuestra aventura desde cero bajo las órdenes de ${mgr.name}.", likes = 990, reposts = 310, timeAgo = "1m"))
        _socialFeed.value = posts

        // Save everything to disk
        addNews("Guardando datos del nuevo club y jugadores...")
        _currentDate.value = LocalDate.of(2025, 1, 1)
        storage.saveManager(mgr)
        storage.saveCalendarDate("2025-01-01")
        storage.saveClubsBatch(_clubs.value)
        storage.saveLigasBatch(_ligas.value)
        storage.savePlayersBatch(_clubs.value.flatMap { it.squad })

        _isOnboardingFinished.value = true
        _isSimulating.value = false
    }

    // Try to load existing game
    suspend fun tryLoadGame(): Boolean = withContext(Dispatchers.IO) {
        val loadedClubs = storage.loadClubs()
        val loadedLigas = storage.loadLigas()

        if (loadedClubs.isEmpty() || loadedLigas.isEmpty()) {
            return@withContext false
        }

        _clubs.value = loadedClubs
        _ligas.value = loadedLigas

        // Re-construct squad references to maintain memory integrity
        loadedClubs.forEach { club ->
            val squadPlayers = club.squad.toMutableList()
            club.squad.clear()
            club.squad.addAll(squadPlayers)
        }

        // Load Manager profile
        val loadedManager = storage.loadManager()
        if (loadedManager != null) {
            _manager.value = loadedManager
        }

        // Load Calendar date
        val loadedDateStr = storage.loadCalendarDate()
        if (loadedDateStr != null) {
            try {
                _currentDate.value = LocalDate.parse(loadedDateStr)
            } catch (e: Exception) {
                _currentDate.value = LocalDate.of(2025, 1, 1)
            }
        } else {
            _currentDate.value = LocalDate.of(2025, 1, 1)
        }

        _newsLog.value = listOf("Partida cargada con éxito desde el almacenamiento AES fragmentado.")
        _isOnboardingFinished.value = true
        true
    }

    // Advance 1 Round (Main logic loop executed strictly outside UI Thread)
    suspend fun advanceRound() = withContext(Dispatchers.Default) {
        if (_isSimulating.value) return@withContext
        _isSimulating.value = true
        addNews("Iniciando simulación de la jornada...")

        val activeLeagues = _ligas.value
        val activeClubs = _clubs.value.associateBy { it.id }

        // Process Match Simulation per League depending on visibility levels
        activeLeagues.forEach { league ->
            val fixtures = league.fixtures
            val roundIdx = league.currentRound

            if (roundIdx < fixtures.size) {
                val round = fixtures[roundIdx]
                addNews("Simulando fecha ${round.roundNumber} de la ${league.name}...")

                round.matches.forEach { match ->
                    val homeClub = activeClubs[match.homeClubId]
                    val awayClub = activeClubs[match.awayClubId]

                    if (homeClub != null && awayClub != null && !match.played) {
                        MatchEngine.simulateMatch(match, homeClub, awayClub, league.visibility)
                    }
                }

                league.currentRound++
            } else {
                addNews("La ${league.name} ha concluido la temporada. Reseteando tabla...")
                league.clubs.forEach { it.resetStats() }
                league.generateSchedule()
            }
        }

        // Process Manager Salary & Career Finances
        val currentMgr = _manager.value
        val activeClub = _clubs.value.firstOrNull { it.id == currentMgr.currentClubId }
        if (activeClub != null) {
            // Manager weekly payout based on reputation and club tier
            var salaryEarned = (currentMgr.reputation * 30 + activeClub.stadiumCapacity / 1000 * 50).toLong().coerceIn(1000L, 10000L)
            if (currentMgr.isSummoned) {
                salaryEarned += 2500L // Extra $2,500 for national team duties
            }
            currentMgr.personalWealth += salaryEarned
            addNews("Recibiste tu salario semanal de $${salaryEarned} como mánager del ${activeClub.name}${if (currentMgr.isSummoned) " y tu labor de Selección Nacional" else ""}.")
        }

        // Auto-increment national team summon progress slowly if not summoned
        if (!currentMgr.isSummoned && currentMgr.nationalSummonProgress < 100) {
            val randomIncrement = Random.nextInt(2, 6) + (currentMgr.reputation / 25)
            currentMgr.nationalSummonProgress = (currentMgr.nationalSummonProgress + randomIncrement).coerceAtMost(100)
            if (currentMgr.nationalSummonProgress >= 100) {
                addNews("🦁 CONVOCATORIA DISPONIBLE: ¡Tu reputación continental es excelente! Tienes una oferta de Selección Nacional en el Gabinete.")
            }
        }

        // Process FEDEBOL President Election cycle
        val currentFafi = _fafi.value
        currentFafi.yearsUntilElection--
        if (currentFafi.yearsUntilElection <= 0) {
            val announcement = currentFafi.triggerElectionEvent()
            addNews("🗳️ ELECCIONES FEDEBOL: $announcement")
            _newsLog.value = (_newsLog.value + "FEDEBOL: ${currentFafi.currentRuleSet}")
        }
        _fafi.value = currentFafi

        // Trigger dynamic player events & Social Feed conflicts
        generateRandomSocialCrisis()

        // Advance date by 7 days (weekly matches)
        _currentDate.value = _currentDate.value.plusDays(7)

        // Batch save to slow storage asynchronously (preventing UI bottleneck)
        addNews("Guardando estado del universo de forma asíncrona en lotes...")
        storage.saveManager(_manager.value)
        storage.saveCalendarDate(_currentDate.value.toString())
        storage.saveClubsBatch(_clubs.value)
        storage.saveLigasBatch(_ligas.value)

        _isSimulating.value = false
        addNews("Jornada simulada exitosamente. Revisa el feed social y las tablas.")
    }

    // Handles Manager social network feedback decisions
    fun handleSocialFeedDecision(postId: String, decisionIndex: Int) {
        socialFeedManager.handleSocialFeedDecision(postId, decisionIndex)
    }

    // Dynamic Microblogging generator for player egos & disciplines
    fun generateRandomSocialCrisis() {
        socialFeedManager.generateRandomSocialCrisis()
    }

    private fun addNews(news: String) {
        val log = _newsLog.value.toMutableList()
        log.add(0, news)
        if (log.size > 40) {
            _newsLog.value = log.take(40)
        } else {
            _newsLog.value = log
        }
        Log.i("GameEngine", news)
    }

    // Recover player energies and decrease injury timers
    fun advanceWeekRecovery() {
        val currentClubs = _clubs.value
        currentClubs.forEach { club ->
            club.squad.forEach { player ->
                // Infinite lungs recovery boost
                val staminaRefill = if (player.traits.contains(Trait.PULMON_INFINITO)) 18 else 12
                player.energy = (player.energy + staminaRefill).coerceAtMost(100)

                if (player.isInjured) {
                    player.injuryDurationWeeks--
                    if (player.injuryDurationWeeks <= 0) {
                        player.isInjured = false
                        player.injuryDurationWeeks = 0
                        addNews("🏥 MÉDICO: ${player.fullName} se ha recuperado de su lesión y está disponible.")
                    }
                }
            }
        }
    }

    fun purchaseLicense(type: String): Boolean {
        return careerManager.purchaseLicense(type)
    }

    fun hirePrivateAgent(): Boolean {
        return careerManager.hirePrivateAgent()
    }

    fun investInPRCampaign(): Boolean {
        return careerManager.investInPRCampaign()
    }

    fun acceptNationalSummon(): Boolean {
        return careerManager.acceptNationalSummon()
    }

    fun resignNationalSummon(): Boolean {
        return careerManager.resignNationalSummon()
    }
}
