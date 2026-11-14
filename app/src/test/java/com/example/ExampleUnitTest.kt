package com.example

import com.example.core.SeasonalThemeManager
import com.example.data.model.AiConnectorConfig
import com.example.data.repository.VisitorStudyStore
import com.example.domain.SubjectCatalog
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ExampleUnitTest {

    @Test
    fun visitorStudyStore_supportsProfileTasksTeachersAndUnlimitedAgentSub() = runBlocking {
        val store = VisitorStudyStore()
        val profile = store.ensureProfileInitialized("Visitante").getOrThrow()
        assertEquals("Visitante", profile.displayName)
        assertEquals(15L, profile.ticketsRemaining)
        assertFalse(profile.hasUnlimitedAi)

        // Consuming a ticket reduces count in free mode
        val afterOne = store.consumeAiTicket(profile).getOrThrow()
        assertEquals(14L, afterOne)

        // Activating monthly automated agent unlocks unlimited AI
        store.updateProfileFields(mapOf("hasMonthlyAgentSub" to true)).getOrThrow()
        val updated = store.observeProfile("visitante").first()!!
        assertTrue(updated.hasUnlimitedAi)
        val afterSub = store.consumeAiTicket(updated).getOrThrow()
        assertEquals(14L, afterSub)

        // Add school teacher and verify observation
        val teacherId = store.upsertSchoolTeacher(
            name = "Prof. Navarro",
            subject = "Artes Visuales",
            focusNote = "Mural escolar",
            howToHelp = "Organizar pinceles y boceto inicial."
        ).getOrThrow()
        val teachers = store.observeSchoolTeachers("visitante").first()
        assertTrue(teachers.any { it.id == teacherId && it.name == "Prof. Navarro" })
    }

    @Test
    fun subjectCatalog_coversPrimariaThroughUniversidadAndDuelAgents() {
        val levels = SubjectCatalog.modules.map { it.level }.toSet()
        assertTrue(levels.containsAll(listOf("Primaria", "Secundaria", "Preparatoria", "Universidad")))
        assertTrue(SubjectCatalog.modules.any { it.category == "Artes" })
        assertTrue(SubjectCatalog.duelAgents.size >= 4)
    }

    @Test
    fun visitorStudyStore_supportsPlanAltoFinancesAndPetConfigurator() = runBlocking {
        val store = VisitorStudyStore()
        val initialFinances = store.observeFinanceEntries("visitante").first()
        assertTrue(initialFinances.isNotEmpty())

        val newId = store.addFinanceEntry(
            title = "Curso de Kotlin Avanzado",
            amount = 850.0,
            entryType = "gasto",
            category = "Educación",
            note = "Inversión en habilidades"
        ).getOrThrow()
        val afterAdd = store.observeFinanceEntries("visitante").first()
        assertTrue(afterAdd.any { it.id == newId && it.title == "Curso de Kotlin Avanzado" })

        store.updateProfileFields(
            mapOf(
                "petSpecies" to "autosauro",
                "petAccessory" to "corona",
                "petColorAccent" to "dorado",
                "ageYears" to 21L,
                "degreeOrMaster" to "Maestría en IA",
                "hasProtonExtra25" to true,
                "weatherSeasonOverride" to "navidad"
            )
        ).getOrThrow()
        val updated = store.observeProfile("visitante").first()!!
        assertEquals("autosauro", updated.petSpecies)
        assertEquals("corona", updated.petAccessory)
        assertEquals(21L, updated.ageYears)
        assertTrue(updated.isAdultOrHigherEd)
        assertTrue(updated.hasProtonExtra25)
        assertEquals("navidad", updated.weatherSeasonOverride)
    }

    @Test
    fun seasonalThemeManager_resolvesAutoDatesAndManualOverrides() {
        val dec25 = Calendar.getInstance().apply { set(2026, Calendar.DECEMBER, 25) }
        val nov14 = Calendar.getInstance().apply { set(2026, Calendar.NOVEMBER, 14) }
        val jul10 = Calendar.getInstance().apply { set(2026, Calendar.JULY, 10) }

        assertEquals("navidad", SeasonalThemeManager.detectAutoSeason(dec25))
        assertEquals("otono", SeasonalThemeManager.detectAutoSeason(nov14))
        assertEquals("lluvia", SeasonalThemeManager.detectAutoSeason(jul10))

        val autoDec = SeasonalThemeManager.resolveSeason("auto", dec25)
        assertTrue(autoDec.isChristmasMode)
        assertEquals("navidad", autoDec.seasonId)

        val autoNov = SeasonalThemeManager.resolveSeason("auto", nov14)
        assertFalse(autoNov.isChristmasMode)
        assertEquals("otono", autoNov.seasonId)

        val forcedXmas = SeasonalThemeManager.resolveSeason("navidad", nov14)
        assertTrue(forcedXmas.isChristmasMode)
        assertEquals("Huevecillo Esfera Navideña", forcedXmas.eggName)

        val forcedRain = SeasonalThemeManager.resolveSeason("lluvia", dec25)
        assertFalse(forcedRain.isChristmasMode)
        assertEquals("Lluvia Eléctrica", forcedRain.badgeText)
    }

    @Test
    fun aiConnectorConfig_formatsActiveLabelsForAllProviders() {
        assertEquals("Firebase AI Logic (Protón 2)", AiConnectorConfig().activeLabel())
        assertEquals(
            "Gemini API (gemini-2.5-pro)",
            AiConnectorConfig(activeProvider = "gemini_byok", geminiModel = "gemini-2.5-pro").activeLabel()
        )
        assertEquals(
            "Cloud Code / Claude (claude-sonnet-4-20250514)",
            AiConnectorConfig(activeProvider = "cloud_code").activeLabel()
        )
        assertEquals(
            "Servicio Externo (gpt-4o)",
            AiConnectorConfig(activeProvider = "openai_compatible", customModel = "gpt-4o").activeLabel()
        )
    }
}

