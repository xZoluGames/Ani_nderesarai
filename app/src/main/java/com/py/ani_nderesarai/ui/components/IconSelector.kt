package com.py.ani_nderesarai.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.py.ani_nderesarai.data.model.PaymentIconType

@Composable
fun IconSelector(
    selectedIcon: PaymentIconType,
    onIconSelected: (PaymentIconType) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Icono",
            style = MaterialTheme.typography.labelMedium
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(PaymentIconType.values()) { iconType ->
                IconChip(
                    iconType = iconType,
                    isSelected = iconType == selectedIcon,
                    onClick = { onIconSelected(iconType) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconChip(
    iconType: PaymentIconType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        onClick = onClick,
        label = {
            Icon(
                imageVector = getPaymentIcon(iconType),
                contentDescription = getIconName(iconType),
                modifier = Modifier.size(20.dp)
            )
        },
        selected = isSelected
    )
}

fun getPaymentIcon(iconType: PaymentIconType): ImageVector {
    return when (iconType) {
        PaymentIconType.GENERIC -> Icons.Default.Payment
        PaymentIconType.WATER -> Icons.Default.Water
        PaymentIconType.ELECTRICITY -> Icons.Default.ElectricBolt
        PaymentIconType.GAS -> Icons.Default.LocalGasStation
        PaymentIconType.INTERNET -> Icons.Default.Wifi
        PaymentIconType.PHONE -> Icons.Default.Phone
        PaymentIconType.CREDIT_CARD -> Icons.Default.CreditCard
        PaymentIconType.BANK -> Icons.Default.AccountBalance
        PaymentIconType.HOUSE -> Icons.Default.Home
        PaymentIconType.CAR -> Icons.Default.DirectionsCar
        PaymentIconType.HEALTH -> Icons.Default.LocalHospital
        PaymentIconType.EDUCATION -> Icons.Default.School
        PaymentIconType.SHOPPING -> Icons.Default.ShoppingCart
        PaymentIconType.ENTERTAINMENT -> Icons.Default.Movie
    }
}

fun getIconName(iconType: PaymentIconType): String {
    return when (iconType) {
        PaymentIconType.GENERIC -> "General"
        PaymentIconType.WATER -> "Agua"
        PaymentIconType.ELECTRICITY -> "Luz"
        PaymentIconType.GAS -> "Gas"
        PaymentIconType.INTERNET -> "Internet"
        PaymentIconType.PHONE -> "Teléfono"
        PaymentIconType.CREDIT_CARD -> "Tarjeta"
        PaymentIconType.BANK -> "Banco"
        PaymentIconType.HOUSE -> "Casa"
        PaymentIconType.CAR -> "Auto"
        PaymentIconType.HEALTH -> "Salud"
        PaymentIconType.EDUCATION -> "Educación"
        PaymentIconType.SHOPPING -> "Compras"
        PaymentIconType.ENTERTAINMENT -> "Entretenimiento"
    }
}