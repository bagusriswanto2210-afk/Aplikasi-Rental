package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Light Nature Theme Palette (Tema Alam: Pinus, Lumut, Kayu & Api Unggun)
val PineDark = Color(0xFF0F2A1D)
val ForestGreenPrimary = Color(0xFF1B4332)
val ForestGreenOnPrimary = Color(0xFFFFFFFF)
val ForestGreenContainer = Color(0xFFD8F3DC)
val ForestGreenOnContainer = Color(0xFF081C15)

val MossGreen = Color(0xFF2D5A27)
val FernGreen = Color(0xFF40916C)
val SageLight = Color(0xFFE8F5E9)

val CampfireOrange = Color(0xFFE76F51)
val WarmAmber = Color(0xFFF4A261)
val TerraSecondary = Color(0xFFC85A17)
val TerraOnSecondary = Color(0xFFFFFFFF)
val TerraSecondaryContainer = Color(0xFFFFE0D3)
val TerraOnSecondaryContainer = Color(0xFF380D00)

val WoodBarkBrown = Color(0xFF6B4226)
val WoodBarkLight = Color(0xFFDDA15E)
val AmberTertiary = Color(0xFFBC6C25)
val AmberOnTertiary = Color(0xFFFFFFFF)
val AmberTertiaryContainer = Color(0xFFFAEDCD)
val AmberOnTertiaryContainer = Color(0xFF2E1500)

val MountainSlate = Color(0xFF354F52)
val NatureCardBorder = Color(0xFFCCE3DE)

val AppBackground = Color(0xFFF3F6F2) // Soft earthy forest mist tint
val AppSurface = Color(0xFFFFFFFF)
val AppSurfaceVariant = Color(0xFFE4EDE4)
val AppOutline = Color(0xFF6B7B6E)

// Dark Palette (Malam di Gunung / Night Camp)
val ForestGreenPrimaryDark = Color(0xFF74C69D)
val ForestGreenOnPrimaryDark = Color(0xFF003920)
val ForestGreenContainerDark = Color(0xFF1B4332)
val ForestGreenOnContainerDark = Color(0xFFD8F3DC)

val TerraSecondaryDark = Color(0xFFFFB59D)
val TerraOnSecondaryDark = Color(0xFF5C1900)
val TerraSecondaryContainerDark = Color(0xFF832700)
val TerraOnSecondaryContainerDark = Color(0xFFFFDBCF)

val AppBackgroundDark = Color(0xFF0D1611) // Night forest
val AppSurfaceDark = Color(0xFF14211A)
val AppSurfaceVariantDark = Color(0xFF22352B)
val AppOutlineDark = Color(0xFF8A9A8E)

// Status colors
val StatusPaid = Color(0xFF2D6A4F) // Forest Green
val StatusPartial = Color(0xFFD97706) // Campfire Amber
val StatusUnpaid = Color(0xFFB91C1C) // Wild Berry Red
val StatusOverdue = Color(0xFFDC2626) // Alert Red
val StatusBooked = Color(0xFF1E3A8A) // Mountain Lake Blue
val StatusRented = Color(0xFF0F766E) // Deep Teal River
val StatusCompleted = Color(0xFF4D7C0F) // Olive Foliage

// Nature Gradients
val PineForestGradient = Brush.linearGradient(
    listOf(Color(0xFF1B4332), Color(0xFF2D6A4F), Color(0xFF40916C))
)

val CampfireGlowGradient = Brush.linearGradient(
    listOf(Color(0xFFC85A17), Color(0xFFE76F51), Color(0xFFF4A261))
)

val MorningMistGradient = Brush.linearGradient(
    listOf(Color(0xFFD8F3DC), Color(0xFFF3F6F2))
)

val WoodTrailGradient = Brush.linearGradient(
    listOf(Color(0xFF6B4226), Color(0xFF8C5832), Color(0xFFBC6C25))
)
