package com.py.ani_nderesarai.network

/**
 * Constantes del API REST del Bot
 * 
 * IMPORTANTE: Actualiza BASE_URL con la IP de tu VPS después del despliegue
 */
object ApiConstants {
    // URL base del API (actualizar con tu IP de VPS)
    const val BASE_URL = "http://83.147.38.240:3000"
    // Ejemplo para VPS: const val BASE_URL = "http://
    // 192.168.1.100:3000"
    // Ejemplo con dominio: const val BASE_URL = "https://api.tu-dominio.com"
    
    // Endpoints - Health & Status
    const val HEALTH = "/api/health"
    const val BOT_STATUS = "/api/bot/status"
    
    // Endpoints - Verificación
    const val VERIFY_REQUEST = "/api/verify/request"
    const val VERIFY_CONFIRM = "/api/verify/confirm"
    const val VERIFY_STATUS = "/api/verify/status/{phone_number}"
    
    // Endpoints - Mensajes
    const val SEND_MESSAGE = "/api/messages/send"
    const val SEND_SUMMARY = "/api/messages/send-summary"
    const val MESSAGE_HISTORY = "/api/messages/history/{phone_number}"
    const val SCHEDULED_MESSAGES = "/api/messages/scheduled/{phone_number}"
    
    // Timeout
    const val CONNECT_TIMEOUT = 30L // segundos
    const val READ_TIMEOUT = 30L // segundos
    const val WRITE_TIMEOUT = 30L // segundosg
}