package com.py.ani_nderesarai.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Utilidades para diseño responsive
 * Permite adaptar la UI según el tamaño de pantalla
 */

// ============================================
// CLASES DE TAMAÑO DE PANTALLA
// ============================================

/**
 * Representa el tamaño de ventana actual
 */
enum class WindowSize {
    /**
     * Pantallas pequeñas (< 600dp)
     * Típicamente teléfonos en portrait
     */
    COMPACT,

    /**
     * Pantallas medianas (600dp - 840dp)
     * Típicamente teléfonos en landscape o tablets pequeñas
     */
    MEDIUM,

    /**
     * Pantallas grandes (> 840dp)
     * Típicamente tablets grandes o escritorio
     */
    EXPANDED
}

/**
 * Clase de tamaño de pantalla con dimensiones específicas
 */
data class WindowSizeClass(
    val widthSizeClass: WindowSize,
    val heightSizeClass: WindowSize,
    val widthDp: Dp,
    val heightDp: Dp
)

// ============================================
// COMPOSABLES
// ============================================

/**
 * Determina el tamaño de ventana actual basado en el ancho de pantalla
 */
@Composable
fun rememberWindowSize(): WindowSize {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp

    return when {
        screenWidthDp < 600.dp -> WindowSize.COMPACT
        screenWidthDp < 840.dp -> WindowSize.MEDIUM
        else -> WindowSize.EXPANDED
    }
}

/**
 * Proporciona información completa sobre el tamaño de ventana
 */
@Composable
fun rememberWindowSizeClass(): WindowSizeClass {
    val configuration = LocalConfiguration.current
    val widthDp = configuration.screenWidthDp.dp
    val heightDp = configuration.screenHeightDp.dp

    val widthClass = when {
        widthDp < 600.dp -> WindowSize.COMPACT
        widthDp < 840.dp -> WindowSize.MEDIUM
        else -> WindowSize.EXPANDED
    }

    val heightClass = when {
        heightDp < 480.dp -> WindowSize.COMPACT
        heightDp < 900.dp -> WindowSize.MEDIUM
        else -> WindowSize.EXPANDED
    }

    return WindowSizeClass(
        widthSizeClass = widthClass,
        heightSizeClass = heightClass,
        widthDp = widthDp,
        heightDp = heightDp
    )
}

// ============================================
// FUNCIONES DE UTILIDAD
// ============================================

/**
 * Obtiene el número de columnas para un grid según el tamaño de pantalla
 */
fun WindowSize.gridColumns(): Int {
    return when (this) {
        WindowSize.COMPACT -> 1
        WindowSize.MEDIUM -> 2
        WindowSize.EXPANDED -> 3
    }
}

/**
 * Obtiene el padding horizontal según el tamaño de pantalla
 */
fun WindowSize.horizontalPadding(): Dp {
    return when (this) {
        WindowSize.COMPACT -> 16.dp
        WindowSize.MEDIUM -> 24.dp
        WindowSize.EXPANDED -> 32.dp
    }
}

/**
 * Obtiene el espaciado vertical según el tamaño de pantalla
 */
fun WindowSize.verticalSpacing(): Dp {
    return when (this) {
        WindowSize.COMPACT -> 12.dp
        WindowSize.MEDIUM -> 16.dp
        WindowSize.EXPANDED -> 20.dp
    }
}

/**
 * Determina si debe usar layout compacto
 */
fun WindowSize.isCompact(): Boolean = this == WindowSize.COMPACT

/**
 * Determina si debe usar layout expandido
 */
fun WindowSize.isExpanded(): Boolean = this == WindowSize.EXPANDED

// ============================================
// EXTENSIONES PARA DP
// ============================================

/**
 * Escala un valor dp según el tamaño de pantalla
 */
fun Dp.responsive(windowSize: WindowSize): Dp {
    val scale = when (windowSize) {
        WindowSize.COMPACT -> 1.0f
        WindowSize.MEDIUM -> 1.2f
        WindowSize.EXPANDED -> 1.4f
    }
    return this * scale
}

/**
 * Obtiene un valor dp diferente según el tamaño de pantalla
 */
fun responsiveDp(
    compact: Dp,
    medium: Dp = compact * 1.2f,
    expanded: Dp = compact * 1.5f,
    windowSize: WindowSize
): Dp {
    return when (windowSize) {
        WindowSize.COMPACT -> compact
        WindowSize.MEDIUM -> medium
        WindowSize.EXPANDED -> expanded
    }
}

// ============================================
// EJEMPLOS DE USO
// ============================================

/**
 * EJEMPLO 1: Grid adaptativo
 *
 * val windowSize = rememberWindowSize()
 *
 * LazyVerticalGrid(
 *     columns = GridCells.Fixed(windowSize.gridColumns())
 * ) {
 *     items(reminders) { reminder ->
 *         ReminderCard(reminder)
 *     }
 * }
 */

/**
 * EJEMPLO 2: Padding responsive
 *
 * val windowSize = rememberWindowSize()
 *
 * Column(
 *     modifier = Modifier.padding(
 *         horizontal = windowSize.horizontalPadding()
 *     )
 * ) {
 *     // Contenido
 * }
 */

/**
 * EJEMPLO 3: Layout condicional
 *
 * val windowSize = rememberWindowSize()
 *
 * if (windowSize.isCompact()) {
 *     // Layout vertical para móviles
 *     Column { ... }
 * } else {
 *     // Layout horizontal para tablets
 *     Row { ... }
 * }
 */

/**
 * EJEMPLO 4: Tamaño responsive
 *
 * val windowSize = rememberWindowSize()
 *
 * Icon(
 *     modifier = Modifier.size(24.dp.responsive(windowSize))
 * )
 */

/**
 * EJEMPLO 5: Valores específicos por pantalla
 *
 * val windowSize = rememberWindowSize()
 *
 * val cardWidth = responsiveDp(
 *     compact = 200.dp,
 *     medium = 300.dp,
 *     expanded = 400.dp,
 *     windowSize = windowSize
 * )
 */

// ============================================
// CONSTANTES PREDEFINIDAS
// ============================================

object ResponsiveDimensions {
    // Padding
    const val PADDING_SMALL_COMPACT = 8
    const val PADDING_SMALL_MEDIUM = 12
    const val PADDING_SMALL_EXPANDED = 16

    const val PADDING_MEDIUM_COMPACT = 16
    const val PADDING_MEDIUM_MEDIUM = 24
    const val PADDING_MEDIUM_EXPANDED = 32

    const val PADDING_LARGE_COMPACT = 24
    const val PADDING_LARGE_MEDIUM = 32
    const val PADDING_LARGE_EXPANDED = 40

    // Tamaños de íconos
    const val ICON_SMALL_COMPACT = 16
    const val ICON_SMALL_MEDIUM = 20
    const val ICON_SMALL_EXPANDED = 24

    const val ICON_MEDIUM_COMPACT = 24
    const val ICON_MEDIUM_MEDIUM = 32
    const val ICON_MEDIUM_EXPANDED = 40

    const val ICON_LARGE_COMPACT = 48
    const val ICON_LARGE_MEDIUM = 64
    const val ICON_LARGE_EXPANDED = 80

    // Espaciado
    const val SPACING_SMALL_COMPACT = 4
    const val SPACING_SMALL_MEDIUM = 6
    const val SPACING_SMALL_EXPANDED = 8

    const val SPACING_MEDIUM_COMPACT = 8
    const val SPACING_MEDIUM_MEDIUM = 12
    const val SPACING_MEDIUM_EXPANDED = 16

    const val SPACING_LARGE_COMPACT = 16
    const val SPACING_LARGE_MEDIUM = 24
    const val SPACING_LARGE_EXPANDED = 32
}

/**
 * Helper para obtener dimensiones responsive
 */
object Responsive {
    @Composable
    fun paddingSmall(): Dp {
        val windowSize = rememberWindowSize()
        return when (windowSize) {
            WindowSize.COMPACT -> ResponsiveDimensions.PADDING_SMALL_COMPACT.dp
            WindowSize.MEDIUM -> ResponsiveDimensions.PADDING_SMALL_MEDIUM.dp
            WindowSize.EXPANDED -> ResponsiveDimensions.PADDING_SMALL_EXPANDED.dp
        }
    }

    @Composable
    fun paddingMedium(): Dp {
        val windowSize = rememberWindowSize()
        return when (windowSize) {
            WindowSize.COMPACT -> ResponsiveDimensions.PADDING_MEDIUM_COMPACT.dp
            WindowSize.MEDIUM -> ResponsiveDimensions.PADDING_MEDIUM_MEDIUM.dp
            WindowSize.EXPANDED -> ResponsiveDimensions.PADDING_MEDIUM_EXPANDED.dp
        }
    }

    @Composable
    fun paddingLarge(): Dp {
        val windowSize = rememberWindowSize()
        return when (windowSize) {
            WindowSize.COMPACT -> ResponsiveDimensions.PADDING_LARGE_COMPACT.dp
            WindowSize.MEDIUM -> ResponsiveDimensions.PADDING_LARGE_MEDIUM.dp
            WindowSize.EXPANDED -> ResponsiveDimensions.PADDING_LARGE_EXPANDED.dp
        }
    }

    @Composable
    fun iconMedium(): Dp {
        val windowSize = rememberWindowSize()
        return when (windowSize) {
            WindowSize.COMPACT -> ResponsiveDimensions.ICON_MEDIUM_COMPACT.dp
            WindowSize.MEDIUM -> ResponsiveDimensions.ICON_MEDIUM_MEDIUM.dp
            WindowSize.EXPANDED -> ResponsiveDimensions.ICON_MEDIUM_EXPANDED.dp
        }
    }

    @Composable
    fun spacingMedium(): Dp {
        val windowSize = rememberWindowSize()
        return when (windowSize) {
            WindowSize.COMPACT -> ResponsiveDimensions.SPACING_MEDIUM_COMPACT.dp
            WindowSize.MEDIUM -> ResponsiveDimensions.SPACING_MEDIUM_MEDIUM.dp
            WindowSize.EXPANDED -> ResponsiveDimensions.SPACING_MEDIUM_EXPANDED.dp
        }
    }
}

/**
 * GUÍA DE IMPLEMENTACIÓN EN TU APP:
 *
 * 1. Copia este archivo a: app/src/main/java/com/py/ani_nderesarai/ui/utils/
 *
 * 2. Usa en tus pantallas:
 *    val windowSize = rememberWindowSize()
 *
 * 3. Aplica en layouts:
 *    - Grids: columns = GridCells.Fixed(windowSize.gridColumns())
 *    - Padding: padding(horizontal = windowSize.horizontalPadding())
 *    - Condicional: if (windowSize.isCompact()) { ... }
 *
 * 4. Para valores específicos:
 *    val padding = Responsive.paddingMedium()
 */