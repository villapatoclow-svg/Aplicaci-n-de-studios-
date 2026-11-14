package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.StudyModule
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class StudyRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun ensureProfileInitialized_createsProfileAndAllowsUpdates() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = StudyRepository(firestore, auth)

        val profileResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.ensureProfileInitialized("Ana")
        }
        assertTrue(profileResult.isSuccess)
        val profile = profileResult.getOrThrow()
        assertEquals(uid, profile.userId)
        assertEquals("Ana", profile.displayName)

        val observed = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeProfile(uid).first { it != null }
        }
        assertNotNull(observed)
        assertEquals("gamification", observed?.sessionMode)
    }

    @Test
    fun smartTask_createObserveAndCrossUserIsolation() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = StudyRepository(firestore, auth)
        val taskId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createSmartTask(
                title = "Resolver integrales",
                subject = "Cálculo",
                description = "5 ejercicios por sustitución",
                procedureSteps = listOf("Elegir u", "Derivar du", "Integrar")
            ).getOrThrow()
        }
        assertTrue(taskId.isNotBlank())

        val tasks = withTimeout(FLOW_TIMEOUT_MS) {
            aliceRepo.observeSmartTasks(aliceUid).first { list -> list.any { it.id == taskId } }
        }
        assertTrue(tasks.any { it.id == taskId })

        // Cross-user read should fail with PERMISSION_DENIED
        signInTestUser(BOB_EMAIL)
        val bobRepo = StudyRepository(firestore, auth)
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                bobRepo.observeSmartTasks(aliceUid).first()
            }
            fail("Expected PERMISSION_DENIED for cross-user smart_tasks read")
        } catch (e: Exception) {
            val fsException = checkNotNull(e.unwrapFirestoreException()) { "Expected FirebaseFirestoreException, got $e" }
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsException.code)
        }
    }

    @Test
    fun studyPlan_andWorkshop_createAndDownloadWorkflow() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = StudyRepository(firestore, auth)

        val planId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createStudyPlan(
                title = "Plan Secundaria Refuerzo",
                academicLevel = "Secundaria",
                focusSubjects = listOf("Matemáticas", "Física"),
                summary = "Enfoque práctico en materias bajas.",
                modules = listOf(
                    StudyModule(
                        title = "Álgebra básica",
                        subject = "Matemáticas",
                        durationMinutes = 30L,
                        activities = listOf("Ejercicios de despeje")
                    )
                ),
                dailyMinutes = 45L
            ).getOrThrow()
        }
        assertTrue(planId.isNotBlank())

        val itemId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.publishWorkshopItem(
                authorName = "Ana",
                title = "Guía Rápida de Cinemática",
                category = "plan",
                subject = "Física",
                academicLevel = "Secundaria",
                description = "Fórmulas y ejercicios de MRU.",
                steps = listOf("Revisar v = d/t", "Resolver 4 problemas")
            ).getOrThrow()
        }

        val bobUid = signInTestUser(BOB_EMAIL)
        val bobRepo = StudyRepository(firestore, auth)
        val items = withTimeout(FLOW_TIMEOUT_MS) {
            bobRepo.observeWorkshopItems().first { list -> list.any { it.id == itemId } }
        }
        val workshopItem = items.first { it.id == itemId }
        val clonedPlanId = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.downloadWorkshopItemToUser(workshopItem).getOrThrow()
        }
        assertTrue(clonedPlanId.isNotBlank())
        assertTrue(bobUid != aliceUid)
    }

    @Test
    fun observeSmartTasks_unauthenticated_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repository = StudyRepository(firestore, auth)
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeSmartTasks("alice_123").first()
            }
            fail("Expected PERMISSION_DENIED for unauthenticated caller")
        } catch (e: Exception) {
            val fsException = checkNotNull(e.unwrapFirestoreException()) { "Expected FirebaseFirestoreException, got $e" }
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsException.code)
        }
    }

    @Test
    fun schoolTeachers_createObserveAndCrossUserIsolation() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = StudyRepository(firestore, auth)
        val teacherId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.upsertSchoolTeacher(
                name = "Mtra. Sofía Herrera",
                subject = "Ciencias",
                focusNote = "Feria de ciencias este viernes",
                howToHelp = "Llevar cartel visual con hipótesis y materiales."
            ).getOrThrow()
        }
        assertTrue(teacherId.isNotBlank())

        val teachers = withTimeout(FLOW_TIMEOUT_MS) {
            aliceRepo.observeSchoolTeachers(aliceUid).first { list -> list.any { it.id == teacherId } }
        }
        assertTrue(teachers.any { it.id == teacherId && it.subject == "Ciencias" })

        signInTestUser(BOB_EMAIL)
        val bobRepo = StudyRepository(firestore, auth)
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                bobRepo.observeSchoolTeachers(aliceUid).first()
            }
            fail("Expected PERMISSION_DENIED for cross-user school_teachers read")
        } catch (e: Exception) {
            val fsException = checkNotNull(e.unwrapFirestoreException()) { "Expected FirebaseFirestoreException, got $e" }
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsException.code)
        }
    }

    @Test
    fun financeEntries_createObserveAndCrossUserIsolation() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = StudyRepository(firestore, auth)
        val entryId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.addFinanceEntry(
                title = "Beca Posgrado",
                amount = 5200.0,
                entryType = "ingreso",
                category = "Beca",
                note = "Apoyo mensual"
            ).getOrThrow()
        }
        assertTrue(entryId.isNotBlank())

        val entries = withTimeout(FLOW_TIMEOUT_MS) {
            aliceRepo.observeFinanceEntries(aliceUid).first { list -> list.any { it.id == entryId } }
        }
        assertTrue(entries.any { it.id == entryId && it.entryType == "ingreso" })

        signInTestUser(BOB_EMAIL)
        val bobRepo = StudyRepository(firestore, auth)
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                bobRepo.observeFinanceEntries(aliceUid).first()
            }
            fail("Expected PERMISSION_DENIED for cross-user finance_entries read")
        } catch (e: Exception) {
            val fsException = checkNotNull(e.unwrapFirestoreException()) { "Expected FirebaseFirestoreException, got $e" }
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsException.code)
        }
    }

    @Test
    fun nonAdmin_cannotExecuteAdminOperations() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = StudyRepository(firestore, auth)
        aliceRepo.ensureProfileInitialized("Ana").getOrThrow()

        signInTestUser(BOB_EMAIL)
        val bobRepo = StudyRepository(firestore, auth)
        val inspectResult = bobRepo.inspectUserBundleAdmin(aliceUid)
        assertTrue(inspectResult.isFailure)
        val deleteResult = bobRepo.adminDeleteUserDocument(aliceUid)
        assertTrue(deleteResult.isFailure)
    }

    private companion object {
        const val ALICE_EMAIL = "alice_mentepro@test.com"
        const val BOB_EMAIL = "bob_mentepro@test.com"
        const val DEFAULT_TIMEOUT_MS = 10_000L
        const val FLOW_TIMEOUT_MS = 10_000L
    }
}
