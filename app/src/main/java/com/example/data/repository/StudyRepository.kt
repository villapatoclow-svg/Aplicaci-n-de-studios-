package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.core.OperationType
import com.example.core.handleFirestoreError
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
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StudyRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) : StudyDataStore {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("El usuario debe iniciar sesión con Google antes de acceder a Firestore.")
    }

    private fun todayString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    // --- Profile ---

    override fun observeProfile(userId: String): Flow<StudentProfile?> = flow {
        val docRef = db.collection("users").document(userId)
        emitAll(
            docRef.snapshots()
                .map { snapshot ->
                    snapshot.toObject(
                        StudentProfile::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val toLog = firestoreError ?: (error as? Exception)
                    if (toLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(toLog, OperationType.GET, docRef.path)
                    }
                    throw error
                }
        )
    }

    override suspend fun ensureProfileInitialized(
        displayName: String
    ): Result<StudentProfile> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val today = todayString()
        return try {
            val existing = docRef.get().await()
            if (existing.exists()) {
                val profile = existing.toObject(
                    StudentProfile::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )!!
                val resetQuota = if (profile.hasProtonExtra25) PROTON_EXTRA_DAILY_TICKETS.toLong() else DAILY_FREE_TICKETS.toLong()
                if (profile.lastTicketDate != today && profile.ticketsRemaining < resetQuota) {
                    docRef.update(
                        mapOf(
                            "ticketsRemaining" to resetQuota,
                            "lastTicketDate" to today,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                    Result.success(profile.copy(ticketsRemaining = resetQuota, lastTicketDate = today))
                } else {
                    Result.success(profile)
                }
            } else {
                val cleanName = displayName.trim().ifBlank { "Estudiante" }.take(60)
                val payload = mapOf(
                    "userId" to uid,
                    "displayName" to cleanName,
                    "academicLevel" to "Secundaria",
                    "sessionMode" to "gamification",
                    "themeAccent" to "indigo",
                    "petName" to "Bubo",
                    "petLevel" to 1L,
                    "petXp" to 20L,
                    "streakDays" to 1L,
                    "studyCoins" to 50L,
                    "ticketsRemaining" to DAILY_FREE_TICKETS.toLong(),
                    "lastTicketDate" to today,
                    "hasLifetimePass" to false,
                    "hasMonthlyAgentSub" to false,
                    "hasProtonExtra25" to false,
                    "schoolName" to "",
                    "schoolNews" to "",
                    "studyGroupCode" to "",
                    "ageYears" to 16L,
                    "degreeOrMaster" to "",
                    "petSpecies" to "buho",
                    "petAccessory" to "ninguno",
                    "petColorAccent" to "indigo",
                    "weatherSeasonOverride" to "auto",
                    "unlockedBadges" to listOf("inicio"),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
                val created = docRef.get().await().toObject(
                    StudentProfile::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ) ?: StudentProfile(
                    id = uid,
                    userId = uid,
                    displayName = cleanName,
                    lastTicketDate = today
                )
                Result.success(created)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    override suspend fun updateProfileFields(fields: Map<String, Any>): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val payload = fields.toMutableMap().apply {
            put("updatedAt", FieldValue.serverTimestamp())
        }
        return try {
            docRef.update(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    override suspend fun consumeAiTicket(profile: StudentProfile): Result<Long> {
        if (profile.hasUnlimitedAi) return Result.success(profile.ticketsRemaining)
        if (profile.ticketsRemaining <= 0L) {
            return Result.failure(IllegalStateException("Sin tickets de IA disponibles hoy."))
        }
        val nextCount = (profile.ticketsRemaining - 1L).coerceAtLeast(0L)
        return updateProfileFields(mapOf("ticketsRemaining" to nextCount)).map { nextCount }
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
        return updateProfileFields(
            mapOf(
                "petXp" to newXp,
                "petLevel" to newLevel,
                "studyCoins" to newCoins,
                "ticketsRemaining" to newTickets
            )
        ).map {
            currentProfile.copy(
                petXp = newXp,
                petLevel = newLevel,
                studyCoins = newCoins,
                ticketsRemaining = newTickets
            )
        }
    }

    // --- Subjects ---

    override fun observeSubjects(userId: String): Flow<List<SubjectRecord>> = flow {
        val colRef = db.collection("users").document(userId).collection("subjects")
        emitAll(
            colRef.snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        SubjectRecord::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedBy { it.grade }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val toLog = firestoreError ?: (error as? Exception)
                    if (toLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(toLog, OperationType.LIST, colRef.path)
                    }
                    throw error
                }
        )
    }

    override suspend fun upsertSubject(
        name: String,
        grade: Double,
        targetHoursPerWeek: Long,
        existingId: String?
    ): Result<String> {
        val uid = requireUserId()
        val cleanName = name.trim().take(60)
        val clampedGrade = grade.coerceIn(0.0, 10.0)
        val priority = SubjectRecord.computePriority(clampedGrade)
        val colRef = db.collection("users").document(uid).collection("subjects")
        val docRef = if (!existingId.isNullOrBlank()) colRef.document(existingId) else colRef.document()

        return try {
            if (!existingId.isNullOrBlank()) {
                docRef.update(
                    mapOf(
                        "name" to cleanName,
                        "grade" to clampedGrade,
                        "priority" to priority,
                        "targetHoursPerWeek" to targetHoursPerWeek.coerceIn(1L, 40L),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.set(
                    mapOf(
                        "userId" to uid,
                        "name" to cleanName,
                        "grade" to clampedGrade,
                        "priority" to priority,
                        "targetHoursPerWeek" to targetHoursPerWeek.coerceIn(1L, 40L),
                        "completedMinutes" to 0L,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    override suspend fun deleteSubject(subjectId: String): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("subjects").document(subjectId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    // --- School Teachers ---

    override fun observeSchoolTeachers(userId: String): Flow<List<SchoolTeacher>> = flow {
        val colRef = db.collection("users").document(userId).collection("school_teachers")
        emitAll(
            colRef.snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        SchoolTeacher::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedBy { it.subject }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val toLog = firestoreError ?: (error as? Exception)
                    if (toLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(toLog, OperationType.LIST, colRef.path)
                    }
                    throw error
                }
        )
    }

    override suspend fun upsertSchoolTeacher(
        name: String,
        subject: String,
        focusNote: String,
        howToHelp: String,
        existingId: String?
    ): Result<String> {
        val uid = requireUserId()
        val cleanName = name.trim().take(80).ifBlank { "Profesor" }
        val cleanSubject = subject.trim().take(60).ifBlank { "General" }
        val cleanFocus = focusNote.trim().take(400)
        val cleanHelp = howToHelp.trim().take(600).ifBlank { "Participar activamente y preparar dudas de clase." }
        val colRef = db.collection("users").document(uid).collection("school_teachers")
        val docRef = if (!existingId.isNullOrBlank()) colRef.document(existingId) else colRef.document()

        return try {
            if (!existingId.isNullOrBlank()) {
                docRef.update(
                    mapOf(
                        "name" to cleanName,
                        "subject" to cleanSubject,
                        "focusNote" to cleanFocus,
                        "howToHelp" to cleanHelp,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.set(
                    mapOf(
                        "userId" to uid,
                        "name" to cleanName,
                        "subject" to cleanSubject,
                        "focusNote" to cleanFocus,
                        "howToHelp" to cleanHelp,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    override suspend fun deleteSchoolTeacher(teacherId: String): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("school_teachers").document(teacherId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    // --- Study Plans ---

    override fun observeStudyPlans(userId: String): Flow<List<StudyPlan>> = flow {
        val colRef = db.collection("users").document(userId).collection("study_plans")
        emitAll(
            colRef.snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        StudyPlan::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val toLog = firestoreError ?: (error as? Exception)
                    if (toLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(toLog, OperationType.LIST, colRef.path)
                    }
                    throw error
                }
        )
    }

    override suspend fun createStudyPlan(
        title: String,
        academicLevel: String,
        focusSubjects: List<String>,
        summary: String,
        modules: List<StudyModule>,
        dailyMinutes: Long,
        sourceAuthor: String
    ): Result<String> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("study_plans").document()
        val moduleMaps = modules.take(25).map { mod ->
            mapOf(
                "title" to mod.title.take(100),
                "subject" to mod.subject.take(60),
                "durationMinutes" to mod.durationMinutes,
                "completed" to mod.completed,
                "activities" to mod.activities.take(10)
            )
        }
        val payload = mapOf(
            "userId" to uid,
            "title" to title.trim().take(120),
            "academicLevel" to academicLevel.take(40),
            "focusSubjects" to focusSubjects.take(15),
            "summary" to summary.trim().take(1500),
            "modules" to moduleMaps,
            "dailyMinutes" to dailyMinutes.coerceIn(10L, 600L),
            "progressPercent" to 0L,
            "sourceAuthor" to sourceAuthor.take(60),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    override suspend fun toggleStudyPlanModule(plan: StudyPlan, moduleIndex: Int): Result<Unit> {
        val uid = requireUserId()
        require(plan.id.isNotBlank()) { "StudyPlan ID requerido." }
        if (moduleIndex !in plan.modules.indices) return Result.success(Unit)

        val updatedModules = plan.modules.mapIndexed { idx, mod ->
            if (idx == moduleIndex) mod.copy(completed = !mod.completed) else mod
        }
        val completedCount = updatedModules.count { it.completed }
        val progress = if (updatedModules.isEmpty()) 0L else ((completedCount * 100L) / updatedModules.size)

        val moduleMaps = updatedModules.map { mod ->
            mapOf(
                "title" to mod.title,
                "subject" to mod.subject,
                "durationMinutes" to mod.durationMinutes,
                "completed" to mod.completed,
                "activities" to mod.activities
            )
        }
        val docRef = db.collection("users").document(uid).collection("study_plans").document(plan.id)
        return try {
            docRef.update(
                mapOf(
                    "modules" to moduleMaps,
                    "progressPercent" to progress,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    override suspend fun deleteStudyPlan(planId: String): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("study_plans").document(planId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    // --- Smart Tasks ---

    override fun observeSmartTasks(userId: String): Flow<List<SmartTask>> = flow {
        val colRef = db.collection("users").document(userId).collection("smart_tasks")
        emitAll(
            colRef.snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        SmartTask::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedWith(compareBy<SmartTask> { it.completed }.thenByDescending { it.createdAt })
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val toLog = firestoreError ?: (error as? Exception)
                    if (toLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(toLog, OperationType.LIST, colRef.path)
                    }
                    throw error
                }
        )
    }

    override suspend fun createSmartTask(
        title: String,
        subject: String,
        description: String,
        procedureSteps: List<String>,
        xpReward: Long,
        dueDateLabel: String
    ): Result<String> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("smart_tasks").document()
        val payload = mapOf(
            "userId" to uid,
            "title" to title.trim().take(120),
            "subject" to subject.trim().take(60).ifBlank { "General" },
            "description" to description.trim().take(2500).ifBlank { title.trim() },
            "procedureSteps" to procedureSteps.take(20),
            "completed" to false,
            "xpReward" to xpReward.coerceIn(0L, 500L),
            "dueDateLabel" to dueDateLabel.take(40),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    override suspend fun toggleSmartTask(task: SmartTask): Result<Boolean> {
        val uid = requireUserId()
        require(task.id.isNotBlank()) { "Task ID requerido." }
        val docRef = db.collection("users").document(uid).collection("smart_tasks").document(task.id)
        val newCompleted = !task.completed
        return try {
            docRef.update(
                mapOf(
                    "completed" to newCompleted,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(newCompleted)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    override suspend fun deleteSmartTask(taskId: String): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("smart_tasks").document(taskId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    // --- Exam Results ---

    override fun observeExamResults(userId: String): Flow<List<ExamResult>> = flow {
        val colRef = db.collection("users").document(userId).collection("exam_results")
        emitAll(
            colRef.snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        ExamResult::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val toLog = firestoreError ?: (error as? Exception)
                    if (toLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(toLog, OperationType.LIST, colRef.path)
                    }
                    throw error
                }
        )
    }

    override suspend fun saveExamResult(
        subject: String,
        title: String,
        score: Long,
        totalQuestions: Long,
        correctAnswers: Long,
        difficulty: String,
        feedbackSummary: String
    ): Result<String> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("exam_results").document()
        val validDifficulty = if (difficulty in listOf("basico", "intermedio", "avanzado")) difficulty else "intermedio"
        val payload = mapOf(
            "userId" to uid,
            "subject" to subject.trim().take(60),
            "title" to title.trim().take(120),
            "score" to score.coerceIn(0L, 100L),
            "totalQuestions" to totalQuestions.coerceIn(1L, 50L),
            "correctAnswers" to correctAnswers.coerceIn(0L, totalQuestions),
            "difficulty" to validDifficulty,
            "feedbackSummary" to feedbackSummary.trim().take(1500),
            "createdAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    // --- Finance Entries (Plan Alto 18+) ---

    override fun observeFinanceEntries(userId: String): Flow<List<FinanceEntry>> = flow {
        val colRef = db.collection("users").document(userId).collection("finance_entries")
        emitAll(
            colRef.snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        FinanceEntry::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val toLog = firestoreError ?: (error as? Exception)
                    if (toLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(toLog, OperationType.LIST, colRef.path)
                    }
                    throw error
                }
        )
    }

    override suspend fun addFinanceEntry(
        title: String,
        amount: Double,
        entryType: String,
        category: String,
        note: String
    ): Result<String> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("finance_entries").document()
        val validType = if (entryType in listOf("ingreso", "gasto", "bien")) entryType else "gasto"
        val payload = mapOf(
            "userId" to uid,
            "title" to title.trim().take(100).ifBlank { "Movimiento" },
            "amount" to amount.coerceIn(0.0, 100_000_000.0),
            "entryType" to validType,
            "category" to category.trim().take(50).ifBlank { "General" },
            "note" to note.trim().take(300),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    override suspend fun deleteFinanceEntry(entryId: String): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("finance_entries").document(entryId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    // --- Community Workshop ---

    override fun observeWorkshopItems(): Flow<List<WorkshopItem>> = flow {
        val query = db.collection("workshop_items").whereEqualTo("visibility", "public")
        emitAll(
            query.snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        WorkshopItem::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.downloadsCount }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val toLog = firestoreError ?: (error as? Exception)
                    if (toLog != null && auth.currentUser != null) {
                        handleFirestoreError(toLog, OperationType.LIST, "workshop_items")
                    }
                    throw error
                }
        )
    }

    override suspend fun publishWorkshopItem(
        authorName: String,
        title: String,
        category: String,
        subject: String,
        academicLevel: String,
        description: String,
        steps: List<String>
    ): Result<String> {
        val uid = requireUserId()
        val docRef = db.collection("workshop_items").document()
        val validCat = if (category in listOf("plan", "actividad", "examen", "guia")) category else "plan"
        val payload = mapOf(
            "authorId" to uid,
            "authorName" to authorName.trim().take(60).ifBlank { "Estudiante" },
            "title" to title.trim().take(120),
            "category" to validCat,
            "subject" to subject.trim().take(60),
            "academicLevel" to academicLevel.trim().take(40),
            "description" to description.trim().take(1500),
            "steps" to steps.take(20),
            "visibility" to "public",
            "downloadsCount" to 0L,
            "likesCount" to 0L,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    override suspend fun downloadWorkshopItemToUser(item: WorkshopItem): Result<String> {
        requireUserId()
        require(item.id.isNotBlank()) { "Workshop item ID requerido." }
        val itemRef = db.collection("workshop_items").document(item.id)
        return try {
            itemRef.update(
                mapOf(
                    "downloadsCount" to FieldValue.increment(1L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            if (item.category == "plan") {
                val modules = item.steps.mapIndexed { idx, step ->
                    StudyModule(
                        title = "Módulo ${idx + 1}: ${step.take(50)}",
                        subject = item.subject,
                        durationMinutes = 40L,
                        completed = false,
                        activities = listOf(step)
                    )
                }.ifEmpty {
                    listOf(
                        StudyModule(
                            title = item.title,
                            subject = item.subject,
                            durationMinutes = 45L,
                            activities = listOf(item.description)
                        )
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
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, itemRef.path)
            Result.failure(e)
        }
    }

    override suspend fun likeWorkshopItem(itemId: String): Result<Unit> {
        requireUserId()
        val itemRef = db.collection("workshop_items").document(itemId)
        return try {
            itemRef.update(
                mapOf(
                    "likesCount" to FieldValue.increment(1L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, itemRef.path)
            Result.failure(e)
        }
    }

    // --- Admin Exclusivo (villapatoclow@gmail.com) ---

    private fun isAdminSession(): Boolean {
        val user = auth.currentUser ?: return false
        val email = user.email ?: user.providerData.firstOrNull { !it.email.isNullOrBlank() }?.email
        return email.equals(ADMIN_EMAIL, ignoreCase = true)
    }

    override fun observeAllUsersAdmin(): Flow<List<StudentProfile>> = flow {
        if (!isAdminSession()) {
            emit(emptyList())
            return@flow
        }
        val colRef = db.collection("users")
        emitAll(
            colRef.snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        StudentProfile::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.petLevel }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val toLog = firestoreError ?: (error as? Exception)
                    if (toLog != null) {
                        handleFirestoreError(toLog, OperationType.LIST, colRef.path)
                    }
                    throw error
                }
        )
    }

    override suspend fun inspectUserBundleAdmin(targetUserId: String): Result<AdminUserInspectBundle> {
        if (!isAdminSession()) {
            return Result.failure(SecurityException("Acceso exclusivo para administrador autorizado."))
        }
        return try {
            val userDoc = db.collection("users").document(targetUserId).get().await()
            val prof = userDoc.toObject(
                StudentProfile::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            ) ?: throw IllegalStateException("Usuario no encontrado.")

            val tasks = db.collection("users").document(targetUserId).collection("smart_tasks").get().await()
                .toObjects(SmartTask::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            val plans = db.collection("users").document(targetUserId).collection("study_plans").get().await()
                .toObjects(StudyPlan::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            val subjects = db.collection("users").document(targetUserId).collection("subjects").get().await()
                .toObjects(SubjectRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            val finances = db.collection("users").document(targetUserId).collection("finance_entries").get().await()
                .toObjects(FinanceEntry::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)

            Result.success(AdminUserInspectBundle(prof, tasks, plans, subjects, finances))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, "users/$targetUserId")
            Result.failure(e)
        }
    }

    override suspend fun adminUpdateUserProfile(targetUserId: String, fields: Map<String, Any>): Result<Unit> {
        if (!isAdminSession()) {
            return Result.failure(SecurityException("Acceso exclusivo para administrador autorizado."))
        }
        val docRef = db.collection("users").document(targetUserId)
        val payload = fields.toMutableMap().apply { put("updatedAt", FieldValue.serverTimestamp()) }
        return try {
            docRef.update(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    override suspend fun adminDeleteUserDocument(targetUserId: String): Result<Unit> {
        if (!isAdminSession()) {
            return Result.failure(SecurityException("Acceso exclusivo para administrador autorizado."))
        }
        val docRef = db.collection("users").document(targetUserId)
        return try {
            val subCollections = listOf("subjects", "school_teachers", "study_plans", "smart_tasks", "exam_results", "finance_entries")
            for (sub in subCollections) {
                val snap = docRef.collection(sub).get().await()
                for (doc in snap.documents) {
                    doc.reference.delete().await()
                }
            }
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    override suspend fun adminDeleteUserSubItem(
        targetUserId: String,
        subcollection: String,
        docId: String
    ): Result<Unit> {
        if (!isAdminSession()) {
            return Result.failure(SecurityException("Acceso exclusivo para administrador autorizado."))
        }
        val docRef = db.collection("users").document(targetUserId).collection(subcollection).document(docId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    override suspend fun adminDeleteWorkshopItem(itemId: String): Result<Unit> {
        if (!isAdminSession()) {
            return Result.failure(SecurityException("Acceso exclusivo para administrador autorizado."))
        }
        val docRef = db.collection("workshop_items").document(itemId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    companion object {
        const val DAILY_FREE_TICKETS = 15
        const val PROTON_EXTRA_DAILY_TICKETS = 40
        const val ADMIN_EMAIL = "villapatoclow@gmail.com"
    }
}
