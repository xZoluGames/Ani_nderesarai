package com.py.ani_nderesarai.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.py.ani_nderesarai.data.model.PaymentIconType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconSelector(
    selectedIcon: PaymentIconType,
    onIconSelected: (PaymentIconType) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = getIconLabel(selectedIcon),
            onValueChange = {},
            readOnly = true,
            label = { Text("Ícono") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            leadingIcon = { Icon(getIcon(selectedIcon), contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            PaymentIconType.values().forEach { iconType ->
                DropdownMenuItem(
                    text = { Text(getIconLabel(iconType)) },
                    onClick = {
                        onIconSelected(iconType)
                        expanded = false
                    },
                    leadingIcon = {
                        Icon(getIcon(iconType), contentDescription = null)
                    }
                )
            }
        }
    }
}

fun getIconLabel(iconType: PaymentIconType): String {
    return when (iconType) {
        PaymentIconType.GENERIC -> "Genérico"
        PaymentIconType.WATER -> "Agua"
        PaymentIconType.ELECTRICITY -> "Electricidad"
        PaymentIconType.GAS -> "Gas"
        PaymentIconType.INTERNET -> "Internet"
        PaymentIconType.PHONE -> "Teléfono"
        PaymentIconType.CREDIT_CARD -> "Tarjeta de Crédito"
        PaymentIconType.BANK -> "Banco"
        PaymentIconType.HOUSE -> "Casa"
        PaymentIconType.CAR -> "Auto"
        PaymentIconType.HEALTH -> "Salud"
        PaymentIconType.EDUCATION -> "Educación"
        PaymentIconType.SHOPPING -> "Compras"
        PaymentIconType.ENTERTAINMENT -> "Entretenimiento"
        PaymentIconType.TRANSPORT -> "Transporte"
        PaymentIconType.SUBSCRIPTION -> "Suscripción"
        PaymentIconType.INSURANCE -> "Seguro"
        PaymentIconType.TAX -> "Impuesto"
    }
}

fun getIcon(iconType: PaymentIconType): androidx.compose.ui.graphics.vector.ImageVector {
    return when (iconType) {
        PaymentIconType.GENERIC -> Icons.Default.Payments
        PaymentIconType.WATER -> Icons.Default.WaterDrop
        PaymentIconType.ELECTRICITY -> Icons.Default.Bolt
        PaymentIconType.GAS -> Icons.Default.LocalFireDepartment
        PaymentIconType.INTERNET -> Icons.Default.Wifi
        PaymentIconType.PHONE -> Icons.Default.Phone
        PaymentIconType.CREDIT_CARD -> Icons.Default.CreditCard
        PaymentIconType.BANK -> Icons.Default.AccountBalance
        PaymentIconType.HOUSE -> Icons.Default.House
        PaymentIconType.CAR -> Icons.Default.DirectionsCar
        PaymentIconType.HEALTH -> Icons.Default.LocalHospital
        PaymentIconType.EDUCATION -> Icons.Default.School
        PaymentIconType.SHOPPING -> Icons.Default.ShoppingCart
        PaymentIconType.ENTERTAINMENT -> Icons.Default.SportsEsports
        PaymentIconType.TRANSPORT -> Icons.Default.DirectionsBus
        PaymentIconType.SUBSCRIPTION -> Icons.Default.Subscriptions
        PaymentIconType.INSURANCE -> Icons.Default.Shield
        PaymentIconType.TAX -> Icons.Default.Receipt
    }
}