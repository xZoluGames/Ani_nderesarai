package com.py.ani_nderesarai.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.py.ani_nderesarai.data.model.RecurringType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringTypeSelector(
    selectedType: RecurringType,
    onTypeSelected: (RecurringType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Frecuencia",
            style = MaterialTheme.typography.labelMedium
        )
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = getRecurringTypeName(selectedType),
                onValueChange = { },
                readOnly = true,
                label = { Text("Repetir cada") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
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
                        text = { Text(getRecurringTypeName(type)) },
                        onClick = {
                            onTypeSelected(type)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

fun getRecurringTypeName(type: RecurringType): String {
    return when (type) {
        RecurringType.WEEKLY -> "Semana"
        RecurringType.MONTHLY -> "Mes"
        RecurringType.QUARTERLY -> "3 Meses"
        RecurringType.SEMI_ANNUAL -> "6 Meses"
        RecurringType.ANNUAL -> "Año"
        RecurringType.CUSTOM -> "Personalizado"
    }
}