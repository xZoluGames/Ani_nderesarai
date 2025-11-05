package com.py.ani_nderesarai.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.py.ani_nderesarai.data.model.RecurringType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringTypeSelector(
    selectedType: RecurringType,
    onTypeSelected: (RecurringType) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = getRecurringTypeLabel(selectedType),
            onValueChange = {},
            readOnly = true,
            label = { Text("Frecuencia") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            leadingIcon = { Icon(Icons.Default.Repeat, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            RecurringType.values().forEach { type ->
                DropdownMenuItem(
                    text = { Text(getRecurringTypeLabel(type)) },
                    onClick = {
                        onTypeSelected(type)
                        expanded = false
                    }
                )
            }
        }
    }
}

fun getRecurringTypeLabel(type: RecurringType): String {
    return when (type) {
        RecurringType.DAILY -> "Diario"
        RecurringType.WEEKLY -> "Semanal"
        RecurringType.BIWEEKLY -> "Quincenal"
        RecurringType.MONTHLY -> "Mensual"
        RecurringType.BIMONTHLY -> "Bimestral"
        RecurringType.QUARTERLY -> "Trimestral"
        RecurringType.SEMI_ANNUAL -> "Semestral"
        RecurringType.ANNUAL -> "Anual"
        RecurringType.CUSTOM -> "Personalizado"
    }
}