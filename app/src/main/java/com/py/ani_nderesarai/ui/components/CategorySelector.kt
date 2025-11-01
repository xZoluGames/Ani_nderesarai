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
import com.py.ani_nderesarai.data.model.PaymentCategory

@Composable
fun CategorySelector(
    selectedCategory: PaymentCategory,
    onCategorySelected: (PaymentCategory) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Categoría",
            style = MaterialTheme.typography.labelMedium
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(PaymentCategory.values()) { category ->
                CategoryChip(
                    category = category,
                    isSelected = category == selectedCategory,
                    onClick = { onCategorySelected(category) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryChip(
    category: PaymentCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        onClick = onClick,
        label = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = getCategoryIcon(category),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Text(getCategoryName(category))
            }
        },
        selected = isSelected
    )
}

fun getCategoryIcon(category: PaymentCategory): ImageVector {
    return when (category) {
        PaymentCategory.UTILITIES -> Icons.Default.ElectricBolt
        PaymentCategory.LOANS -> Icons.Default.AccountBalance
        PaymentCategory.CREDIT_CARDS -> Icons.Default.CreditCard
        PaymentCategory.INSURANCE -> Icons.Default.Security
        PaymentCategory.RENT -> Icons.Default.Home
        PaymentCategory.SUBSCRIPTIONS -> Icons.Default.Subscriptions
        PaymentCategory.TAXES -> Icons.Default.Receipt
        PaymentCategory.EDUCATION -> Icons.Default.School
        PaymentCategory.HEALTH -> Icons.Default.LocalHospital
        PaymentCategory.OTHER -> Icons.Default.Category
    }
}

fun getCategoryName(category: PaymentCategory): String {
    return when (category) {
        PaymentCategory.UTILITIES -> "Servicios"
        PaymentCategory.LOANS -> "Préstamos"
        PaymentCategory.CREDIT_CARDS -> "Tarjetas"
        PaymentCategory.INSURANCE -> "Seguros"
        PaymentCategory.RENT -> "Alquiler"
        PaymentCategory.SUBSCRIPTIONS -> "Suscripciones"
        PaymentCategory.TAXES -> "Impuestos"
        PaymentCategory.EDUCATION -> "Educación"
        PaymentCategory.HEALTH -> "Salud"
        PaymentCategory.OTHER -> "Otros"
    }
}