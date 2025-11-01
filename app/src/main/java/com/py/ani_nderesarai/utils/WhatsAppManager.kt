package com.py.ani_nderesarai.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.py.ani_nderesarai.data.model.PaymentReminder
import java.time.format.DateTimeFormatter

class WhatsAppManager {
    
    fun sendReminder(context: Context, reminder: PaymentReminder): Boolean {
        return try {
            val message = buildReminderMessage(reminder)
            val phoneNumber = reminder.whatsappNumber.replace("+", "").replace(" ", "")
            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://api.whatsapp.com/send?phone=$phoneNumber&text=${Uri.encode(message)}")
                setPackage("com.whatsapp")
            }
            
            // Verificar si WhatsApp está instalado
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                true
            } else {
                // Intentar con WhatsApp Business
                intent.setPackage("com.whatsapp.w4b")
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                    true
                } else {
                    // Abrir en navegador web
                    intent.setPackage(null)
                    context.startActivity(intent)
                    true
                }
            }
        } catch (e: Exception) {
            false
        }
    }
    
    private fun buildReminderMessage(reminder: PaymentReminder): String {
        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        
        return if (reminder.customMessage.isNotBlank()) {
            reminder.customMessage
        } else {
            buildString {
                append("🔔 *Recordatorio de Pago - Ani Nderesarai*\n\n")
                append("📝 *${reminder.title}*\n")
                
                if (reminder.description.isNotBlank()) {
                    append("📋 ${reminder.description}\n")
                }
                
                append("📅 Vence: ${reminder.dueDate.format(dateFormatter)}\n")
                
                reminder.amount?.let { amount ->
                    append("💰 Monto: ${formatCurrency(amount, reminder.currency)}\n")
                }
                
                append("\n¡No olvides realizar tu pago a tiempo! ⏰")
            }
        }
    }
    
    private fun formatCurrency(amount: Double, currency: String): String {
        return when (currency) {
            "PYG" -> "${String.format("%,.0f", amount)} Gs."
            "USD" -> "$${String.format("%.2f", amount)}"
            else -> "${String.format("%.2f", amount)} $currency"
        }
    }
}