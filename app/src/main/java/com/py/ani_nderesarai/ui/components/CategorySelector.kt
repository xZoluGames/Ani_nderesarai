package com.py.ani_nderesarai.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.py.ani_nderesarai.data.model.PaymentCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelector(
    selectedCategory: PaymentCategory,
    onCategorySelected: (PaymentCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = getCategoryLabel(selectedCategory),
            onValueChange = {},
            readOnly = true,
            label = { Text("Categoría") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            leadingIcon = { Icon(getCategoryIcon(selectedCategory), contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            PaymentCategory.values().forEach { category ->
                DropdownMenuItem(
                    text = { Text(getCategoryLabel(category)) },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    },
                    leadingIcon = {
                        Icon(getCategoryIcon(category), contentDescription = null)
                    }
                )
            }
        }
    }
}

fun getCategoryLabel(category: PaymentCategory): String {
    return when (category) {
        PaymentCategory.UTILITIES -> "Servicios Públicos"
        PaymentCategory.WATER -> "Agua"
        PaymentCategory.ELECTRICITY -> "Electricidad"
        PaymentCategory.GAS -> "Gas"
        PaymentCategory.INTERNET -> "Internet"
        PaymentCategory.PHONE -> "Teléfono"
        PaymentCategory.LOANS -> "Préstamos"
        PaymentCategory.CREDIT_CARDS -> "Tarjetas de Crédito"
        PaymentCategory.INSURANCE -> "Seguros"
        PaymentCategory.RENT -> "Alquiler"
        PaymentCategory.SUBSCRIPTIONS -> "Suscripciones"
        PaymentCategory.TAXES -> "Impuestos"
        PaymentCategory.EDUCATION -> "Educación"
        PaymentCategory.HEALTH -> "Salud"
        PaymentCategory.ENTERTAINMENT -> "Entretenimiento"
        PaymentCategory.TRANSPORT -> "Transporte"
        PaymentCategory.OTHER -> "Otros"
    }
}

fun getCategoryIcon(category: PaymentCategory): androidx.compose.ui.graphics.vector.ImageVector {
    return when (category) {
        PaymentCategory.UTILITIES -> Icons.Default.Home
        PaymentCategory.WATER -> Icons.Default.WaterDrop
        PaymentCategory.ELECTRICITY -> Icons.Default.Bolt
        PaymentCategory.GAS -> Icons.Default.LocalFireDepartment
        PaymentCategory.INTERNET -> Icons.Default.Wifi
        PaymentCategory.PHONE -> Icons.Default.Phone
        PaymentCategory.LOANS -> Icons.Default.AccountBalance
        PaymentCategory.CREDIT_CARDS -> Icons.Default.CreditCard
        PaymentCategory.INSURANCE -> Icons.Default.Shield
        PaymentCategory.RENT -> Icons.Default.House
        PaymentCategory.SUBSCRIPTIONS -> Icons.Default.Subscriptions
        PaymentCategory.TAXES -> Icons.Default.Receipt
        PaymentCategory.EDUCATION -> Icons.Default.School
        PaymentCategory.HEALTH -> Icons.Default.LocalHospital
        PaymentCategory.ENTERTAINMENT -> Icons.Default.SportsEsports
        PaymentCategory.TRANSPORT -> Icons.Default.DirectionsCar
        PaymentCategory.OTHER -> Icons.Default.Category
    }
}