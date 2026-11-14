package com.example.data.repository

import com.example.data.model.AdminUserInspectBundle
import com.example.data.model.ExamResult
import com.example.data.model.FinanceEntry
import com.example.data.model.SchoolTeacher
import com.example.data.model.SmartTask
import com.example.data.model.StudentProfile
import com.example.data.model.StudyModule
import com.example.data.model.StudyPlan
import com.example.data.model.SubjectRecord
import com.example.data.model.WorkshopItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Almacén completamente en memoria para el modo Visitante (sin llamadas a Firestore ni Auth anónima).
 * Permite explorar todas las herramientas, materias, escuela y suscripciones de vista previa.
 */
class VisitorStudyStore : StudyDataStore {

    private val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private val profileFlow = MutableStateFlow<StudentProfile?>(
        StudentProfile(
            id = "visitante",
            userId = "visitante",
            displayName = "Visitante",
            academicLevel = "Secundaria",
            sessionMode = "gamification",
            themeAccent = "indigo",
            petName = "Bubo",
            petLevel = 2L,
            petXp = 45L,
            streakDays = 1L,
            studyCoins = 80L,
            ticketsRemaining = 15L,
            lastTicketDate = today,
            hasLifetimePass = false,
            hasMonthlyAgentSub = false,
            hasProtonExtra25 = false,
            schoolName = "Secundaria Técnica #14",
            schoolNews = "Esta semana hay feria de ciencias el viernes y examen parcial de Matemáticas el miércoles.",
            ageYears = 18L,
            degreeOrMaster = "Preparatoria / Ingeniería en Computación",
            petSpecies = "buho",
            petAccessory = "lentes",
            petColorAccent = "indigo",
            weatherSeasonOverride = "auto",
            unlockedBadges = listOf("inicio", "primer_paso")
        )
    )

    private val financeEntriesFlow = MutableStateFlow(
        listOf(
            FinanceEntry(
                id = "fin_demo_1",
                userId = "visitante",
                title = "Beca / Ingreso Mensual",
                amount = 4200.0,
                entryType = "ingreso",
                category = "Beca",
                note = "Apoyo mensual para transporte, materiales y estudio"
            ),
            FinanceEntry(
                id = "fin_demo_2",
                userId = "visitante",
                title = "Laptop y Tablet de Estudio",
                amount = 11500.0,
                entryType = "bien",
                category = "Equipo",
                note = "Patrimonio académico principal"
            ),
            FinanceEntry(
                id = "fin_demo_3",
                userId = "visitante",
                title = "Transporte y Copias",
                amount = 650.0,
                entryType = "gasto",
                category = "Movilidad",
                note = "Pasajes de la semana y material impreso"
            )
        )
    )

    private val subjectsFlow = MutableStateFlow(
        listOf(
            SubjectRecord(
                id = "sub_math",
                userId = "visitante",
                name = "Matemáticas",
                grade = 6.4,
                priority = "reforzar",
                targetHoursPerWeek = 6L
            ),
            SubjectRecord(
                id = "sub_arts",
                userId = "visitante",
                name = "Artes Visuales",
                grade = 9.2,
                priority = "fuerte",
                targetHoursPerWeek = 3L
            ),
            SubjectRecord(
                id = "sub_sci",
                userId = "visitante",
                name = "Física y Química",
                grade = 7.8,
                priority = "estable",
                targetHoursPerWeek = 4L
            )
        )
    )

    private val teachersFlow = MutableStateFlow(
        listOf(
            SchoolTeacher(
                id = "teach_1",
                userId = "visitante",
                name = "Prof. Ricardo Morales",
                subject = "Matemáticas",
                focusNote = "Prepara evaluación parcial de ecuaciones lineales el miércoles.",
                howToHelp = "Organiza una hoja de fórmulas limpias con 3 ejemplos resueltos para compartir al grupo antes del repaso."
            ),
            SchoolTeacher(
                id = "teach_2",
                userId = "visitante",
                name = "Mtra. Sofía Herrera",
                subject = "Ciencias / Feria",
                focusNote = "Coordina los equipos de la feria de ciencias del viernes.",
                howToHelp = "Lleva una maqueta o cartel visual claro con hipótesis, materiales y conclusión en 3 puntos."
            )
        )
    )

    private val plansFlow = MutableStateFlow(
        listOf(
            StudyPlan(
                id = "plan_demo_1",
                userId = "visitante",
                title = "Ruta de Refuerzo: Álgebra y Ciencias",
                academicLevel = "Secundaria",
                focusSubjects = listOf("Matemáticas", "Física y Química"),
                summary = "Plan inicial de visitante enfocado en subir Matemáticas arriba de 8.0 con bloques de 30 minutos.",
                modules = listOf(
                    StudyModule(
                        title = "Despeje de ecuaciones lineales",
                        subject = "Matemáticas",
                        durationMinutes = 30L,
                        completed = true,
                        activities = listOf("Revisar signos al cambiar de lado", "Resolver 4 ejercicios guiados")
                    ),
                    StudyModule(
                        title = "Velocidad y aceleración básica",
                        subject = "Física y Química",
                        durationMinutes = 30L,
                        completed = false,
                        activities = listOf("Practicar fórmula v = d/t en Calculadora IA")
                    )
                ),
                dailyMinutes = 60L,
                progressPercent = 50L,
                sourceAuthor = "IA MentePro"
            )
        )
    )

    private val tasksFlow = MutableStateFlow(
        listOf(
            SmartTask(
                id = "task_demo_1",
                userId = "visitante",
                title = "Guía rápida para el parcial de ecuaciones",
                subject = "Matemáticas",
                description = "Práctica guiada paso a paso para dominar ecuaciones de primer grado.",
                procedureSteps = listOf(
                    "Agrupa los términos con x de un lado del signo igual.",
                    "Agrupa las constantes numéricas del otro lado cambiando su signo.",
                    "Divide ambos lados entre el coeficiente de x y comprueba sustituyendo."
                ),
                completed = false,
                xpReward = 45L,
                dueDateLabel = "Miércoles"
            )
        )
    )

    private val examResultsFlow = MutableStateFlow<List<ExamResult>>(emptyList())

    private val workshopFlow = MutableStateFlow(
        listOf(
            WorkshopItem(
                id = "ws_demo_1",
                authorId = "comunidad_1",
                authorName = "Valeria G.",
                title = "Guía Maestra de Fracciones y Álgebra",
                category = "plan",
                subject = "Matemáticas",
                academicLevel = "Secundaria",
                description = "Secuencia de 4 pasos para pasar de fracciones a ecuaciones sin enredos.",
                steps = listOf(
                    "Equivalencias y simplificación rápida",
                    "Suma con mínimo común múltiplo",
                    "Ecuaciones con denominadores",
                    "Miniexamen de comprobación"
                ),
                downloadsCount = 42L,
                likesCount = 19L
            ),
            WorkshopItem(
                id = "ws_demo_2",
                authorId = "comunidad_2",
                authorName = "Diego M.",
                title = "Proyecto Creativo de Teoría del Color",
                category = "guia",
                subject = "Artes Visuales",
                academicLevel = "Secundaria",
                description = "Cómo combinar colores complementarios y análogos para láminas escolares.",
                steps = listOf(
                    "Trazar círculo cromático base",
                    "Elegir paleta cálida o fría según emoción",
                    "Aplicar sombras con tonos complementarios"
                ),
                downloadsCount = 27L,
                likesCount = 14L
            )
        )
    )

    override fun observeProfile(userId: String): Flow<StudentProfile?> = profileFlow

    override suspend fun ensureProfileInitialized(displayName: String): Result<StudentProfile> {
        val current = profileFlow.value!!
        return Result.success(current)
    }

    override suspend fun updateProfileFields(fields: Map<String, Any>): Result<Unit> {
        val cur = profileFlow.value ?: return Result.success(Unit)
        profileFlow.value = cur.copy(
            displayName = (fields["displayName"] as? String) ?: cur.displayName,
            academicLevel = (fields["academicLevel"] as? String) ?: cur.academicLevel,
            sessionMode = (fields["sessionMode"] as? String) ?: cur.sessionMode,
            themeAccent = (fields["themeAccent"] as? String) ?: cur.themeAccent,
            petName = (fields["petName"] as? String) ?: cur.petName,
            petLevel = (fields["petLevel"] as? Number)?.toLong() ?: cur.petLevel,
            petXp = (fields["petXp"] as? Number)?.toLong() ?: cur.petXp,
            streakDays = (fields["streakDays"] as? Number)?.toLong() ?: cur.streakDays,
            studyCoins = (fields["studyCoins"] as? Number)?.toLong() ?: cur.studyCoins,
            ticketsRemaining = (fields["ticketsRemaining"] as? Number)?.toLong() ?: cur.ticketsRemaining,
            lastTicketDate = (fields["lastTicketDate"] as? String) ?: cur.lastTicketDate,
            hasLifetimePass = (fields["hasLifetimePass"] as? Boolean) ?: cur.hasLifetimePass,
            hasMonthlyAgentSub = (fields["hasMonthlyAgentSub"] as? Boolean) ?: cur.hasMonthlyAgentSub,
            hasProtonExtra25 = (fields["hasProtonExtra25"] as? Boolean) ?: cur.hasProtonExtra25,
            schoolName = (fields["schoolName"] as? String) ?: cur.schoolName,
            schoolNews = (fields["schoolNews"] as? String) ?: cur.schoolNews,
            studyGroupCode = (fields["studyGroupCode"] as? String) ?: cur.studyGroupCode,
            ageYears = (fields["ageYears"] as? Number)?.toLong() ?: cur.ageYears,
            degreeOrMaster = (fields["degreeOrMaster"] as? String) ?: cur.degreeOrMaster,
            petSpecies = (fields["petSpecies"] as? String) ?: cur.petSpecies,
            petAccessory = (fields["petAccessory"] as? String) ?: cur.petAccessory,
            petColorAccent = (fields["petColorAccent"] as? String) ?: cur.petColorAccent,
            weatherSeasonOverride = (fields["weatherSeasonOverride"] as? String) ?: cur.weatherSeasonOverride,
            unlockedBadges = (fields["unlockedBadges"] as? List<*>)
                ?.filterIsInstance<String>()
                ?: cur.unlockedBadges
        )
        return Result.success(Unit)
    }

    override suspend fun consumeAiTicket(profile: StudentProfile): Result<Long> {
        if (profile.hasUnlimitedAi) return Result.success(profile.ticketsRemaining)
        if (profile.ticketsRemaining <= 0L) {
            return Result.failure(IllegalStateException("Sin tickets de IA disponibles hoy."))
        }
        val next = (profile.ticketsRemaining - 1L).coerceAtLeast(0L)
        updateProfileFields(mapOf("ticketsRemaining" to next))
        return Result.success(next)
    }

    override suspend fun awardStudyProgress(
        currentProfile: StudentProfile,
        xpEarned: Long,
        coinsEarned: Long,
        bonusTickets: Long
    ): Result<StudentProfile> {
        var newXp = currentProfile.petXp + xpEarned
        var newLevel = currentProfile.petLevel
        while (newXp >= newLevel * 100L && newLevel < 100L) {
            newXp -= newLevel * 100L
            newLevel += 1L
        }
        val newCoins = currentProfile.studyCoins + coinsEarned
        val newTickets = (currentProfile.ticketsRemaining + bonusTickets).coerceAtMost(999L)
        val updated = currentProfile.copy(
            petXp = newXp,
            petLevel = newLevel,
            studyCoins = newCoins,
            ticketsRemaining = newTickets
        )
        profileFlow.value = updated
        return Result.success(updated)
    }

    override fun observeSubjects(userId: String): Flow<List<SubjectRecord>> = subjectsFlow

    override suspend fun upsertSubject(
        name: String,
        grade: Double,
        targetHoursPerWeek: Long,
        existingId: String?
    ): Result<String> {
        val cleanName = name.trim().take(60)
        val clamped = grade.coerceIn(0.0, 10.0)
        val priority = SubjectRecord.computePriority(clamped)
        val id = existingId ?: "sub_${UUID.randomUUID()}"
        val record = SubjectRecord(
            id = id,
            userId = "visitante",
            name = cleanName,
            grade = clamped,
            priority = priority,
            targetHoursPerWeek = targetHoursPerWeek.coerceIn(1L, 40L)
        )
        val current = subjectsFlow.value.toMutableList()
        val idx = current.indexOfFirst { it.id == id }
        if (idx >= 0) current[idx] = record else current.add(record)
        subjectsFlow.value = current.sortedBy { it.grade }
        return Result.success(id)
    }

    override suspend fun deleteSubject(subjectId: String): Result<Unit> {
        subjectsFlow.value = subjectsFlow.value.filterNot { it.id == subjectId }
        return Result.success(Unit)
    }

    override fun observeSchoolTeachers(userId: String): Flow<List<SchoolTeacher>> = teachersFlow

    override suspend fun upsertSchoolTeacher(
        name: String,
        subject: String,
        focusNote: String,
        howToHelp: String,
        existingId: String?
    ): Result<String> {
        val id = existingId ?: "teach_${UUID.randomUUID()}"
        val teacher = SchoolTeacher(
            id = id,
            userId = "visitante",
            name = name.trim().take(80).ifBlank { "Profesor" },
            subject = subject.trim().take(60).ifBlank { "General" },
            focusNote = focusNote.trim().take(400),
            howToHelp = howToHelp.trim().take(600)
        )
        val list = teachersFlow.value.toMutableList()
        val idx = list.indexOfFirst { it.id == id }
        if (idx >= 0) list[idx] = teacher else list.add(teacher)
        teachersFlow.value = list.sortedBy { it.subject }
        return Result.success(id)
    }

    override suspend fun deleteSchoolTeacher(teacherId: String): Result<Unit> {
        teachersFlow.value = teachersFlow.value.filterNot { it.id == teacherId }
        return Result.success(Unit)
    }

    override fun observeStudyPlans(userId: String): Flow<List<StudyPlan>> = plansFlow

    override suspend fun createStudyPlan(
        title: String,
        academicLevel: String,
        focusSubjects: List<String>,
        summary: String,
        modules: List<StudyModule>,
        dailyMinutes: Long,
        sourceAuthor: String
    ): Result<String> {
        val id = "plan_${UUID.randomUUID()}"
        val plan = StudyPlan(
            id = id,
            userId = "visitante",
            title = title.trim().take(120),
            academicLevel = academicLevel,
            focusSubjects = focusSubjects,
            summary = summary.trim().take(1500),
            modules = modules.take(25),
            dailyMinutes = dailyMinutes.coerceIn(10L, 600L),
            progressPercent = 0L,
            sourceAuthor = sourceAuthor
        )
        plansFlow.value = listOf(plan) + plansFlow.value
        return Result.success(id)
    }

    override suspend fun toggleStudyPlanModule(plan: StudyPlan, moduleIndex: Int): Result<Unit> {
        plansFlow.value = plansFlow.value.map { existing ->
            if (existing.id != plan.id) existing
            else {
                val updatedMods = existing.modules.mapIndexed { idx, mod ->
                    if (idx == moduleIndex) mod.copy(completed = !mod.completed) else mod
                }
                val done = updatedMods.count { it.completed }
                val progress = if (updatedMods.isEmpty()) 0L else (done * 100L) / updatedMods.size
                existing.copy(modules = updatedMods, progressPercent = progress)
            }
        }
        return Result.success(Unit)
    }

    override suspend fun deleteStudyPlan(planId: String): Result<Unit> {
        plansFlow.value = plansFlow.value.filterNot { it.id == planId }
        return Result.success(Unit)
    }

    override fun observeSmartTasks(userId: String): Flow<List<SmartTask>> = tasksFlow

    override suspend fun createSmartTask(
        title: String,
        subject: String,
        description: String,
        procedureSteps: List<String>,
        xpReward: Long,
        dueDateLabel: String
    ): Result<String> {
        val id = "task_${UUID.randomUUID()}"
        val task = SmartTask(
            id = id,
            userId = "visitante",
            title = title.trim().take(120),
            subject = subject.trim().take(60).ifBlank { "General" },
            description = description.trim().take(2500),
            procedureSteps = procedureSteps.take(20),
            completed = false,
            xpReward = xpReward.coerceIn(0L, 500L),
            dueDateLabel = dueDateLabel
        )
        tasksFlow.value = listOf(task) + tasksFlow.value
        return Result.success(id)
    }

    override suspend fun toggleSmartTask(task: SmartTask): Result<Boolean> {
        var newCompleted = false
        tasksFlow.value = tasksFlow.value.map { existing ->
            if (existing.id == task.id) {
                newCompleted = !existing.completed
                existing.copy(completed = newCompleted)
            } else existing
        }
        return Result.success(newCompleted)
    }

    override suspend fun deleteSmartTask(taskId: String): Result<Unit> {
        tasksFlow.value = tasksFlow.value.filterNot { it.id == taskId }
        return Result.success(Unit)
    }

    override fun observeExamResults(userId: String): Flow<List<ExamResult>> = examResultsFlow

    override suspend fun saveExamResult(
        subject: String,
        title: String,
        score: Long,
        totalQuestions: Long,
        correctAnswers: Long,
        difficulty: String,
        feedbackSummary: String
    ): Result<String> {
        val id = "exam_${UUID.randomUUID()}"
        val res = ExamResult(
            id = id,
            userId = "visitante",
            subject = subject,
            title = title,
            score = score,
            totalQuestions = totalQuestions,
            correctAnswers = correctAnswers,
            difficulty = difficulty,
            feedbackSummary = feedbackSummary
        )
        examResultsFlow.value = listOf(res) + examResultsFlow.value
        return Result.success(id)
    }

    override fun observeFinanceEntries(userId: String): Flow<List<FinanceEntry>> = financeEntriesFlow

    override suspend fun addFinanceEntry(
        title: String,
        amount: Double,
        entryType: String,
        category: String,
        note: String
    ): Result<String> {
        val id = "fin_${UUID.randomUUID()}"
        val entry = FinanceEntry(
            id = id,
            userId = "visitante",
            title = title.trim().take(100).ifBlank { "Movimiento" },
            amount = amount.coerceIn(0.0, 100_000_000.0),
            entryType = if (entryType in listOf("ingreso", "gasto", "bien")) entryType else "gasto",
            category = category.trim().take(50).ifBlank { "General" },
            note = note.trim().take(300)
        )
        financeEntriesFlow.value = listOf(entry) + financeEntriesFlow.value
        return Result.success(id)
    }

    override suspend fun deleteFinanceEntry(entryId: String): Result<Unit> {
        financeEntriesFlow.value = financeEntriesFlow.value.filterNot { it.id == entryId }
        return Result.success(Unit)
    }

    override fun observeWorkshopItems(): Flow<List<WorkshopItem>> = workshopFlow

    override suspend fun publishWorkshopItem(
        authorName: String,
        title: String,
        category: String,
        subject: String,
        academicLevel: String,
        description: String,
        steps: List<String>
    ): Result<String> {
        val id = "ws_${UUID.randomUUID()}"
        val item = WorkshopItem(
            id = id,
            authorId = "visitante",
            authorName = authorName.ifBlank { "Visitante" },
            title = title,
            category = category,
            subject = subject,
            academicLevel = academicLevel,
            description = description,
            steps = steps
        )
        workshopFlow.value = listOf(item) + workshopFlow.value
        return Result.success(id)
    }

    override suspend fun downloadWorkshopItemToUser(item: WorkshopItem): Result<String> {
        workshopFlow.value = workshopFlow.value.map { existing ->
            if (existing.id == item.id) existing.copy(downloadsCount = existing.downloadsCount + 1L) else existing
        }
        return if (item.category == "plan") {
            val modules = item.steps.mapIndexed { idx, step ->
                StudyModule(
                    title = "Módulo ${idx + 1}: ${step.take(50)}",
                    subject = item.subject,
                    durationMinutes = 40L,
                    activities = listOf(step)
                )
            }
            createStudyPlan(
                title = item.title,
                academicLevel = item.academicLevel,
                focusSubjects = listOf(item.subject),
                summary = item.description,
                modules = modules,
                dailyMinutes = 45L,
                sourceAuthor = item.authorName
            )
        } else {
            createSmartTask(
                title = item.title,
                subject = item.subject,
                description = item.description,
                procedureSteps = item.steps.ifEmpty { listOf(item.description) },
                xpReward = 45L,
                dueDateLabel = "Workshop"
            )
        }
    }

    override suspend fun likeWorkshopItem(itemId: String): Result<Unit> {
        workshopFlow.value = workshopFlow.value.map { existing ->
            if (existing.id == itemId) existing.copy(likesCount = existing.likesCount + 1L) else existing
        }
        return Result.success(Unit)
    }

    override fun observeAllUsersAdmin(): Flow<List<StudentProfile>> = MutableStateFlow(emptyList())

    override suspend fun inspectUserBundleAdmin(targetUserId: String): Result<AdminUserInspectBundle> =
        Result.failure(SecurityException("El modo administrador requiere iniciar sesión con Google."))

    override suspend fun adminUpdateUserProfile(targetUserId: String, fields: Map<String, Any>): Result<Unit> =
        Result.failure(SecurityException("El modo administrador requiere iniciar sesión con Google."))

    override suspend fun adminDeleteUserDocument(targetUserId: String): Result<Unit> =
        Result.failure(SecurityException("El modo administrador requiere iniciar sesión con Google."))

    override suspend fun adminDeleteUserSubItem(targetUserId: String, subcollection: String, docId: String): Result<Unit> =
        Result.failure(SecurityException("El modo administrador requiere iniciar sesión con Google."))

    override suspend fun adminDeleteWorkshopItem(itemId: String): Result<Unit> =
        Result.failure(SecurityException("El modo administrador requiere iniciar sesión con Google."))
}
