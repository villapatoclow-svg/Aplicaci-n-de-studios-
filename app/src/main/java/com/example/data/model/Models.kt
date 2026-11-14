package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class StudentProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "Estudiante",
    val academicLevel: String = "Secundaria", // Primaria | Secundaria | Preparatoria | Universidad | Posgrado
    val sessionMode: String = "gamification", // "gamification" | "numeric"
    val themeAccent: String = "indigo", // "indigo" | "emerald" | "amber" | "obsidian"
    val petName: String = "Bubo",
    val petLevel: Long = 1L,
    val petXp: Long = 0L,
    val streakDays: Long = 1L,
    val studyCoins: Long = 50L,
    val ticketsRemaining: Long = 15L,
    val lastTicketDate: String = "2026-11-14",
    val hasLifetimePass: Boolean = false,
    val hasMonthlyAgentSub: Boolean = false,
    val hasProtonExtra25: Boolean = false,
    val schoolName: String = "",
    val schoolNews: String = "",
    val studyGroupCode: String = "",
    val ageYears: Long = 16L,
    val degreeOrMaster: String = "",
    val petSpecies: String = "buho", // "buho" | "zorro" | "dragon" | "gato" | "autosauro"
    val petAccessory: String = "ninguno", // "ninguno" | "lentes" | "birrete" | "audifonos" | "corona"
    val petColorAccent: String = "indigo", // "indigo" | "esmeralda" | "dorado" | "rosa"
    val weatherSeasonOverride: String = "auto", // "auto" | "navidad" | "lluvia" | "soleado" | "otono"
    val unlockedBadges: List<String> = listOf("inicio"),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val petStageTitle: String
        get() = when {
            petLevel >= 10 -> "Sabio Cósmico"
            petLevel >= 6 -> "Maestro Erudito"
            petLevel >= 3 -> "Aprendiz Avanzado"
            else -> "Explorador Inicial"
        }

    val xpForNextLevel: Long
        get() = petLevel * 100L

    val hasUnlimitedAi: Boolean
        get() = hasLifetimePass || hasMonthlyAgentSub

    val isAdultOrHigherEd: Boolean
        get() = ageYears >= 18L || academicLevel in listOf("Preparatoria", "Universidad", "Posgrado")
}

@IgnoreExtraProperties
data class SubjectRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val grade: Double = 8.0,
    val priority: String = "estable", // "reforzar" | "estable" | "fuerte"
    val targetHoursPerWeek: Long = 4L,
    val completedMinutes: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    companion object {
        fun computePriority(grade: Double): String = when {
            grade < 7.0 -> "reforzar"
            grade < 8.5 -> "estable"
            else -> "fuerte"
        }
    }
}

@IgnoreExtraProperties
data class SchoolTeacher(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val subject: String = "",
    val focusNote: String = "",
    val howToHelp: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class StudyModule(
    val title: String = "",
    val subject: String = "",
    val durationMinutes: Long = 30L,
    val completed: Boolean = false,
    val activities: List<String> = emptyList()
)

@IgnoreExtraProperties
data class StudyPlan(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val academicLevel: String = "Secundaria",
    val focusSubjects: List<String> = emptyList(),
    val summary: String = "",
    val modules: List<StudyModule> = emptyList(),
    val dailyMinutes: Long = 60L,
    val progressPercent: Long = 0L,
    val sourceAuthor: String = "IA MentePro",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class SmartTask(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val subject: String = "",
    val description: String = "",
    val procedureSteps: List<String> = emptyList(),
    val completed: Boolean = false,
    val xpReward: Long = 40L,
    val dueDateLabel: String = "Hoy",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ExamResult(
    @DocumentId val id: String = "",
    val userId: String = "",
    val subject: String = "",
    val title: String = "",
    val score: Long = 0L,
    val totalQuestions: Long = 5L,
    val correctAnswers: Long = 0L,
    val difficulty: String = "intermedio",
    val feedbackSummary: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class WorkshopItem(
    @DocumentId val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val title: String = "",
    val category: String = "plan", // "plan" | "actividad" | "examen" | "guia"
    val subject: String = "",
    val academicLevel: String = "Secundaria",
    val description: String = "",
    val steps: List<String> = emptyList(),
    val visibility: String = "public",
    val downloadsCount: Long = 0L,
    val likesCount: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

// --- AI & Interactive Domain Models ---

data class TutorMessage(
    val id: String,
    val isFromUser: Boolean,
    val text: String,
    val isStreaming: Boolean = false
)

data class StepByStepSolution(
    val finalAnswer: String,
    val subjectArea: String,
    val formulaUsed: String,
    val steps: List<String>,
    val studyTip: String
)

data class ExamQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class GeneratedExam(
    val title: String,
    val subject: String,
    val difficulty: String,
    val questions: List<ExamQuestion>
)

data class MatchPair(
    val id: String,
    val term: String,
    val definition: String
)

data class SlideAnalysisResult(
    val title: String,
    val keyPoints: List<String>,
    val simpleExplanation: String,
    val suggestedTaskTitle: String,
    val suggestedTaskSteps: List<String>
)

data class ChessPuzzle(
    val id: String,
    val title: String,
    val theme: String,
    val sideToMove: String,
    val prompt: String,
    val boardFen: String,
    val solutionFrom: Pair<Int, Int>,
    val solutionTo: Pair<Int, Int>,
    val solutionMoveSan: String,
    val explanation: String
)

// --- New Models: AI Duels, Arts Help, School Automation, Subject Modules ---

data class AiDuelAgent(
    val id: String,
    val name: String,
    val title: String,
    val subject: String,
    val tagline: String,
    val levelLabel: String
)

data class DuelChallenge(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val agentChoiceIndex: Int,
    val agentTaunt: String,
    val explanation: String
)

data class ArtsGuideResult(
    val projectTitle: String,
    val discipline: String,
    val materials: List<String>,
    val paletteOrMood: String,
    val steps: List<String>,
    val masteryTip: String
)

data class SchoolAutomationResult(
    val actionSummary: String,
    val suggestedTeachers: List<SchoolTeacherDraft>,
    val suggestedTasks: List<SchoolTaskDraft>
)

data class SchoolTeacherDraft(
    val name: String,
    val subject: String,
    val focusNote: String,
    val howToHelp: String
)

data class SchoolTaskDraft(
    val title: String,
    val subject: String,
    val description: String,
    val steps: List<String>
)

data class SubjectCatalogModule(
    val id: String,
    val level: String, // Primaria | Secundaria | Preparatoria | Universidad
    val name: String,
    val category: String, // Ciencias | Matemáticas | Artes | Humanidades | Tecnología
    val description: String,
    val keyTopics: List<String>,
    val specialToolType: String // "calculator" | "arts" | "tutor" | "duel"
)

// --- Versión 2 Models: Finanzas 18+, Cámara Tareas, Rutas Campus, Asistente Personal, Código/Juegos, Gamificación ---

@IgnoreExtraProperties
data class FinanceEntry(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val entryType: String = "gasto", // "ingreso" | "gasto" | "bien"
    val category: String = "General",
    val note: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class HomeworkScanResult(
    val subject: String,
    val problemStatement: String,
    val finalAnswer: String,
    val steps: List<String>,
    val verificationTip: String,
    val suggestedTaskTitle: String
)

data class RouteStepInfo(
    val instruction: String,
    val streetOrLine: String,
    val distanceOrDuration: String
)

data class RoutePlanResult(
    val origin: String,
    val destination: String,
    val transportMode: String,
    val totalEstimatedTime: String,
    val estimatedCost: String,
    val routeSteps: List<RouteStepInfo>,
    val streetHighlights: List<String>,
    val tips: String
)

data class PurchaseAdvice(
    val item: String,
    val estimatedCost: String,
    val reason: String
)

data class ExecutiveAdvisorResult(
    val dailyPriorityPlan: String,
    val buyList: List<PurchaseAdvice>,
    val avoidBuyingList: List<PurchaseAdvice>,
    val automationActions: List<String>
)

data class CodePlaygroundResult(
    val title: String,
    val kind: String, // "game" | "app"
    val summary: String,
    val htmlPreview: String,
    val codeSnippet: String,
    val instructions: String
)

data class GamificationMission(
    val id: String,
    val title: String,
    val description: String,
    val xpReward: Long,
    val coinReward: Long,
    val completed: Boolean,
    val progressText: String
)

// --- Protón 2 Models: Conectores API / Cloud Code, Clima/Easter Eggs, Play Billing y Admin ---

data class AiConnectorConfig(
    val activeProvider: String = "firebase_ai", // "firebase_ai" | "gemini_byok" | "cloud_code" | "openai_compatible"
    val geminiApiKey: String = "",
    val geminiModel: String = "gemini-2.5-flash",
    val cloudCodeApiKey: String = "",
    val cloudCodeEndpoint: String = "https://api.anthropic.com/v1/messages",
    val cloudCodeModel: String = "claude-sonnet-4-20250514",
    val customApiKey: String = "",
    val customBaseUrl: String = "https://api.openai.com/v1/chat/completions",
    val customModel: String = "gpt-4o-mini"
) {
    fun activeLabel(): String = when (activeProvider) {
        "gemini_byok" -> "Gemini API ($geminiModel)"
        "cloud_code" -> "Cloud Code / Claude ($cloudCodeModel)"
        "openai_compatible" -> "Servicio Externo ($customModel)"
        else -> "Firebase AI Logic (Protón 2)"
    }
}

data class SeasonalThemeInfo(
    val seasonId: String, // "navidad" | "lluvia" | "soleado" | "otono"
    val title: String,
    val badgeText: String,
    val bannerMessage: String,
    val eggName: String,
    val eggRewardNote: String,
    val isChristmasMode: Boolean
)

data class PlayProductOfferUi(
    val productId: String,
    val title: String,
    val description: String,
    val fallbackPriceLabel: String,
    val livePlayPrice: String?,
    val isSubscription: Boolean,
    val isAvailableInPlayStore: Boolean
)

data class AdminUserInspectBundle(
    val profile: StudentProfile,
    val tasks: List<SmartTask>,
    val plans: List<StudyPlan>,
    val subjects: List<SubjectRecord>,
    val finances: List<FinanceEntry>
)

