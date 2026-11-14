package com.example

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.core.ApiKeyVaultManager
import com.example.core.PlayBillingManager
import com.example.core.SeasonalThemeManager
import com.example.core.attemptAutoSignIn
import com.example.core.authStateFlow
import com.example.core.configureAppCheck
import com.example.core.onGoogleSignInClicked
import com.example.core.signOutUser
import com.example.data.repository.AiStudyRepository
import com.example.data.repository.StudyRepository
import com.example.data.repository.VisitorStudyStore
import com.example.ui.components.MenteProTopBar
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.AiConnectorsScreen
import com.example.ui.screens.AiLabScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PlanAltoScreen
import com.example.ui.screens.PlannerScreen
import com.example.ui.screens.ProfileSettingsDialog
import com.example.ui.screens.SplashLoadingScreen
import com.example.ui.screens.SubjectsHubScreen
import com.example.ui.theme.MenteProTheme
import com.example.ui.viewmodel.MenteProViewModel
import com.example.ui.viewmodel.UiState
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Step 1: Configure Firebase App Check with launch Intent before any UI or ViewModels
        configureAppCheck(this, intent)

        enableEdgeToEdge()
        setContent {
            val systemDark = isSystemInDarkTheme()
            var darkThemeOverride by rememberSaveable { mutableStateOf<Boolean?>(null) }
            val isDark = darkThemeOverride ?: systemDark

            AppNavigation(
                isDarkTheme = isDark,
                onToggleDarkTheme = { darkThemeOverride = !isDark }
            )
        }
    }
}

@Composable
fun AppNavigation(
    isDarkTheme: Boolean,
    onToggleDarkTheme: () -> Unit,
    auth: FirebaseAuth = Firebase.auth
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    var showSplash by rememberSaveable { mutableStateOf(true) }
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    var isVisitorMode by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (auth.currentUser == null) {
            attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = { isVisitorMode = false },
                onUnauthenticated = {},
                scope = scope
            )
        }
    }

    if (showSplash) {
        val bootSeason = remember { SeasonalThemeManager.resolveSeason("auto") }
        MenteProTheme(seasonId = bootSeason.seasonId, darkTheme = isDarkTheme) {
            SplashLoadingScreen(
                seasonalInfo = bootSeason,
                onContinueToApp = { showSplash = false }
            )
        }
        return
    }

    val user = currentUser
    when {
        user != null -> {
            val email = user.email ?: user.providerData.firstOrNull { !it.email.isNullOrBlank() }?.email
            key(user.uid) {
                StudyWorkspaceContent(
                    sessionKey = user.uid,
                    currentUserId = user.uid,
                    displayName = user.displayName ?: "Estudiante",
                    userEmail = email,
                    isEmailVerified = user.isEmailVerified,
                    isVisitorMode = false,
                    isDarkTheme = isDarkTheme,
                    onToggleDarkTheme = onToggleDarkTheme,
                    onConnectGoogle = {},
                    onSignOut = {
                        isVisitorMode = false
                        signOutUser(credentialManager, scope)
                    }
                )
            }
        }
        isVisitorMode -> {
            key("visitante") {
                StudyWorkspaceContent(
                    sessionKey = "visitante",
                    currentUserId = "visitante",
                    displayName = "Visitante",
                    userEmail = null,
                    isEmailVerified = false,
                    isVisitorMode = true,
                    isDarkTheme = isDarkTheme,
                    onToggleDarkTheme = onToggleDarkTheme,
                    onConnectGoogle = {
                        onGoogleSignInClicked(
                            context = context,
                            credentialManager = credentialManager,
                            onAuthSuccess = { isVisitorMode = false },
                            onAuthError = {},
                            scope = scope
                        )
                    },
                    onSignOut = {
                        isVisitorMode = false
                    }
                )
            }
        }
        else -> {
            MenteProTheme(
                seasonId = SeasonalThemeManager.detectAutoSeason(),
                darkTheme = isDarkTheme
            ) {
                AuthScreen(
                    onAuthSuccess = { isVisitorMode = false },
                    onEnterAsVisitor = { isVisitorMode = true }
                )
            }
        }
    }
}

private data class NavDestination(
    val label: String,
    val icon: ImageVector,
    val testTag: String
)

@Composable
private fun StudyWorkspaceContent(
    sessionKey: String,
    currentUserId: String,
    displayName: String,
    userEmail: String?,
    isEmailVerified: Boolean,
    isVisitorMode: Boolean,
    isDarkTheme: Boolean,
    onToggleDarkTheme: () -> Unit,
    onConnectGoogle: () -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val viewModel: MenteProViewModel = viewModel(
        key = sessionKey,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                val store = if (isVisitorMode) {
                    VisitorStudyStore()
                } else {
                    val databaseId = app.getString(R.string.firestore_database_id)
                    StudyRepository(FirebaseFirestore.getInstance(databaseId))
                }
                MenteProViewModel(
                    studyRepo = store,
                    aiRepo = AiStudyRepository(),
                    currentUserId = currentUserId,
                    initialDisplayName = displayName,
                    isVisitorMode = isVisitorMode,
                    apiKeyVault = ApiKeyVaultManager(app.applicationContext),
                    userEmail = userEmail,
                    isEmailVerified = isEmailVerified
                )
            }
        }
    )

    val playBillingManager = remember(sessionKey) {
        PlayBillingManager(
            context = context.applicationContext,
            scope = scope,
            onPurchasesVerified = { productIds -> viewModel.syncPlayBillingPurchases(productIds) },
            onBillingStatusMessage = { msg ->
                scope.launch { snackbarHostState.showSnackbar(msg) }
            }
        )
    }

    DisposableEffect(playBillingManager) {
        playBillingManager.startConnection()
        onDispose { playBillingManager.endConnection() }
    }

    val playOffers by playBillingManager.offers.collectAsStateWithLifecycle()
    val isPlayStoreReady by playBillingManager.isPlayStoreReady.collectAsStateWithLifecycle()

    val profileState by viewModel.profileState.collectAsStateWithLifecycle()
    val profile = (profileState as? UiState.Success)?.data
    val seasonalInfo by viewModel.seasonalInfoState.collectAsStateWithLifecycle()
    val aiConnectorConfig by viewModel.aiConnectorConfig.collectAsStateWithLifecycle()
    val connectorTestState by viewModel.connectorTestState.collectAsStateWithLifecycle()
    val allUsersAdmin by viewModel.allUsersAdminState.collectAsStateWithLifecycle()
    val inspectedUserState by viewModel.inspectedUserState.collectAsStateWithLifecycle()

    val subjects by viewModel.subjectsState.collectAsStateWithLifecycle()
    val teachers by viewModel.schoolTeachersState.collectAsStateWithLifecycle()
    val plans by viewModel.studyPlansState.collectAsStateWithLifecycle()
    val tasks by viewModel.smartTasksState.collectAsStateWithLifecycle()
    val examResults by viewModel.examResultsState.collectAsStateWithLifecycle()
    val workshopItems by viewModel.workshopItemsState.collectAsStateWithLifecycle()
    val financeEntries by viewModel.financeEntriesState.collectAsStateWithLifecycle()
    val missions by viewModel.missionsState.collectAsStateWithLifecycle()

    val calcSolutionState by viewModel.calcSolutionState.collectAsStateWithLifecycle()
    val isGeneratingPlan by viewModel.isGeneratingPlan.collectAsStateWithLifecycle()
    val isGeneratingTask by viewModel.isGeneratingTask.collectAsStateWithLifecycle()
    val examState by viewModel.examState.collectAsStateWithLifecycle()
    val matchPairsState by viewModel.matchPairsState.collectAsStateWithLifecycle()
    val homeworkScanState by viewModel.homeworkScanState.collectAsStateWithLifecycle()
    val slideAnalysisState by viewModel.slideAnalysisState.collectAsStateWithLifecycle()
    val tutorMessages by viewModel.tutorMessages.collectAsStateWithLifecycle()
    val isTutorStreaming by viewModel.isTutorStreaming.collectAsStateWithLifecycle()
    val petMessage by viewModel.petMessage.collectAsStateWithLifecycle()
    val chessCoachState by viewModel.chessCoachState.collectAsStateWithLifecycle()
    val schoolAutomationState by viewModel.schoolAutomationState.collectAsStateWithLifecycle()
    val duelChallengeState by viewModel.duelChallengeState.collectAsStateWithLifecycle()
    val duelSession by viewModel.duelSession.collectAsStateWithLifecycle()
    val artsGuideState by viewModel.artsGuideState.collectAsStateWithLifecycle()
    val automatedAgentState by viewModel.automatedAgentState.collectAsStateWithLifecycle()
    val routePlanState by viewModel.routePlanState.collectAsStateWithLifecycle()
    val executiveAdvisorState by viewModel.executiveAdvisorState.collectAsStateWithLifecycle()
    val codePlaygroundState by viewModel.codePlaygroundState.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

    LaunchedEffect(toastMessage) {
        val msg = toastMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var plannerInitialSubTab by rememberSaveable { mutableIntStateOf(0) }
    var aiLabInitialSubTab by rememberSaveable { mutableIntStateOf(0) }
    var showSettings by remember { mutableStateOf(false) }
    var showConnectorsScreen by rememberSaveable { mutableStateOf(false) }
    var showAdminScreen by rememberSaveable { mutableStateOf(false) }

    if (selectedTab != 0 && !showConnectorsScreen && !showAdminScreen) {
        BackHandler { selectedTab = 0 }
    }

    val destinations = remember {
        listOf(
            NavDestination("Inicio", Icons.Filled.Home, "nav_home"),
            NavDestination("Plan Alto", Icons.Filled.BusinessCenter, "nav_plan_alto"),
            NavDestination("Materias", Icons.AutoMirrored.Filled.MenuBook, "nav_subjects"),
            NavDestination("Planes", Icons.Filled.DateRange, "nav_plans"),
            NavDestination("Lab IA", Icons.Filled.Psychology, "nav_ailab")
        )
    }

    MenteProTheme(
        accentKey = profile?.themeAccent ?: "indigo",
        seasonId = seasonalInfo.seasonId,
        darkTheme = isDarkTheme
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val useRail = maxWidth >= 600.dp

                Scaffold(
                    contentWindowInsets = WindowInsets.safeDrawing,
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    bottomBar = {
                        if (!useRail) {
                            NavigationBar {
                                destinations.forEachIndexed { idx, dest ->
                                    NavigationBarItem(
                                        selected = selectedTab == idx && !showConnectorsScreen && !showAdminScreen,
                                        onClick = {
                                            showConnectorsScreen = false
                                            showAdminScreen = false
                                            selectedTab = idx
                                        },
                                        icon = { Icon(dest.icon, contentDescription = dest.label) },
                                        label = { Text(dest.label, maxLines = 1) },
                                        modifier = Modifier.testTag(dest.testTag)
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (useRail) {
                            NavigationRail {
                                destinations.forEachIndexed { idx, dest ->
                                    NavigationRailItem(
                                        selected = selectedTab == idx && !showConnectorsScreen && !showAdminScreen,
                                        onClick = {
                                            showConnectorsScreen = false
                                            showAdminScreen = false
                                            selectedTab = idx
                                        },
                                        icon = { Icon(dest.icon, contentDescription = dest.label) },
                                        label = { Text(dest.label, maxLines = 1) },
                                        modifier = Modifier.testTag(dest.testTag)
                                    )
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            MenteProTopBar(
                                profile = profile,
                                isVisitorMode = isVisitorMode,
                                isAdmin = viewModel.isAdmin,
                                isChristmasMode = seasonalInfo.isChristmasMode,
                                onToggleSessionMode = {
                                    val next = if (profile?.sessionMode == "numeric") "gamification" else "numeric"
                                    viewModel.setSessionMode(next)
                                },
                                onOpenAdmin = {
                                    showConnectorsScreen = false
                                    showAdminScreen = true
                                },
                                onConnectGoogle = onConnectGoogle,
                                onOpenSettings = { showSettings = true }
                            )

                            Box(modifier = Modifier.weight(1f)) {
                                when {
                                    showAdminScreen -> AdminPanelScreen(
                                        isAdmin = viewModel.isAdmin,
                                        allUsers = allUsersAdmin,
                                        workshopItems = workshopItems,
                                        inspectedUserState = inspectedUserState,
                                        onInspectUser = { uid -> viewModel.inspectUserAdmin(uid) },
                                        onGrantEntitlement = { uid, f, v -> viewModel.adminToggleUserEntitlement(uid, f, v) },
                                        onDeleteUserSubItem = { uid, sub, docId -> viewModel.adminDeleteUserSubItem(uid, sub, docId) },
                                        onDeleteUser = { uid -> viewModel.adminDeleteUser(uid) },
                                        onDeleteWorkshopItem = { id -> viewModel.adminDeleteWorkshopItem(id) },
                                        onBack = { showAdminScreen = false }
                                    )
                                    showConnectorsScreen -> AiConnectorsScreen(
                                        config = aiConnectorConfig,
                                        testState = connectorTestState,
                                        onSaveConfig = { cfg -> viewModel.saveAiConnectorConfig(cfg) },
                                        onTestConnector = { cfg -> viewModel.testAiConnector(cfg) },
                                        onClearKeys = { viewModel.clearAiConnectorKeys() },
                                        onBack = { showConnectorsScreen = false }
                                    )
                                    else -> when (selectedTab) {
                                        0 -> HomeScreen(
                                            profile = profile,
                                            subjects = subjects,
                                            plans = plans,
                                            tasks = tasks,
                                            examResults = examResults,
                                            petMessage = petMessage,
                                            isGeneratingTask = isGeneratingTask,
                                            seasonalInfo = seasonalInfo,
                                            connectorConfig = aiConnectorConfig,
                                            isAdmin = viewModel.isAdmin,
                                            onRefreshPetMessage = { viewModel.refreshPetEncouragement() },
                                            onSaveSubject = { n, g, h -> viewModel.saveSubject(n, g, h) },
                                            onDeleteSubject = { id -> viewModel.deleteSubject(id) },
                                            onCreateSmartTask = { p, s -> viewModel.createAiSmartTask(p, s) },
                                            onToggleTask = { t -> viewModel.toggleSmartTaskCompletion(t) },
                                            onDeleteTask = { id -> viewModel.deleteSmartTask(id) },
                                            onNavigateToCalculator = {
                                                aiLabInitialSubTab = 4
                                                selectedTab = 4
                                            },
                                            onNavigateToPlanner = { subTab ->
                                                plannerInitialSubTab = subTab
                                                selectedTab = 3
                                            },
                                            onNavigateToSubjects = { selectedTab = 2 },
                                            onNavigateToAiLab = { subTab ->
                                                aiLabInitialSubTab = subTab
                                                selectedTab = 4
                                            },
                                            onNavigateToPlanAlto = { selectedTab = 1 },
                                            onNavigateToConnectors = { showConnectorsScreen = true },
                                            onNavigateToAdmin = { showAdminScreen = true },
                                            onClaimSeasonalEgg = { viewModel.claimSeasonalEgg() },
                                            onTriggerProton2Egg = { viewModel.triggerProton2EasterEgg() }
                                        )

                                        1 -> PlanAltoScreen(
                                            profile = profile,
                                            financeEntries = financeEntries,
                                            routePlanState = routePlanState,
                                            executiveAdvisorState = executiveAdvisorState,
                                            onUpdateHigherEdProfile = { age, lvl, deg ->
                                                viewModel.updateHigherEdProfile(age, lvl, deg)
                                            },
                                            onAddFinanceEntry = { t, amt, type, cat, note ->
                                                viewModel.addFinanceEntry(t, amt, type, cat, note)
                                            },
                                            onDeleteFinanceEntry = { id -> viewModel.deleteFinanceEntry(id) },
                                            onPlanRoute = { o, d, m -> viewModel.planUniversityRoute(o, d, m) },
                                            onRunExecutiveAdvisor = { q, auto ->
                                                viewModel.runExecutivePersonalAdvisor(q, auto)
                                            }
                                        )

                                        2 -> SubjectsHubScreen(
                                            profile = profile,
                                            teachers = teachers,
                                            schoolAutomationState = schoolAutomationState,
                                            artsGuideState = artsGuideState,
                                            onAutomateSchool = { name, news -> viewModel.saveSchoolAndAutomate(name, news) },
                                            onAddManualTeacher = { n, s, f, h -> viewModel.addManualTeacher(n, s, f, h) },
                                            onDeleteTeacher = { id -> viewModel.deleteTeacher(id) },
                                            onGenerateArtsHelp = { disc, topic -> viewModel.generateArtsHelp(disc, topic) },
                                            onSaveArtsTask = { guide -> viewModel.saveArtsGuideAsTask(guide) },
                                            onCreateTopicTask = { prompt, subj ->
                                                viewModel.createAiSmartTask(prompt, subj)
                                            },
                                            onOpenCalculator = {
                                                aiLabInitialSubTab = 4
                                                selectedTab = 4
                                            },
                                            onOpenTutor = {
                                                aiLabInitialSubTab = 0
                                                selectedTab = 4
                                            },
                                            onOpenDuels = {
                                                aiLabInitialSubTab = 5
                                                selectedTab = 4
                                            }
                                        )

                                        3 -> PlannerScreen(
                                            profile = profile,
                                            subjects = subjects,
                                            plans = plans,
                                            workshopItems = workshopItems,
                                            playOffers = playOffers,
                                            isPlayStoreReady = isPlayStoreReady,
                                            isGeneratingPlan = isGeneratingPlan,
                                            automatedAgentState = automatedAgentState,
                                            initialSubTab = plannerInitialSubTab,
                                            onSaveSubject = { n, g, h -> viewModel.saveSubject(n, g, h) },
                                            onGeneratePlan = { lvl, mins, goal ->
                                                viewModel.generateAiStudyPlan(lvl, mins, goal)
                                            },
                                            onToggleModule = { plan, idx -> viewModel.togglePlanModule(plan, idx) },
                                            onPublishPlanToWorkshop = { plan ->
                                                val steps = plan.modules.map { "${it.title} (${it.subject})" }
                                                viewModel.publishToWorkshop(
                                                    title = plan.title,
                                                    category = "plan",
                                                    subject = plan.focusSubjects.firstOrNull() ?: "General",
                                                    description = plan.summary,
                                                    steps = steps
                                                )
                                            },
                                            onDeletePlan = { id -> viewModel.deleteStudyPlan(id) },
                                            onLaunchPlayPurchase = { sku ->
                                                playBillingManager.launchPurchaseFlow(activity, sku)
                                            },
                                            onRestorePlayPurchases = {
                                                playBillingManager.restorePurchases(silent = false)
                                            },
                                            onRunAutomatedAgent = { viewModel.runMonthlyAutomatedAgent() },
                                            onDownloadWorkshopItem = { item -> viewModel.downloadFromWorkshop(item) },
                                            onLikeWorkshopItem = { id -> viewModel.likeWorkshopItem(id) },
                                            onPublishWorkshopItem = { t, c, s, d, steps ->
                                                viewModel.publishToWorkshop(t, c, s, d, steps)
                                            }
                                        )

                                        4 -> AiLabScreen(
                                            profile = profile,
                                            subjects = subjects,
                                            missions = missions,
                                            seasonalInfo = seasonalInfo,
                                            tutorMessages = tutorMessages,
                                            isTutorStreaming = isTutorStreaming,
                                            duelSession = duelSession,
                                            duelChallengeState = duelChallengeState,
                                            examState = examState,
                                            examResults = examResults,
                                            homeworkScanState = homeworkScanState,
                                            slideAnalysisState = slideAnalysisState,
                                            codePlaygroundState = codePlaygroundState,
                                            calcSolutionState = calcSolutionState,
                                            matchPairsState = matchPairsState,
                                            chessCoachState = chessCoachState,
                                            initialSubTab = aiLabInitialSubTab,
                                            onSendTutorMessage = { msg -> viewModel.sendTutorMessage(msg) },
                                            onScanHomework = { bmp, hint -> viewModel.scanHomeworkImage(bmp, hint) },
                                            onSaveHomeworkTask = { scan -> viewModel.saveHomeworkAsTask(scan) },
                                            onAnalyzeSlide = { bmp, hint -> viewModel.analyzeSlideImage(bmp, hint) },
                                            onSaveSlideTask = { res -> viewModel.saveSlideAsSmartTask(res) },
                                            onGeneratePlayableCode = { p, k -> viewModel.generatePlayableCode(p, k) },
                                            onUpdatePetConfig = { name, sp, acc, col, cost ->
                                                viewModel.updatePetConfiguration(name, sp, acc, col, cost)
                                            },
                                            onApplyBaseConfig = { sp -> viewModel.applyMascotaBaseConfig(sp) },
                                            onFeedPet = { viewModel.feedPetWithCoins() },
                                            onUnlockBadge = { id, title -> viewModel.unlockBadge(id, title) },
                                            onSelectSeason = { key -> viewModel.setWeatherSeasonOverride(key) },
                                            onClaimSeasonalEgg = { viewModel.claimSeasonalEgg() },
                                            onSolveWithAiCalc = { prob, mode -> viewModel.solveWithAiCalculator(prob, mode) },
                                            onStartDuel = { agent -> viewModel.startDuel(agent) },
                                            onSubmitDuelAnswer = { idx -> viewModel.submitDuelAnswer(idx) },
                                            onNextDuelRound = { viewModel.nextDuelRound() },
                                            onResetDuel = { viewModel.resetDuel() },
                                            onGenerateExam = { subj, topic, diff, count ->
                                                viewModel.generateAiExam(subj, topic, diff, count)
                                            },
                                            onSubmitExamResult = { exam, correct ->
                                                viewModel.submitExamResult(exam, correct)
                                            },
                                            onResetExam = { viewModel.resetExam() },
                                            onGenerateMatchPairs = { subj -> viewModel.generateMatchMinigame(subj) },
                                            onMinigameWin = { label -> viewModel.rewardMinigameWin(label) },
                                            onAskChessCoach = { puzzle, q -> viewModel.askChessCoach(puzzle, q) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSettings) {
        ProfileSettingsDialog(
            profile = profile,
            isVisitorMode = isVisitorMode,
            isAdmin = viewModel.isAdmin,
            isDarkTheme = isDarkTheme,
            onToggleDarkTheme = onToggleDarkTheme,
            onSetSessionMode = { mode -> viewModel.setSessionMode(mode) },
            onSetThemeAccent = { accent -> viewModel.setThemeAccent(accent) },
            onSetSeasonOverride = { season -> viewModel.setWeatherSeasonOverride(season) },
            onSaveDetails = { lvl, pet, code, age, deg ->
                viewModel.updateAcademicAndPet(lvl, pet, code, age, deg)
            },
            onLaunchPlayPurchase = { sku ->
                playBillingManager.launchPurchaseFlow(activity, sku)
            },
            onRestorePlayPurchases = {
                playBillingManager.restorePurchases(silent = false)
            },
            onOpenConnectors = { showConnectorsScreen = true },
            onOpenAdminPanel = { showAdminScreen = true },
            onConnectGoogle = {
                showSettings = false
                onConnectGoogle()
            },
            onSignOut = {
                showSettings = false
                onSignOut()
            },
            onDismiss = { showSettings = false }
        )
    }
}
