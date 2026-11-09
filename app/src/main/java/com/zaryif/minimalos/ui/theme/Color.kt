package com.zaryif.minimalos.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

data class MinimalTheme(
    val id: String,
    val name: String,
    val background: Color,
    val card: Color,
    val darkCard: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accent: Color,
    val onAccent: Color = if (accent.red * 0.299f + accent.green * 0.587f + accent.blue * 0.114f > 0.6f) Color(0xFF000000) else Color(0xFFFFFFFF)
)

object MinimalThemeRegistry {
    /** The launcher's own themes. Onboarding shows only these, as its picker is a single fixed row. */
    val ORIGINAL_THEMES = listOf(
        MinimalTheme("AMOLED_RED", "AMOLED Crimson", Color(0xFF000000), Color(0xFF111111), Color(0xFF0A0A0A), Color(0xFF262626), Color(0xFFFFFFFF), Color(0xFF8E8E93), Color(0xFFFF3B30)),
        MinimalTheme("SPACE_GRAY", "Apple Space Gray", Color(0xFF141416), Color(0xFF1C1C1E), Color(0xFF161618), Color(0xFF2C2C2E), Color(0xFFFFFFFF), Color(0xFF98989D), Color(0xFF0A84FF)),
        MinimalTheme("NORDIC_FROST", "Nordic Snow", Color(0xFF000000), Color(0xFF141414), Color(0xFF0D0D0D), Color(0xFF2E2E2E), Color(0xFFFFFFFF), Color(0xFFA0A0A0), Color(0xFFFFFFFF), Color(0xFF000000)),
        MinimalTheme("RABBIT_ORANGE", "Rabbit Amber", Color(0xFF0D0D0F), Color(0xFF18181B), Color(0xFF121214), Color(0xFF2D2D32), Color(0xFFFFFFFF), Color(0xFF9E9EA4), Color(0xFFFF5722)),
        MinimalTheme("EMERALD_MATRIX", "Matrix Forest", Color(0xFF070E0A), Color(0xFF0E1A13), Color(0xFF08120B), Color(0xFF1C3224), Color(0xFFFFFFFF), Color(0xFF7EA88F), Color(0xFF10B981)),
        MinimalTheme("MIDNIGHT_NAVY", "Midnight Obsidian", Color(0xFF060913), Color(0xFF0D1424), Color(0xFF080C17), Color(0xFF1A2640), Color(0xFFFFFFFF), Color(0xFF8B9BB4), Color(0xFF38BDF8), Color(0xFF000000)),
        MinimalTheme("TITANIUM_VIOLET", "Titanium Purple", Color(0xFF0A0714), Color(0xFF151024), Color(0xFF0E0919), Color(0xFF261C3D), Color(0xFFFFFFFF), Color(0xFF9A8CB2), Color(0xFFA855F7))
    )

    /**
     * Dark palettes from Omarchy (https://github.com/omacom/omarchy), MIT,
     * Copyright (c) David Heinemeier Hansson, via Omalauncher's bundled colors.toml.
     * Mapping: background, lighter_background -> card, dark_background -> darkCard,
     * muted -> border, foreground, dark_foreground -> secondary text, accent.
     * Omarchy's light themes are left out: this launcher always uses a dark scheme.
     */
    val OMARCHY_THEMES = listOf(
        MinimalTheme("OMARCHY_CATPPUCCIN", "Catppuccin", Color(0xFF1E1E2E), Color(0xFF313244), Color(0xFF161622), Color(0xFF585B70), Color(0xFFCDD6F4), Color(0xFF6C7086), Color(0xFF89B4FA)),
        MinimalTheme("OMARCHY_ETHEREAL", "Ethereal", Color(0xFF060B1E), Color(0xFF131A3A), Color(0xFF040816), Color(0xFF6D7DB6), Color(0xFFFFCEAD), Color(0xFF6D7DB6), Color(0xFF7D82D9)),
        MinimalTheme("OMARCHY_EVERFOREST", "Everforest", Color(0xFF2D353B), Color(0xFF343F44), Color(0xFF21272C), Color(0xFF475258), Color(0xFFD3C6AA), Color(0xFF4F585E), Color(0xFF7FBBB3)),
        MinimalTheme("OMARCHY_GRUVBOX", "Gruvbox", Color(0xFF282828), Color(0xFF3C3836), Color(0xFF1E1E1E), Color(0xFF665C54), Color(0xFFD4BE98), Color(0xFF7C6F64), Color(0xFF7DAEA3)),
        MinimalTheme("OMARCHY_HACKERMAN", "Hackerman", Color(0xFF0B0C16), Color(0xFF151828), Color(0xFF080910), Color(0xFF2D3450), Color(0xFFDDF7FF), Color(0xFF6A6E95), Color(0xFF82FB9C)),
        MinimalTheme("OMARCHY_JADE_HORSE", "Jade Horse", Color(0xFF142620), Color(0xFF203B31), Color(0xFF0F1E19), Color(0xFF64887B), Color(0xFFDFEDE5), Color(0xFF91AD9F), Color(0xFF8BCBB1)),
        MinimalTheme("OMARCHY_KANAGAWA", "Kanagawa", Color(0xFF1F1F28), Color(0xFF223249), Color(0xFF17171E), Color(0xFF54546D), Color(0xFFDCD7BA), Color(0xFF727169), Color(0xFFDCD7BA)),
        MinimalTheme("OMARCHY_LAST_HORIZON", "Last Horizon", Color(0xFF0C0B0C), Color(0xFF0C0B0C), Color(0xFF090809), Color(0xFF584E51), Color(0xFFFAFCFB), Color(0xFF584E51), Color(0xFFB59790)),
        MinimalTheme("OMARCHY_LUMON", "Lumon", Color(0xFF16242D), Color(0xFF1B2D40), Color(0xFF101B21), Color(0xFF304860), Color(0xFFD6E2EE), Color(0xFF4D86B0), Color(0xFF8BC9EB)),
        MinimalTheme("OMARCHY_MATTE_BLACK", "Matte Black", Color(0xFF121212), Color(0xFF1E1E1E), Color(0xFF0D0D0D), Color(0xFF333333), Color(0xFFBEBEBE), Color(0xFF555555), Color(0xFFE68E0D)),
        MinimalTheme("OMARCHY_MIASMA", "Miasma", Color(0xFF222222), Color(0xFF2C2C2C), Color(0xFF191919), Color(0xFF666666), Color(0xFFC2C2B0), Color(0xFF555555), Color(0xFF78824B)),
        MinimalTheme("OMARCHY_NORD", "Nord", Color(0xFF2E3440), Color(0xFF3B4252), Color(0xFF222730), Color(0xFF4C566A), Color(0xFFD8DEE9), Color(0xFF667080), Color(0xFF81A1C1)),
        MinimalTheme("OMARCHY_RETRO_82", "Retro 82", Color(0xFF05182E), Color(0xFF0A2540), Color(0xFF031222), Color(0xFF2A6B78), Color(0xFFF6DCAC), Color(0xFF3F8F8A), Color(0xFFFAA968)),
        MinimalTheme("OMARCHY_RISTRETTO", "Ristretto", Color(0xFF2C2525), Color(0xFF3D2F2A), Color(0xFF211B1B), Color(0xFF72696A), Color(0xFFE6D9DB), Color(0xFF72696A), Color(0xFFF38D70)),
        MinimalTheme("OMARCHY_SOLITUDE", "Solitude", Color(0xFF101315), Color(0xFF101315), Color(0xFF0C0E10), Color(0xFF4B4E55), Color(0xFFCACCCC), Color(0xFF4B4E55), Color(0xFF798186)),
        MinimalTheme("OMARCHY_STARSHIP", "Starship", Color(0xFF1C1C1E), Color(0xFF2C2C2E), Color(0xFF161618), Color(0xFF8E8E93), Color(0xFFF5F5F7), Color(0xFF98989D), Color(0xFFFF9F0A)),
        MinimalTheme("OMARCHY_TOKYO_NIGHT", "Tokyo Night", Color(0xFF1A1B26), Color(0xFF24283B), Color(0xFF13141C), Color(0xFF414868), Color(0xFFA9B1D6), Color(0xFF565F89), Color(0xFF7AA2F7)),
        MinimalTheme("OMARCHY_VANTABLACK", "Vantablack", Color(0xFF000000), Color(0xFF1A1A1A), Color(0xFF090909), Color(0xFF7A7A7A), Color(0xFFFFFFFF), Color(0xFF505050), Color(0xFF8D8D8D))
    )

    val THEMES = ORIGINAL_THEMES + OMARCHY_THEMES

    fun getTheme(id: String): MinimalTheme {
        return THEMES.find { it.id.equals(id, ignoreCase = true) } ?: THEMES.first()
    }
}

var currentActiveTheme by mutableStateOf(MinimalThemeRegistry.THEMES.first())

val MinimalBlack: Color get() = currentActiveTheme.background
val MinimalWhite: Color get() = currentActiveTheme.textPrimary
val MinimalRed: Color get() = currentActiveTheme.accent
val MinimalOnAccent: Color get() = currentActiveTheme.onAccent
val MinimalGray: Color get() = currentActiveTheme.textSecondary
val MinimalDarkGray: Color get() = currentActiveTheme.border
val MinimalCardBg: Color get() = currentActiveTheme.card
val MinimalDarkCardBg: Color get() = currentActiveTheme.darkCard
