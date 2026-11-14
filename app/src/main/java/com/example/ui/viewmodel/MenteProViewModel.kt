package com.example.ui.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.ApiKeyVaultManager
import com.example.core.PlayBillingManager
import com.example.core.SeasonalThemeManager
import com.example.data.model.AdminUserInspectBundle
import com.example.data.model.AiConnectorConfig
import com.example.data.model.AiDuelAgent
import com.example.data.model.ArtsGuideResult
import com.example.data.model.ChessPuzzle
import com.example.data.model.CodePlaygroundResult
import com.example.data.model.DuelChallenge
import com.example.data.model.ExamResult
import com.example.data.model.ExecutiveAdvisorResult
import com.example.data.model.FinanceEntry
import com.example.data.model.GamificationMission
import com.example.data.model.GeneratedExam
import com.example.data.model.HomeworkScanResult
import com.example.data.model.MatchPair
import com.example.data.model.RoutePlanResult
import com.example.data.model.SchoolAutomationResult
import com.example.data.model.SchoolTeacher
import com.example.data.model.SeasonalThemeInfo
import com.example.data.model.SlideAnalysisResult
import com.example.data.model.SmartTask
import com.example.data.model.StepByStepSolution
import com.example.data.model.StudentProfile
import com.example.data.model.StudyPlan
import com.example.data.model.SubjectRecord
import com.example.data.model.TutorMessage
import com.example.data.model.WorkshopItem
import com.example.data.repository.AiStudyRepository
import com.example.data.repository.StudyDataStore
import com.example.data.repository.StudyRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

sealed interface AiCallState<out T> {
    data object Idle : AiCallState<Nothing>
    data object Loading : AiCallState<Nothing>
    data class Success<T>(val data: T) : AiCallState<T>
    data class Error(val message: String) : AiCallState<Nothing>
}

data class DuelSessionState(
    val activeAgent: AiDuelAgent? = null,
    val round: Int = 1,
    val totalRounds: Int = 3,
    val playerScore: Int = 0,
    val agentScore: Int = 0,
    val selectedOption: Int? = null,
    val pastQuestions: List<String> = emptyList(),
    val isFinished: Boolean = false
)

class MenteProViewModel(
    private val studyRepo: StudyDataStore,
    private val aiRepo: AiStudyRepository,
    private val currentUserId: String,
    private val initialDisplayName: String,
    val isVisitorMode: Boolean = false,
    private val apiKeyVault: ApiKeyVaultManager? = null,
    userEmail: String? = null,
    isEmailVerified: Boolean = false
) : ViewModel() {

    val isAdmin: Boolean = !isVisitorMode &&
        isEmailVerified &&
        userEmail.equals(StudyRepository.ADMIN_EMAIL, ignoreCase = true)

    private fun unwrapFirestoreError(error: Throwable): Throwable {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        return firestoreError ?: error
    }

    val profileState: StateFlow<UiState<StudentProfile>> = studyRepo.observeProfile(currentUserId)
        .map<StudentProfile?, UiState<StudentProfile>> { profile ->
            if (profile != null) UiState.Success(profile) else UiState.Loading
        }
        .catch { error ->
            val root = unwrapFirestoreError(error)
            Log.w(TAG, "Error observing profile", root)
            emit(UiState.Error(root.message ?: "Error al cargar el perfil"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    val subjectsState: StateFlow<List<SubjectRecord>> = studyRepo.observeSubjects(currentUserId)
        .catch { error ->
            val root = unwrapFirestoreError(error)
            Log.w(TAG, "Error observing subjects", root)
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val schoolTeachersState: StateFlow<List<SchoolTeacher>> = studyRepo.observeSchoolTeachers(currentUserId)
        .catch { error ->
            val root = unwrapFirestoreError(error)
            Log.w(TAG, "Error observing teachers", root)
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val studyPlansState: StateFlow<List<StudyPlan>> = studyRepo.observeStudyPlans(currentUserId)
        .catch { error ->
            val root = unwrapFirestoreError(error)
            Log.w(TAG, "Error observing study plans", root)
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val smartTasksState: StateFlow<List<SmartTask>> = studyRepo.observeSmartTasks(currentUserId)
        .catch { error ->
            val root = unwrapFirestoreError(error)
            Log.w(TAG, "Error observing tasks", root)
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val examResultsState: StateFlow<List<ExamResult>> = studyRepo.observeExamResults(currentUserId)
        .catch { error ->
            val root = unwrapFirestoreError(error)
            Log.w(TAG, "Error observing exam results", root)
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val workshopItemsState: StateFlow<List<WorkshopItem>> = studyRepo.observeWorkshopItems()
        .catch { error ->
            val root = unwrapFirestoreError(error)
            Log.w(TAG, "Error observing workshop", root)
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val financeEntriesState: StateFlow<List<FinanceEntry>> = studyRepo.observeFinanceEntries(currentUserId)
        .catch { error ->
            val root = unwrapFirestoreError(error)
            Log.w(TAG, "Error observing finance entries", root)
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val missionsState: StateFlow<List<GamificationMission>> = combine(
        profileState,
        smartTasksState,
        studyPlansState,
        examResultsState,
        financeEntriesState
    ) { profState, tasks, plans, exams, finances ->
        val prof = (profState as? UiState.Success)?.data
        val completedTasks = tasks.count { it.completed }
        val activePlans = plans.count { it.progressPercent > 0L }
        listOf(
            GamificationMission(
                id = "m_task",
                title = "Dominar una Tarea Inteligente",
                description = "Completa al menos 1 tarea con procedimiento paso a paso.",
                xpReward = 40L,
                coinReward = 20L,
                completed = completedTasks >= 1,
                progressText = "$completedTasks/1"
            ),
            GamificationMission(
                id = "m_plan",
                title = "Avanzar tu Plan de Estudio",
                description = "Marca como completado algún módulo de tu plan.",
                xpReward = 35L,
                coinReward = 20L,
                completed = activePlans >= 1,
                progressText = "$activePlans/1"
            ),
            GamificationMission(
                id = "m_exam",
                title = "Reto de Examen IA",
                description = "Completa un examen adaptativo para medir tu avance.",
                xpReward = 50L,
                coinReward = 30L,
                completed = exams.isNotEmpty(),
                progressText = "${exams.size}/1"
            ),
            GamificationMission(
                id = "m_pet",
                title = "Estilizar a tu Mascota",
                description = "Configura especie, accesorio o color en el Configurador.",
                xpReward = 25L,
                coinReward = 15L,
                completed = (prof?.petAccessory ?: "ninguno") != "ninguno" || (prof?.petSpecies ?: "buho") != "buho",
                progressText = if ((prof?.petAccessory ?: "ninguno") != "ninguno") "1/1" else "0/1"
            ),
            GamificationMission(
                id = "m_finance",
                title = "Control Financiero Plan Alto",
                description = "Registra un ingreso, gasto o bien patrimonial.",
                xpReward = 30L,
                coinReward = 20L,
                completed = finances.isNotEmpty(),
                progressText = "${finances.size}/1"
            )
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage = _toastMessage.asStateFlow()

    private val _calcSolutionState = MutableStateFlow<AiCallState<StepByStepSolution>>(AiCallState.Idle)
    val calcSolutionState = _calcSolutionState.asStateFlow()

    private val _isGeneratingPlan = MutableStateFlow(false)
    val isGeneratingPlan = _isGeneratingPlan.asStateFlow()

    private val _isGeneratingTask = MutableStateFlow(false)
    val isGeneratingTask = _isGeneratingTask.asStateFlow()

    private val _examState = MutableStateFlow<AiCallState<GeneratedExam>>(AiCallState.Idle)
    val examState = _examState.asStateFlow()

    private val _matchPairsState = MutableStateFlow<AiCallState<List<MatchPair>>>(AiCallState.Idle)
    val matchPairsState = _matchPairsState.asStateFlow()

    private val _slideAnalysisState = MutableStateFlow<AiCallState<SlideAnalysisResult>>(AiCallState.Idle)
    val slideAnalysisState = _slideAnalysisState.asStateFlow()

    private val _tutorMessages = MutableStateFlow(
        listOf(
            TutorMessage(
                id = "welcome",
                isFromUser = false,
                text = "¡Hola! Soy tu Tutora IA de MentePro. Pídeme explicaciones paso a paso, fórmulas, resúmenes o ayuda con tus materias más difíciles."
            )
        )
    )
    val tutorMessages = _tutorMessages.asStateFlow()

    private val _isTutorStreaming = MutableStateFlow(false)
    val isTutorStreaming = _isTutorStreaming.asStateFlow()

    private val _petMessage = MutableStateFlow("¡Listo para estudiar juntos! Completa una tarea para hacerme evolucionar.")
    val petMessage = _petMessage.asStateFlow()

    private val _chessCoachState = MutableStateFlow<AiCallState<String>>(AiCallState.Idle)
    val chessCoachState = _chessCoachState.asStateFlow()

    // --- New States: School Automation, Duels, Arts, Automated Monthly Agent ---
    private val _schoolAutomationState = MutableStateFlow<AiCallState<SchoolAutomationResult>>(AiCallState.Idle)
    val schoolAutomationState = _schoolAutomationState.asStateFlow()

    private val _duelChallengeState = MutableStateFlow<AiCallState<DuelChallenge>>(AiCallState.Idle)
    val duelChallengeState = _duelChallengeState.asStateFlow()

    private val _duelSession = MutableStateFlow(DuelSessionState())
    val duelSession = _duelSession.asStateFlow()

    private val _artsGuideState = MutableStateFlow<AiCallState<ArtsGuideResult>>(AiCallState.Idle)
    val artsGuideState = _artsGuideState.asStateFlow()

    private val _automatedAgentState = MutableStateFlow<AiCallState<String>>(AiCallState.Idle)
    val automatedAgentState = _automatedAgentState.asStateFlow()

    // --- Versión 2 States: Cámara Tareas, Rutas Campus, Asistente 18+, Código y Videojuegos ---
    private val _homeworkScanState = MutableStateFlow<AiCallState<HomeworkScanResult>>(AiCallState.Idle)
    val homeworkScanState = _homeworkScanState.asStateFlow()

    private val _routePlanState = MutableStateFlow<AiCallState<RoutePlanResult>>(AiCallState.Idle)
    val routePlanState = _routePlanState.asStateFlow()

    private val _executiveAdvisorState = MutableStateFlow<AiCallState<ExecutiveAdvisorResult>>(AiCallState.Idle)
    val executiveAdvisorState = _executiveAdvisorState.asStateFlow()

    private val _codePlaygroundState = MutableStateFlow<AiCallState<CodePlaygroundResult>>(AiCallState.Idle)
    val codePlaygroundState = _codePlaygroundState.asStateFlow()

    // --- Protón 2 States: Conectores API, Clima/Navidad Easter Eggs, Admin ---
    private val _aiConnectorConfig = MutableStateFlow(apiKeyVault?.loadConfig() ?: AiConnectorConfig())
    val aiConnectorConfig = _aiConnectorConfig.asStateFlow()

    private val _connectorTestState = MutableStateFlow<AiCallState<String>>(AiCallState.Idle)
    val connectorTestState = _connectorTestState.asStateFlow()

    val seasonalInfoState: StateFlow<SeasonalThemeInfo> = profileState
        .map { state ->
            val override = (state as? UiState.Success)?.data?.weatherSeasonOverride
            SeasonalThemeManager.resolveSeason(override)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = SeasonalThemeManager.resolveSeason("auto")
        )

    val allUsersAdminState: StateFlow<List<StudentProfile>> = if (isAdmin) {
        studyRepo.observeAllUsersAdmin()
            .catch { error ->
                Log.w(TAG, "Admin users list error", error)
                emit(emptyList())
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())
    } else {
        flowOf(emptyList<StudentProfile>()).stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    }

    private val _inspectedUserState = MutableStateFlow<AiCallState<AdminUserInspectBundle>>(AiCallState.Idle)
    val inspectedUserState = _inspectedUserState.asStateFlow()

    init {
        aiRepo.updateConnectorConfig(_aiConnectorConfig.value)
        viewModelScope.launch {
            studyRepo.ensureProfileInitialized(initialDisplayName).onFailure { err ->
                _toastMessage.value = err.localizedMessage ?: "No se pudo inicializar el perfil."
            }
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    private fun currentProfileOrNull(): StudentProfile? =
        (profileState.value as? UiState.Success)?.data

    private suspend fun tryConsumeTicket(): StudentProfile? {
        val profile = currentProfileOrNull() ?: run {
            _toastMessage.value = "Cargando tu perfil, intenta en un segundo."
            return null
        }
        val res = studyRepo.consumeAiTicket(profile)
        if (res.isFailure) {
            _toastMessage.value = "Sin tickets de IA hoy. Completa tareas o activa el Pase Único / Agente Pro."
            return null
        }
        return profile
    }

    // --- Profile, Plans & Subscriptions ---

    fun setSessionMode(mode: String) {
        viewModelScope.launch {
            studyRepo.updateProfileFields(mapOf("sessionMode" to mode))
                .onFailure { _toastMessage.value = it.localizedMessage }
        }
    }

    fun setThemeAccent(accent: String) {
        viewModelScope.launch {
            studyRepo.updateProfileFields(mapOf("themeAccent" to accent))
                .onFailure { _toastMessage.value = it.localizedMessage }
        }
    }

    fun updateAcademicAndPet(
        academicLevel: String,
        petName: String,
        studyGroupCode: String,
        ageYears: Long? = null,
        degreeOrMaster: String? = null
    ) {
        viewModelScope.launch {
            val fields = mutableMapOf<String, Any>(
                "academicLevel" to academicLevel,
                "petName" to petName.trim().ifBlank { "Bubo" }.take(30),
                "studyGroupCode" to studyGroupCode.trim().take(20)
            )
            if (ageYears != null) {
                fields["ageYears"] = ageYears.coerceIn(5L, 120L)
            }
            if (degreeOrMaster != null) {
                fields["degreeOrMaster"] = degreeOrMaster.trim().take(120)
            }
            studyRepo.updateProfileFields(fields).onSuccess {
                _toastMessage.value = "Preferencias guardadas."
            }.onFailure {
                _toastMessage.value = it.localizedMessage
            }
        }
    }

    fun updatePetConfiguration(
        petName: String,
        petSpecies: String,
        petAccessory: String,
        petColorAccent: String,
        coinCost: Long = 0L
    ) {
        val profile = currentProfileOrNull() ?: return
        if (coinCost > profile.studyCoins) {
            _toastMessage.value = "Necesitas $coinCost monedas (tienes ${profile.studyCoins}). Completa misiones o tareas."
            return
        }
        viewModelScope.launch {
            val nextCoins = (profile.studyCoins - coinCost).coerceAtLeast(0L)
            val nextBadges = if (!profile.unlockedBadges.contains("estilo_mascota")) {
                profile.unlockedBadges + "estilo_mascota"
            } else profile.unlockedBadges
            studyRepo.updateProfileFields(
                mapOf(
                    "petName" to petName.trim().ifBlank { "Bubo" }.take(30),
                    "petSpecies" to petSpecies,
                    "petAccessory" to petAccessory,
                    "petColorAccent" to petColorAccent,
                    "studyCoins" to nextCoins,
                    "unlockedBadges" to nextBadges
                )
            ).onSuccess {
                _toastMessage.value = "¡Mascota actualizada y lista para estudiar!"
            }.onFailure {
                _toastMessage.value = it.localizedMessage
            }
        }
    }

    fun feedPetWithCoins() {
        val profile = currentProfileOrNull() ?: return
        if (profile.studyCoins < 20L) {
            _toastMessage.value = "Necesitas 20 monedas para dar impulso XP a ${profile.petName}."
            return
        }
        viewModelScope.launch {
            studyRepo.updateProfileFields(mapOf("studyCoins" to profile.studyCoins - 20L))
            val updated = studyRepo.awardStudyProgress(
                currentProfile = profile.copy(studyCoins = profile.studyCoins - 20L),
                xpEarned = 60L,
                coinsEarned = 0L
            ).getOrNull()
            if (updated != null && updated.petLevel > profile.petLevel) {
                _toastMessage.value = "¡${updated.petName} subió al nivel ${updated.petLevel}!"
            } else {
                _toastMessage.value = "+60 XP para ${profile.petName} (-20 monedas)."
            }
        }
    }

    fun unlockBadge(badgeId: String, title: String) {
        val profile = currentProfileOrNull() ?: return
        if (profile.unlockedBadges.contains(badgeId)) return
        viewModelScope.launch {
            studyRepo.updateProfileFields(
                mapOf("unlockedBadges" to (profile.unlockedBadges + badgeId).take(30))
            )
            studyRepo.awardStudyProgress(profile, xpEarned = 25L, coinsEarned = 15L)
            _toastMessage.value = "¡Medalla desbloqueada: $title! +25 XP"
        }
    }

    fun toggleLifetimePassPreview() {
        val profile = currentProfileOrNull() ?: return
        viewModelScope.launch {
            val next = !profile.hasLifetimePass
            studyRepo.updateProfileFields(mapOf("hasLifetimePass" to next))
                .onSuccess {
                    _toastMessage.value = if (next) {
                        "Pase Único Vitalicio ($250 MXN) activado: IA ilimitada."
                    } else {
                        "Pase Único desactivado."
                    }
                }
        }
    }

    fun toggleMonthlyAgentSubPreview() {
        val profile = currentProfileOrNull() ?: return
        viewModelScope.launch {
            val next = !profile.hasMonthlyAgentSub
            studyRepo.updateProfileFields(mapOf("hasMonthlyAgentSub" to next))
                .onSuccess {
                    _toastMessage.value = if (next) {
                        "Suscripción Agente Premium Mensual ($149/mes) activada."
                    } else {
                        "Suscripción Agente Premium cancelada."
                    }
                }
        }
    }

    fun runMonthlyAutomatedAgent() {
        val profile = currentProfileOrNull() ?: return
        if (!profile.hasMonthlyAgentSub) {
            _toastMessage.value = "Activa primero la Suscripción Mensual de Agente Premium."
            return
        }
        viewModelScope.launch {
            _automatedAgentState.value = AiCallState.Loading
            aiRepo.runPremiumAutomatedAgent(
                profile = profile,
                subjects = subjectsState.value,
                schoolName = profile.schoolName,
                schoolNews = profile.schoolNews
            ).onSuccess { bundle ->
                val focus = subjectsState.value.filter { it.priority == "reforzar" }
                    .map { it.name }.ifEmpty { listOf("General") }
                studyRepo.createStudyPlan(
                    title = bundle.plan.title,
                    academicLevel = profile.academicLevel,
                    focusSubjects = focus,
                    summary = bundle.plan.summary,
                    modules = bundle.plan.modules,
                    dailyMinutes = 60L,
                    sourceAuthor = "Agente Premium"
                )
                bundle.tasks.forEach { task ->
                    studyRepo.createSmartTask(
                        title = task.title,
                        subject = task.subject,
                        description = task.description,
                        procedureSteps = task.procedureSteps,
                        xpReward = task.xpReward,
                        dueDateLabel = "Auto-Agente"
                    )
                }
                bundle.teachers.forEach { teacher ->
                    studyRepo.upsertSchoolTeacher(
                        name = teacher.name,
                        subject = teacher.subject,
                        focusNote = teacher.focusNote,
                        howToHelp = teacher.howToHelp
                    )
                }
                studyRepo.awardStudyProgress(profile, xpEarned = 40L, coinsEarned = 25L)
                _automatedAgentState.value = AiCallState.Success(bundle.executiveBrief)
                _toastMessage.value = "¡Agente Premium completó tu plan, tareas y profesores en automático!"
            }.onFailure { err ->
                _automatedAgentState.value = AiCallState.Error(
                    err.localizedMessage ?: "No se pudo ejecutar el Agente Automatizado."
                )
            }
        }
    }

    // --- Subjects ---

    fun saveSubject(name: String, grade: Double, targetHours: Long, existingId: String? = null) {
        if (name.isBlank()) {
            _toastMessage.value = "Escribe el nombre de la materia."
            return
        }
        viewModelScope.launch {
            studyRepo.upsertSubject(name, grade, targetHours, existingId)
                .onSuccess { _toastMessage.value = "Materia actualizada. Prioridad detectada automáticamente." }
                .onFailure { _toastMessage.value = it.localizedMessage }
        }
    }

    fun deleteSubject(subjectId: String) {
        viewModelScope.launch {
            studyRepo.deleteSubject(subjectId)
                .onFailure { _toastMessage.value = it.localizedMessage }
        }
    }

    // --- School / Secundaria News & Teachers ---

    fun saveSchoolAndAutomate(schoolName: String, schoolNews: String) {
        if (schoolName.isBlank() || schoolNews.isBlank()) {
            _toastMessage.value = "Escribe el nombre de tu escuela/secundaria y sus noticias."
            return
        }
        viewModelScope.launch {
            val profile = tryConsumeTicket() ?: return@launch
            studyRepo.updateProfileFields(
                mapOf(
                    "schoolName" to schoolName.trim().take(120),
                    "schoolNews" to schoolNews.trim().take(2000)
                )
            )
            _schoolAutomationState.value = AiCallState.Loading
            aiRepo.analyzeSchoolAndTeachers(
                schoolName = schoolName,
                schoolNews = schoolNews,
                academicLevel = profile.academicLevel,
                subjects = subjectsState.value
            ).onSuccess { result ->
                result.suggestedTeachers.forEach { t ->
                    studyRepo.upsertSchoolTeacher(
                        name = t.name,
                        subject = t.subject,
                        focusNote = t.focusNote,
                        howToHelp = t.howToHelp
                    )
                }
                result.suggestedTasks.forEach { t ->
                    studyRepo.createSmartTask(
                        title = t.title,
                        subject = t.subject,
                        description = t.description,
                        procedureSteps = t.steps,
                        xpReward = 45L,
                        dueDateLabel = "Escuela"
                    )
                }
                _schoolAutomationState.value = AiCallState.Success(result)
                _toastMessage.value = "¡Escuela automatizada! Profesores y tareas listos."
            }.onFailure { err ->
                _schoolAutomationState.value = AiCallState.Error(
                    err.localizedMessage ?: "No se pudo analizar las noticias escolares."
                )
            }
        }
    }

    fun addManualTeacher(name: String, subject: String, focusNote: String, howToHelp: String) {
        if (name.isBlank() || subject.isBlank()) {
            _toastMessage.value = "Ingresa nombre del profesor y materia."
            return
        }
        viewModelScope.launch {
            studyRepo.upsertSchoolTeacher(name, subject, focusNote, howToHelp)
                .onSuccess { _toastMessage.value = "Profesor guardado." }
                .onFailure { _toastMessage.value = it.localizedMessage }
        }
    }

    fun deleteTeacher(teacherId: String) {
        viewModelScope.launch {
            studyRepo.deleteSchoolTeacher(teacherId)
        }
    }

    // --- AI Duels ---

    fun startDuel(agent: AiDuelAgent) {
        viewModelScope.launch {
            val profile = tryConsumeTicket() ?: return@launch
            _duelSession.value = DuelSessionState(
                activeAgent = agent,
                round = 1,
                totalRounds = 3,
                playerScore = 0,
                agentScore = 0,
                selectedOption = null,
                pastQuestions = emptyList(),
                isFinished = false
            )
            loadDuelRound(agent, profile.academicLevel, round = 1, pastQuestions = emptyList())
        }
    }

    private suspend fun loadDuelRound(
        agent: AiDuelAgent,
        academicLevel: String,
        round: Int,
        pastQuestions: List<String>
    ) {
        _duelChallengeState.value = AiCallState.Loading
        aiRepo.generateDuelRound(agent, academicLevel, round, pastQuestions)
            .onSuccess { challenge ->
                _duelChallengeState.value = AiCallState.Success(challenge)
            }
            .onFailure { err ->
                _duelChallengeState.value = AiCallState.Error(
                    err.localizedMessage ?: "No se pudo iniciar la ronda."
                )
            }
    }

    fun submitDuelAnswer(chosenIndex: Int) {
        val currentSession = _duelSession.value
        val challenge = (_duelChallengeState.value as? AiCallState.Success)?.data ?: return
        if (currentSession.selectedOption != null) return

        val playerHit = chosenIndex == challenge.correctIndex
        val agentHit = challenge.agentChoiceIndex == challenge.correctIndex
        val nextPlayerScore = currentSession.playerScore + if (playerHit) 1 else 0
        val nextAgentScore = currentSession.agentScore + if (agentHit) 1 else 0
        val finished = currentSession.round >= currentSession.totalRounds

        _duelSession.value = currentSession.copy(
            selectedOption = chosenIndex,
            playerScore = nextPlayerScore,
            agentScore = nextAgentScore,
            pastQuestions = currentSession.pastQuestions + challenge.question,
            isFinished = finished
        )

        if (finished) {
            val profile = currentProfileOrNull()
            if (profile != null) {
                viewModelScope.launch {
                    val won = nextPlayerScore >= nextAgentScore
                    val xp = if (won) 60L else 25L
                    val bonusTicket = if (won) 1L else 0L
                    studyRepo.awardStudyProgress(profile, xpEarned = xp, coinsEarned = 20L, bonusTickets = bonusTicket)
                    _toastMessage.value = if (won) {
                        "¡Venciste a ${currentSession.activeAgent?.name}! +60 XP y +1 ticket IA"
                    } else {
                        "Buen duelo contra ${currentSession.activeAgent?.name}. +25 XP de práctica"
                    }
                }
            }
        }
    }

    fun nextDuelRound() {
        val session = _duelSession.value
        val agent = session.activeAgent ?: return
        val level = currentProfileOrNull()?.academicLevel ?: "Secundaria"
        if (session.isFinished) {
            resetDuel()
            return
        }
        val nextRound = session.round + 1
        _duelSession.value = session.copy(round = nextRound, selectedOption = null)
        viewModelScope.launch {
            loadDuelRound(agent, level, nextRound, session.pastQuestions)
        }
    }

    fun resetDuel() {
        _duelSession.value = DuelSessionState()
        _duelChallengeState.value = AiCallState.Idle
    }

    // --- Arts Creative Mentor ---

    fun generateArtsHelp(discipline: String, topic: String) {
        if (topic.isBlank()) {
            _toastMessage.value = "Describe tu proyecto o duda artística."
            return
        }
        viewModelScope.launch {
            val profile = tryConsumeTicket() ?: return@launch
            _artsGuideState.value = AiCallState.Loading
            aiRepo.generateArtsHelp(discipline, topic, profile.academicLevel)
                .onSuccess { _artsGuideState.value = AiCallState.Success(it) }
                .onFailure {
                    _artsGuideState.value = AiCallState.Error(it.localizedMessage ?: "Error en el estudio de Artes.")
                }
        }
    }

    fun saveArtsGuideAsTask(guide: ArtsGuideResult) {
        viewModelScope.launch {
            studyRepo.createSmartTask(
                title = guide.projectTitle,
                subject = "Artes · ${guide.discipline}",
                description = "Paleta/Estilo: ${guide.paletteOrMood}. Materiales: ${guide.materials.joinToString()}",
                procedureSteps = guide.steps,
                xpReward = 50L,
                dueDateLabel = "Artes"
            ).onSuccess {
                _toastMessage.value = "Proyecto de Artes guardado en Tareas Inteligentes."
            }
        }
    }

    // --- AI Calculator ---

    fun solveWithAiCalculator(problem: String, mode: String) {
        if (problem.isBlank()) {
            _toastMessage.value = "Escribe una operación o problema científico."
            return
        }
        viewModelScope.launch {
            if (tryConsumeTicket() == null) return@launch
            _calcSolutionState.value = AiCallState.Loading
            aiRepo.solveStepByStep(problem, mode)
                .onSuccess { _calcSolutionState.value = AiCallState.Success(it) }
                .onFailure {
                    _calcSolutionState.value = AiCallState.Error(it.localizedMessage ?: "Error en la calculadora IA.")
                }
        }
    }

    // --- AI Study Plan Generator ---

    fun generateAiStudyPlan(academicLevel: String, dailyMinutes: Int, goal: String) {
        viewModelScope.launch {
            val profile = tryConsumeTicket() ?: return@launch
            _isGeneratingPlan.value = true
            val subjects = subjectsState.value
            aiRepo.generateStudyPlan(academicLevel, subjects, dailyMinutes, goal)
                .onSuccess { generated ->
                    val focusList = subjects.filter { it.priority == "reforzar" }
                        .map { it.name }
                        .ifEmpty { subjects.map { it.name }.ifEmpty { listOf("General") } }
                    studyRepo.createStudyPlan(
                        title = generated.title,
                        academicLevel = academicLevel,
                        focusSubjects = focusList,
                        summary = generated.summary,
                        modules = generated.modules,
                        dailyMinutes = dailyMinutes.toLong(),
                        sourceAuthor = "IA MentePro"
                    ).onSuccess {
                        studyRepo.updateProfileFields(mapOf("academicLevel" to academicLevel))
                        studyRepo.awardStudyProgress(profile, xpEarned = 25L, coinsEarned = 15L)
                        _toastMessage.value = "¡Plan creado al minuto con tus materias!"
                    }.onFailure { err ->
                        _toastMessage.value = err.localizedMessage
                    }
                }
                .onFailure { err ->
                    _toastMessage.value = err.localizedMessage ?: "No se pudo crear el plan."
                }
            _isGeneratingPlan.value = false
        }
    }

    fun togglePlanModule(plan: StudyPlan, moduleIndex: Int) {
        viewModelScope.launch {
            studyRepo.toggleStudyPlanModule(plan, moduleIndex).onSuccess {
                currentProfileOrNull()?.let { profile ->
                    studyRepo.awardStudyProgress(profile, xpEarned = 20L, coinsEarned = 10L)
                }
            }
        }
    }

    fun deleteStudyPlan(planId: String) {
        viewModelScope.launch {
            studyRepo.deleteStudyPlan(planId)
        }
    }

    // --- Smart Tasks ---

    fun createAiSmartTask(prompt: String, subject: String) {
        if (prompt.isBlank()) {
            _toastMessage.value = "Describe qué tarea o procedimiento necesitas."
            return
        }
        viewModelScope.launch {
            val profile = tryConsumeTicket() ?: return@launch
            _isGeneratingTask.value = true
            aiRepo.generateSmartTask(prompt, subject, profile.academicLevel)
                .onSuccess { generated ->
                    studyRepo.createSmartTask(
                        title = generated.title,
                        subject = generated.subject,
                        description = generated.description,
                        procedureSteps = generated.procedureSteps,
                        xpReward = generated.xpReward,
                        dueDateLabel = "Hoy"
                    ).onSuccess {
                        _toastMessage.value = "¡Tarea inteligente lista con su procedimiento!"
                    }.onFailure {
                        _toastMessage.value = it.localizedMessage
                    }
                }
                .onFailure {
                    _toastMessage.value = it.localizedMessage ?: "Error generando tarea inteligente."
                }
            _isGeneratingTask.value = false
        }
    }

    fun toggleSmartTaskCompletion(task: SmartTask) {
        viewModelScope.launch {
            val profile = currentProfileOrNull()
            studyRepo.toggleSmartTask(task).onSuccess { nowCompleted ->
                if (nowCompleted && profile != null) {
                    val updated = studyRepo.awardStudyProgress(
                        profile,
                        xpEarned = task.xpReward,
                        coinsEarned = 15L,
                        bonusTickets = 1L
                    ).getOrNull()
                    if (updated != null && updated.petLevel > profile.petLevel) {
                        _toastMessage.value = "¡${updated.petName} subió a nivel ${updated.petLevel}! +1 ticket IA"
                    } else {
                        _toastMessage.value = "+${task.xpReward} XP para tu mascota y +1 ticket IA"
                    }
                    refreshPetEncouragement(updated ?: profile)
                }
            }
        }
    }

    fun deleteSmartTask(taskId: String) {
        viewModelScope.launch { studyRepo.deleteSmartTask(taskId) }
    }

    // --- AI Tutor (Streaming) ---

    fun sendTutorMessage(messageText: String) {
        val clean = messageText.trim()
        if (clean.isEmpty() || _isTutorStreaming.value) return

        viewModelScope.launch {
            val profile = tryConsumeTicket() ?: return@launch
            val userMsg = TutorMessage(id = UUID.randomUUID().toString(), isFromUser = true, text = clean)
            val botId = UUID.randomUUID().toString()
            val placeholderBot = TutorMessage(id = botId, isFromUser = false, text = "", isStreaming = true)
            val snapshotHistory = _tutorMessages.value

            _tutorMessages.value = snapshotHistory + userMsg + placeholderBot
            _isTutorStreaming.value = true

            val weakSubjects = subjectsState.value.filter { it.priority == "reforzar" }.map { it.name }
            try {
                aiRepo.streamTutorReply(snapshotHistory, clean, profile.academicLevel, weakSubjects)
                    .collect { partial ->
                        _tutorMessages.value = _tutorMessages.value.map { msg ->
                            if (msg.id == botId) msg.copy(text = partial, isStreaming = true) else msg
                        }
                    }
                _tutorMessages.value = _tutorMessages.value.map { msg ->
                    if (msg.id == botId) msg.copy(isStreaming = false) else msg
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Tutor stream error", e)
                _tutorMessages.value = _tutorMessages.value.map { msg ->
                    if (msg.id == botId) {
                        msg.copy(
                            text = "Error de conexión con la Tutora IA: ${e.localizedMessage ?: "Intenta de nuevo."}",
                            isStreaming = false
                        )
                    } else msg
                }
            } finally {
                _isTutorStreaming.value = false
            }
        }
    }

    // --- Exams & Minigames ---

    fun generateAiExam(subject: String, topic: String, difficulty: String, count: Int) {
        viewModelScope.launch {
            val profile = tryConsumeTicket() ?: return@launch
            _examState.value = AiCallState.Loading
            aiRepo.generateExam(subject, topic, difficulty, count, profile.academicLevel)
                .onSuccess { _examState.value = AiCallState.Success(it) }
                .onFailure { _examState.value = AiCallState.Error(it.localizedMessage ?: "Error creando examen.") }
        }
    }

    fun submitExamResult(exam: GeneratedExam, correctCount: Int) {
        val total = exam.questions.size.coerceAtLeast(1)
        val score = ((correctCount * 100) / total).toLong()
        viewModelScope.launch {
            val summary = "Obtuviste $correctCount de $total aciertos en ${exam.subject}."
            studyRepo.saveExamResult(
                subject = exam.subject,
                title = exam.title,
                score = score,
                totalQuestions = total.toLong(),
                correctAnswers = correctCount.toLong(),
                difficulty = exam.difficulty,
                feedbackSummary = summary
            )
            currentProfileOrNull()?.let { profile ->
                val bonus = if (score >= 80L) 2L else 0L
                studyRepo.awardStudyProgress(profile, xpEarned = 35L + score / 2L, coinsEarned = 20L, bonusTickets = bonus)
                _toastMessage.value = if (bonus > 0) {
                    "¡Excelente ($score%)! +2 tickets IA y XP para ${profile.petName}"
                } else {
                    "Examen guardado ($score%). +XP para ${profile.petName}"
                }
            }
        }
    }

    fun resetExam() {
        _examState.value = AiCallState.Idle
    }

    fun generateMatchMinigame(subject: String) {
        viewModelScope.launch {
            val profile = tryConsumeTicket() ?: return@launch
            _matchPairsState.value = AiCallState.Loading
            aiRepo.generateMatchPairs(subject, profile.academicLevel)
                .onSuccess { _matchPairsState.value = AiCallState.Success(it) }
                .onFailure { _matchPairsState.value = AiCallState.Error(it.localizedMessage ?: "Error generando minijuego.") }
        }
    }

    fun rewardMinigameWin(gameLabel: String) {
        viewModelScope.launch {
            currentProfileOrNull()?.let { profile ->
                studyRepo.awardStudyProgress(profile, xpEarned = 30L, coinsEarned = 15L, bonusTickets = 1L)
                _toastMessage.value = "¡$gameLabel completado! +30 XP y +1 ticket IA"
            }
        }
    }

    // --- Slide Camera Helper ---

    fun analyzeSlideImage(bitmap: Bitmap, questionHint: String) {
        viewModelScope.launch {
            if (tryConsumeTicket() == null) return@launch
            _slideAnalysisState.value = AiCallState.Loading
            aiRepo.analyzeSlideBitmap(bitmap, questionHint)
                .onSuccess { _slideAnalysisState.value = AiCallState.Success(it) }
                .onFailure { _slideAnalysisState.value = AiCallState.Error(it.localizedMessage ?: "No se pudo analizar la imagen.") }
        }
    }

    fun saveSlideAsSmartTask(analysis: SlideAnalysisResult) {
        viewModelScope.launch {
            studyRepo.createSmartTask(
                title = analysis.suggestedTaskTitle,
                subject = "Diapositiva",
                description = analysis.simpleExplanation,
                procedureSteps = analysis.suggestedTaskSteps,
                xpReward = 45L,
                dueDateLabel = "Repaso"
            ).onSuccess {
                _toastMessage.value = "Diapositiva guardada como Tarea Inteligente."
            }
        }
    }

    // --- Pet & Chess Coach ---

    fun refreshPetEncouragement(overrideProfile: StudentProfile? = null) {
        val profile = overrideProfile ?: currentProfileOrNull() ?: return
        val weak = subjectsState.value.firstOrNull { it.priority == "reforzar" }?.name
        val pending = smartTasksState.value.count { !it.completed }
        viewModelScope.launch {
            aiRepo.generatePetEncouragement(profile.petName, profile.petLevel, weak, pending)
                .onSuccess { _petMessage.value = it }
        }
    }

    fun askChessCoach(puzzle: ChessPuzzle, question: String) {
        viewModelScope.launch {
            if (tryConsumeTicket() == null) return@launch
            _chessCoachState.value = AiCallState.Loading
            aiRepo.explainChessMove(puzzle, question)
                .onSuccess { _chessCoachState.value = AiCallState.Success(it) }
                .onFailure { _chessCoachState.value = AiCallState.Error(it.localizedMessage ?: "Error del entrenador de ajedrez.") }
        }
    }

    // --- Community Workshop ---

    fun publishToWorkshop(
        title: String,
        category: String,
        subject: String,
        description: String,
        steps: List<String>
    ) {
        val profile = currentProfileOrNull() ?: return
        if (title.isBlank() || description.isBlank()) {
            _toastMessage.value = "Completa el título y descripción para publicar."
            return
        }
        viewModelScope.launch {
            studyRepo.publishWorkshopItem(
                authorName = profile.displayName,
                title = title,
                category = category,
                subject = subject.ifBlank { "General" },
                academicLevel = profile.academicLevel,
                description = description,
                steps = steps.ifEmpty { listOf(description) }
            ).onSuccess {
                studyRepo.awardStudyProgress(profile, xpEarned = 35L, coinsEarned = 25L)
                _toastMessage.value = "¡Publicado en la Workshop! +35 XP"
            }.onFailure {
                _toastMessage.value = it.localizedMessage
            }
        }
    }

    fun downloadFromWorkshop(item: WorkshopItem) {
        viewModelScope.launch {
            studyRepo.downloadWorkshopItemToUser(item)
                .onSuccess {
                    _toastMessage.value = if (item.category == "plan") {
                        "Plan '${item.title}' descargado a tus Planes."
                    } else {
                        "'${item.title}' agregado a tus Tareas Inteligentes."
                    }
                }
                .onFailure { _toastMessage.value = it.localizedMessage }
        }
    }

    fun likeWorkshopItem(itemId: String) {
        viewModelScope.launch {
            studyRepo.likeWorkshopItem(itemId)
        }
    }

    // --- Versión 2: Cámara de Tareas, Plan Alto 18+ (Finanzas, Rutas, Asistente), Código/Juegos ---

    fun scanHomeworkImage(bitmap: Bitmap, hint: String) {
        viewModelScope.launch {
            if (tryConsumeTicket() == null) return@launch
            _homeworkScanState.value = AiCallState.Loading
            aiRepo.solveHomeworkBitmap(bitmap, hint)
                .onSuccess { res ->
                    _homeworkScanState.value = AiCallState.Success(res)
                    currentProfileOrNull()?.let { prof ->
                        studyRepo.awardStudyProgress(prof, xpEarned = 20L, coinsEarned = 10L)
                    }
                }
                .onFailure { err ->
                    _homeworkScanState.value = AiCallState.Error(
                        err.localizedMessage ?: "No se pudo resolver la tarea con cámara."
                    )
                }
        }
    }

    fun saveHomeworkAsTask(scan: HomeworkScanResult) {
        viewModelScope.launch {
            studyRepo.createSmartTask(
                title = scan.suggestedTaskTitle.ifBlank { scan.problemStatement.take(60) },
                subject = scan.subject.ifBlank { "Tarea Cámara" },
                description = "Resultado: ${scan.finalAnswer}\n${scan.problemStatement}",
                procedureSteps = scan.steps,
                xpReward = 50L,
                dueDateLabel = "Cámara IA"
            ).onSuccess {
                _toastMessage.value = "Tarea resuelta guardada en tus Tareas Inteligentes."
            }.onFailure {
                _toastMessage.value = it.localizedMessage
            }
        }
    }

    fun updateHigherEdProfile(ageYears: Long, academicLevel: String, degreeOrMaster: String) {
        viewModelScope.launch {
            val cleanAge = ageYears.coerceIn(10L, 100L)
            val cleanDegree = degreeOrMaster.trim().take(120)
            studyRepo.updateProfileFields(
                mapOf(
                    "ageYears" to cleanAge,
                    "academicLevel" to academicLevel,
                    "degreeOrMaster" to cleanDegree,
                    "sessionMode" to if (cleanAge >= 18L) "numeric" else (currentProfileOrNull()?.sessionMode ?: "gamification")
                )
            ).onSuccess {
                _toastMessage.value = if (cleanAge >= 18L) {
                    "Perfil Plan Alto 18+ actualizado (Modo Serio/Ejecutivo)."
                } else {
                    "Perfil académico actualizado."
                }
            }.onFailure {
                _toastMessage.value = it.localizedMessage
            }
        }
    }

    fun addFinanceEntry(
        title: String,
        amount: Double,
        entryType: String,
        category: String,
        note: String
    ) {
        if (title.isBlank() || amount <= 0.0) {
            _toastMessage.value = "Ingresa concepto y monto válido."
            return
        }
        viewModelScope.launch {
            studyRepo.addFinanceEntry(title, amount, entryType, category, note)
                .onSuccess { _toastMessage.value = "Registro financiero guardado." }
                .onFailure { _toastMessage.value = it.localizedMessage }
        }
    }

    fun deleteFinanceEntry(entryId: String) {
        viewModelScope.launch {
            studyRepo.deleteFinanceEntry(entryId)
                .onFailure { _toastMessage.value = it.localizedMessage }
        }
    }

    fun planUniversityRoute(origin: String, destination: String, transportMode: String) {
        if (origin.isBlank() || destination.isBlank()) {
            _toastMessage.value = "Escribe tu punto de partida y tu universidad o destino."
            return
        }
        viewModelScope.launch {
            val profile = tryConsumeTicket() ?: return@launch
            _routePlanState.value = AiCallState.Loading
            aiRepo.generateUniversityRoute(
                origin = origin.trim(),
                destination = destination.trim(),
                transportMode = transportMode,
                degreeOrMaster = profile.degreeOrMaster
            ).onSuccess {
                _routePlanState.value = AiCallState.Success(it)
            }.onFailure { err ->
                _routePlanState.value = AiCallState.Error(
                    err.localizedMessage ?: "No se pudo trazar la ruta."
                )
            }
        }
    }

    fun runExecutivePersonalAdvisor(decisionQuery: String, autoCreateTasks: Boolean) {
        viewModelScope.launch {
            val profile = tryConsumeTicket() ?: return@launch
            _executiveAdvisorState.value = AiCallState.Loading
            aiRepo.generateExecutiveAdvisor(
                profile = profile,
                finances = financeEntriesState.value,
                tasks = smartTasksState.value,
                decisionQuery = decisionQuery
            ).onSuccess { res ->
                _executiveAdvisorState.value = AiCallState.Success(res)
                if (autoCreateTasks) {
                    res.automationActions.take(2).forEach { action ->
                        studyRepo.createSmartTask(
                            title = action.take(90),
                            subject = profile.degreeOrMaster.ifBlank { "Plan Alto 18+" },
                            description = res.dailyPriorityPlan,
                            procedureSteps = listOf(action, "Verificar avance en tu horario de hoy"),
                            xpReward = 45L,
                            dueDateLabel = "Auto-18+"
                        )
                    }
                    _toastMessage.value = "Asistente 18+: recomendaciones y tareas automatizadas."
                }
            }.onFailure { err ->
                _executiveAdvisorState.value = AiCallState.Error(
                    err.localizedMessage ?: "No se pudo consultar al asistente ejecutivo."
                )
            }
        }
    }

    fun generatePlayableCode(prompt: String, kind: String) {
        if (prompt.isBlank()) {
            _toastMessage.value = "Describe el videojuego o app que quieres crear."
            return
        }
        viewModelScope.launch {
            val profile = tryConsumeTicket() ?: return@launch
            _codePlaygroundState.value = AiCallState.Loading
            aiRepo.generateInstantPlayableCode(prompt.trim(), kind)
                .onSuccess { res ->
                    _codePlaygroundState.value = AiCallState.Success(res)
                    studyRepo.awardStudyProgress(profile, xpEarned = 35L, coinsEarned = 20L)
                }
                .onFailure { err ->
                    _codePlaygroundState.value = AiCallState.Error(
                        err.localizedMessage ?: "No se pudo generar el proyecto interactivo."
                    )
                }
        }
    }

    // --- Protón 2: Conectores API / Cloud Code, Mascota Base, Clima/Navidad Easter Eggs, Play Billing, Admin ---

    fun saveAiConnectorConfig(config: AiConnectorConfig) {
        apiKeyVault?.saveConfig(config)
        _aiConnectorConfig.value = config
        aiRepo.updateConnectorConfig(config)
        _connectorTestState.value = AiCallState.Idle
        _toastMessage.value = "Conector guardado localmente: ${config.activeLabel()}"
    }

    fun clearAiConnectorKeys() {
        apiKeyVault?.clearKeys()
        val reset = apiKeyVault?.loadConfig() ?: AiConnectorConfig()
        _aiConnectorConfig.value = reset
        aiRepo.updateConnectorConfig(reset)
        _connectorTestState.value = AiCallState.Idle
        _toastMessage.value = "Claves API locales eliminadas. Usando Firebase AI Logic."
    }

    fun testAiConnector(config: AiConnectorConfig) {
        viewModelScope.launch {
            _connectorTestState.value = AiCallState.Loading
            aiRepo.testExternalConnector(config)
                .onSuccess { msg ->
                    _connectorTestState.value = AiCallState.Success(msg)
                }
                .onFailure { err ->
                    _connectorTestState.value = AiCallState.Error(
                        err.localizedMessage ?: "No se pudo validar la clave API."
                    )
                }
        }
    }

    fun applyMascotaBaseConfig(species: String = "autosauro") {
        val validSpecies = if (species in listOf("buho", "zorro", "dragon", "gato", "autosauro")) species else "autosauro"
        val profile = currentProfileOrNull() ?: return
        viewModelScope.launch {
            val defaultName = if (validSpecies == "autosauro" && profile.petName == "Bubo") "RexBot" else profile.petName
            studyRepo.updateProfileFields(
                mapOf(
                    "petName" to defaultName,
                    "petSpecies" to validSpecies,
                    "petAccessory" to "ninguno",
                    "petColorAccent" to "esmeralda"
                )
            ).onSuccess {
                _toastMessage.value = "Configuración Mascota Base aplicada ($validSpecies)."
            }.onFailure {
                _toastMessage.value = it.localizedMessage
            }
        }
    }

    fun setWeatherSeasonOverride(seasonKey: String) {
        val valid = if (seasonKey in listOf("auto", "navidad", "lluvia", "soleado", "otono")) seasonKey else "auto"
        viewModelScope.launch {
            studyRepo.updateProfileFields(mapOf("weatherSeasonOverride" to valid))
                .onSuccess {
                    val info = SeasonalThemeManager.resolveSeason(valid)
                    _toastMessage.value = if (info.isChristmasMode) {
                        "¡Modo Navidad Protón 2 activado en toda la app!"
                    } else {
                        "Clima actualizado: ${info.badgeText}"
                    }
                }
                .onFailure { _toastMessage.value = it.localizedMessage }
        }
    }

    fun claimSeasonalEgg() {
        val profile = currentProfileOrNull() ?: return
        val season = seasonalInfoState.value
        val badgeId = "huevo_${season.seasonId}"
        if (profile.unlockedBadges.contains(badgeId)) {
            _toastMessage.value = "Ya abriste el ${season.eggName} de esta temporada. ¡Cambia de clima o espera la siguiente!"
            return
        }
        val bonusCoins = if (season.isChristmasMode) 100L else 75L
        val bonusTickets = if (season.seasonId in listOf("navidad", "lluvia")) 2L else 1L
        val bonusXp = 50L
        viewModelScope.launch {
            studyRepo.updateProfileFields(
                mapOf("unlockedBadges" to (profile.unlockedBadges + badgeId).take(30))
            )
            studyRepo.awardStudyProgress(
                currentProfile = profile.copy(unlockedBadges = (profile.unlockedBadges + badgeId).take(30)),
                xpEarned = bonusXp,
                coinsEarned = bonusCoins,
                bonusTickets = bonusTickets
            )
            _toastMessage.value = "¡Eclosionaste ${season.eggName}! +$bonusCoins monedas, +$bonusTickets tickets IA y +$bonusXp XP"
        }
    }

    fun triggerProton2EasterEgg() {
        val profile = currentProfileOrNull() ?: return
        if (profile.unlockedBadges.contains("cazador_easter_eggs")) {
            _toastMessage.value = "¡Protón 2 Secret Mode! Autosauro Rex te envía energía extra."
            return
        }
        viewModelScope.launch {
            val nextBadges = (profile.unlockedBadges + "cazador_easter_eggs").take(30)
            studyRepo.updateProfileFields(mapOf("unlockedBadges" to nextBadges))
            studyRepo.awardStudyProgress(
                currentProfile = profile.copy(unlockedBadges = nextBadges),
                xpEarned = 60L,
                coinsEarned = 50L,
                bonusTickets = 3L
            )
            _toastMessage.value = "¡Easter Egg Protón 2 descubierto! +50 monedas, +3 tickets IA y medalla secreta."
        }
    }

    fun syncPlayBillingPurchases(productIds: List<String>) {
        if (productIds.isEmpty()) return
        val profile = currentProfileOrNull() ?: return
        viewModelScope.launch {
            val fields = mutableMapOf<String, Any>()
            if (productIds.contains(PlayBillingManager.SKU_EXTRA_25)) {
                fields["hasProtonExtra25"] = true
                if (profile.ticketsRemaining < StudyRepository.PROTON_EXTRA_DAILY_TICKETS) {
                    fields["ticketsRemaining"] = StudyRepository.PROTON_EXTRA_DAILY_TICKETS.toLong()
                }
            }
            if (productIds.contains(PlayBillingManager.SKU_LIFETIME_250)) {
                fields["hasLifetimePass"] = true
            }
            if (productIds.contains(PlayBillingManager.SKU_MONTHLY_AGENT)) {
                fields["hasMonthlyAgentSub"] = true
            }
            if (fields.isNotEmpty()) {
                studyRepo.updateProfileFields(fields)
                    .onSuccess { _toastMessage.value = "Suscripciones y pases sincronizados con Google Play." }
            }
        }
    }

    // --- Admin Exclusivo (villapatoclow@gmail.com) ---

    fun inspectUserAdmin(targetUserId: String) {
        if (!isAdmin) return
        viewModelScope.launch {
            _inspectedUserState.value = AiCallState.Loading
            studyRepo.inspectUserBundleAdmin(targetUserId)
                .onSuccess { _inspectedUserState.value = AiCallState.Success(it) }
                .onFailure { err ->
                    _inspectedUserState.value = AiCallState.Error(err.localizedMessage ?: "No se pudo inspeccionar usuario.")
                }
        }
    }

    fun adminToggleUserEntitlement(targetUserId: String, field: String, value: Any) {
        if (!isAdmin) return
        viewModelScope.launch {
            studyRepo.adminUpdateUserProfile(targetUserId, mapOf(field to value))
                .onSuccess {
                    _toastMessage.value = "Usuario actualizado ($field = $value)."
                    inspectUserAdmin(targetUserId)
                }
                .onFailure { _toastMessage.value = it.localizedMessage }
        }
    }

    fun adminDeleteUserSubItem(targetUserId: String, subcollection: String, docId: String) {
        if (!isAdmin) return
        viewModelScope.launch {
            studyRepo.adminDeleteUserSubItem(targetUserId, subcollection, docId)
                .onSuccess {
                    _toastMessage.value = "Elemento eliminado ($subcollection)."
                    inspectUserAdmin(targetUserId)
                }
                .onFailure { _toastMessage.value = it.localizedMessage }
        }
    }

    fun adminDeleteUser(targetUserId: String) {
        if (!isAdmin) return
        viewModelScope.launch {
            studyRepo.adminDeleteUserDocument(targetUserId)
                .onSuccess {
                    _inspectedUserState.value = AiCallState.Idle
                    _toastMessage.value = "Cuenta de usuario y registros eliminados."
                }
                .onFailure { _toastMessage.value = it.localizedMessage }
        }
    }

    fun adminDeleteWorkshopItem(itemId: String) {
        if (!isAdmin) return
        viewModelScope.launch {
            studyRepo.adminDeleteWorkshopItem(itemId)
                .onSuccess { _toastMessage.value = "Publicación eliminada de la Workshop." }
                .onFailure { _toastMessage.value = it.localizedMessage }
        }
    }

    private companion object {
        const val TAG = "MenteProVM"
    }
}
