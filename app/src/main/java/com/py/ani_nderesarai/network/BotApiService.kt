package com.py.ani_nderesarai.network

import com.py.ani_nderesarai.network.models.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Interfaz del servicio del API REST del Bot
 */
interface BotApiService {
    
    // ============================================
    // HEALTH & STATUS
    // ============================================
    
    @GET(ApiConstants.HEALTH)
    suspend fun getHealth(): Response<HealthResponse>
    
    @GET(ApiConstants.BOT_STATUS)
    suspend fun getBotStatus(): Response<BotStatusResponse>
    
    // ============================================
    // VERIFICACIÓN
    // ============================================
    
    @POST(ApiConstants.VERIFY_REQUEST)
    suspend fun requestVerification(
        @Body request: VerificationRequest
    ): Response<VerificationResponse>
    
    @POST(ApiConstants.VERIFY_CONFIRM)
    suspend fun confirmVerification(
        @Body request: ConfirmVerificationRequest
    ): Response<ConfirmVerificationResponse>
    
    @GET(ApiConstants.VERIFY_STATUS)
    suspend fun getVerificationStatus(
        @Path("phone_number") phoneNumber: String
    ): Response<VerificationStatusResponse>
    
    // ============================================
    // MENSAJES
    // ============================================
    
    @POST(ApiConstants.SEND_MESSAGE)
    suspend fun sendMessage(
        @Body request: SendMessageRequest
    ): Response<SendMessageResponse>
    
    @POST(ApiConstants.SEND_SUMMARY)
    suspend fun sendSummary(
        @Body request: SendSummaryRequest
    ): Response<SendSummaryResponse>
    
    @GET(ApiConstants.MESSAGE_HISTORY)
    suspend fun getMessageHistory(
        @Path("phone_number") phoneNumber: String
    ): Response<MessageHistoryResponse>
    
    @GET(ApiConstants.SCHEDULED_MESSAGES)
    suspend fun getScheduledMessages(
        @Path("phone_number") phoneNumber: String
    ): Response<ScheduledMessagesResponse>
}
