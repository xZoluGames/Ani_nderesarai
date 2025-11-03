package com.py.ani_nderesarai.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ============================================
// HEALTH & STATUS
// ============================================

@Serializable
data class HealthResponse(
    val status: String,
    @SerialName("whatsapp_connected") val whatsappConnected: Boolean,
    val timestamp: String
)

@Serializable
data class BotStatusResponse(
    val connected: Boolean,
    val user: String? = null,
    val phone: String? = null,
    val platform: String? = null,
    val message: String? = null,
    val error: String? = null
)

// ============================================
// VERIFICACIÓN
// ============================================

@Serializable
data class VerificationRequest(
    @SerialName("phone_number") val phoneNumber: String
)

@Serializable
data class VerificationResponse(
    val success: Boolean? = null,
    val message: String,
    @SerialName("expires_in") val expiresIn: Int? = null,
    val error: String? = null
)

@Serializable
data class ConfirmVerificationRequest(
    @SerialName("phone_number") val phoneNumber: String,
    val code: String
)

@Serializable
data class ConfirmVerificationResponse(
    val success: Boolean? = null,
    val message: String,
    @SerialName("phone_number") val phoneNumber: String? = null,
    val error: String? = null
)

@Serializable
data class VerificationStatusResponse(
    val verified: Boolean,
    @SerialName("verified_at") val verifiedAt: String? = null
)

// ============================================
// MENSAJES
// ============================================

@Serializable
data class SendMessageRequest(
    @SerialName("phone_number") val phoneNumber: String,
    val message: String,
    @SerialName("scheduled_at") val scheduledAt: String? = null
)

@Serializable
data class SendMessageResponse(
    val success: Boolean? = null,
    val message: String,
    val timestamp: String? = null,
    val id: Long? = null,
    @SerialName("scheduled_at") val scheduledAt: String? = null,
    val error: String? = null
)

@Serializable
data class ReminderForSummary(
    val title: String,
    val description: String? = null,
    val dueDate: String,
    val amount: Double? = null,
    val currency: String = "PYG",
    val daysUntilDue: Long
)

@Serializable
data class SendSummaryRequest(
    @SerialName("phone_number") val phoneNumber: String,
    val reminders: List<ReminderForSummary>
)

@Serializable
data class SendSummaryResponse(
    val success: Boolean? = null,
    val message: String,
    @SerialName("reminders_count") val remindersCount: Int? = null,
    val error: String? = null
)

@Serializable
data class MessageHistoryItem(
    val id: Long,
    @SerialName("phone_number") val phoneNumber: String,
    val message: String,
    @SerialName("sent_at") val sentAt: String,
    val success: Int
)

@Serializable
data class MessageHistoryResponse(
    @SerialName("phone_number") val phoneNumber: String,
    val messages: List<MessageHistoryItem>
)

@Serializable
data class ScheduledMessage(
    val id: Long,
    @SerialName("phone_number") val phoneNumber: String,
    val message: String,
    @SerialName("scheduled_at") val scheduledAt: String,
    @SerialName("sent_at") val sentAt: String? = null,
    val status: String,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class ScheduledMessagesResponse(
    @SerialName("phone_number") val phoneNumber: String,
    @SerialName("scheduled_messages") val scheduledMessages: List<ScheduledMessage>
)
