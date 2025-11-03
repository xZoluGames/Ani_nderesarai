package com.py.ani_nderesarai.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

// Extensión para íconos personalizados
object CustomIcons {
    // Como WhatsApp no está en los íconos por defecto, usamos Message como alternativa
    val WhatsApp: ImageVector = Icons.Default.Message

    // Otros íconos personalizados que podrías necesitar
    val Money: ImageVector = Icons.Default.AttachMoney
    val Bot: ImageVector = Icons.Default.SmartToy
    val Repeat: ImageVector = Icons.Default.Repeat
}

// Para usar en los componentes:
// import com.py.ani_nderesarai.ui.components.CustomIcons
// Icon(CustomIcons.WhatsApp, ...)