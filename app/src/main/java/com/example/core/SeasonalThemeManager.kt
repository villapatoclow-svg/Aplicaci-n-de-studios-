package com.example.core

import com.example.data.model.SeasonalThemeInfo
import java.util.Calendar

object SeasonalThemeManager {

    val seasonOptions = listOf(
        "auto" to "Automático (Fecha/Clima)",
        "navidad" to "Navidad Festiva",
        "lluvia" to "Clima Lluvioso",
        "soleado" to "Clima Soleado",
        "otono" to "Otoño Dorado"
    )

    fun detectAutoSeason(calendar: Calendar = Calendar.getInstance()): String {
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return when {
            month == Calendar.DECEMBER || (month == Calendar.JANUARY && day <= 7) -> "navidad"
            month in Calendar.JUNE..Calendar.AUGUST -> "lluvia"
            month in Calendar.SEPTEMBER..Calendar.NOVEMBER -> "otono"
            else -> "soleado"
        }
    }

    fun resolveSeason(
        overrideKey: String?,
        calendar: Calendar = Calendar.getInstance()
    ): SeasonalThemeInfo {
        val effectiveId = when (overrideKey) {
            "navidad", "lluvia", "soleado", "otono" -> overrideKey
            else -> detectAutoSeason(calendar)
        }
        return when (effectiveId) {
            "navidad" -> SeasonalThemeInfo(
                seasonId = "navidad",
                title = "Navidad Protón 2",
                badgeText = "Modo Navidad",
                bannerMessage = "¡Feliz Navidad! La app cambia completamente a temática navideña con nieve, gorro festivo para tu mascota y huevecillo especial.",
                eggName = "Huevecillo Esfera Navideña",
                eggRewardNote = "+100 monedas + 2 tickets IA + medalla navideña",
                isChristmasMode = true
            )
            "lluvia" -> SeasonalThemeInfo(
                seasonId = "lluvia",
                title = "Clima de Lluvia",
                badgeText = "Lluvia Eléctrica",
                bannerMessage = "Tarde lluviosa ideal para concentrarte, resolver retos con Autosauro y abrir tu huevecillo eléctrico.",
                eggName = "Huevecillo Nube Rayo",
                eggRewardNote = "+75 monedas + 2 tickets IA",
                isChristmasMode = false
            )
            "soleado" -> SeasonalThemeInfo(
                seasonId = "soleado",
                title = "Clima Soleado",
                badgeText = "Sol Radiante",
                bannerMessage = "Día despejado para avanzar tus planes y ganar energía solar extra con tu mascota.",
                eggName = "Huevecillo Solar Dorado",
                eggRewardNote = "+75 monedas + 50 XP",
                isChristmasMode = false
            )
            else -> SeasonalThemeInfo(
                seasonId = "otono",
                title = "Clima de Otoño",
                badgeText = "Otoño Dorado",
                bannerMessage = "Brisa de otoño y hojas doradas: época perfecta para estudiar con calma y eclosionar el huevecillo ámbar.",
                eggName = "Huevecillo Hoja Ámbar",
                eggRewardNote = "+75 monedas + 50 XP",
                isChristmasMode = false
            )
        }
    }
}
