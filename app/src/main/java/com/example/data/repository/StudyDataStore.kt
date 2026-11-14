package com.example.data.repository

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

interface StudyDataStore {
    fun observeProfile(userId: String): Flow<StudentProfile?>
    suspend fun ensureProfileInitialized(displayName: String): Result<StudentProfile>
    suspend fun updateProfileFields(fields: Map<String, Any>): Result<Unit>
    suspend fun consumeAiTicket(profile: StudentProfile): Result<Long>
    suspend fun awardStudyProgress(
        currentProfile: StudentProfile,
        xpEarned: Long,
        coinsEarned: Long,
        bonusTickets: Long = 0L
    ): Result<StudentProfile>

    fun observeSubjects(userId: String): Flow<List<SubjectRecord>>
    suspend fun upsertSubject(
        name: String,
        grade: Double,
        targetHoursPerWeek: Long = 4L,
        existingId: String? = null
    ): Result<String>
    suspend fun deleteSubject(subjectId: String): Result<Unit>

    fun observeSchoolTeachers(userId: String): Flow<List<SchoolTeacher>>
    suspend fun upsertSchoolTeacher(
        name: String,
        subject: String,
        focusNote: String,
        howToHelp: String,
        existingId: String? = null
    ): Result<String>
    suspend fun deleteSchoolTeacher(teacherId: String): Result<Unit>

    fun observeStudyPlans(userId: String): Flow<List<StudyPlan>>
    suspend fun createStudyPlan(
        title: String,
        academicLevel: String,
        focusSubjects: List<String>,
        summary: String,
        modules: List<StudyModule>,
        dailyMinutes: Long,
        sourceAuthor: String = "IA MentePro"
    ): Result<String>
    suspend fun toggleStudyPlanModule(plan: StudyPlan, moduleIndex: Int): Result<Unit>
    suspend fun deleteStudyPlan(planId: String): Result<Unit>

    fun observeSmartTasks(userId: String): Flow<List<SmartTask>>
    suspend fun createSmartTask(
        title: String,
        subject: String,
        description: String,
        procedureSteps: List<String>,
        xpReward: Long = 40L,
        dueDateLabel: String = "Hoy"
    ): Result<String>
    suspend fun toggleSmartTask(task: SmartTask): Result<Boolean>
    suspend fun deleteSmartTask(taskId: String): Result<Unit>

    fun observeExamResults(userId: String): Flow<List<ExamResult>>
    suspend fun saveExamResult(
        subject: String,
        title: String,
        score: Long,
        totalQuestions: Long,
        correctAnswers: Long,
        difficulty: String,
        feedbackSummary: String
    ): Result<String>

    fun observeFinanceEntries(userId: String): Flow<List<FinanceEntry>>
    suspend fun addFinanceEntry(
        title: String,
        amount: Double,
        entryType: String,
        category: String,
        note: String
    ): Result<String>
    suspend fun deleteFinanceEntry(entryId: String): Result<Unit>

    fun observeWorkshopItems(): Flow<List<WorkshopItem>>
    suspend fun publishWorkshopItem(
        authorName: String,
        title: String,
        category: String,
        subject: String,
        academicLevel: String,
        description: String,
        steps: List<String>
    ): Result<String>
    suspend fun downloadWorkshopItemToUser(item: WorkshopItem): Result<String>
    suspend fun likeWorkshopItem(itemId: String): Result<Unit>

    // --- Admin Exclusivo (villapatoclow@gmail.com) ---
    fun observeAllUsersAdmin(): Flow<List<StudentProfile>>
    suspend fun inspectUserBundleAdmin(targetUserId: String): Result<com.example.data.model.AdminUserInspectBundle>
    suspend fun adminUpdateUserProfile(targetUserId: String, fields: Map<String, Any>): Result<Unit>
    suspend fun adminDeleteUserDocument(targetUserId: String): Result<Unit>
    suspend fun adminDeleteUserSubItem(targetUserId: String, subcollection: String, docId: String): Result<Unit>
    suspend fun adminDeleteWorkshopItem(itemId: String): Result<Unit>
}
