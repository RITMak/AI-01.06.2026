package com.course.openrouterchat.data.api

import com.course.openrouterchat.data.dto.ChatRequest
import com.course.openrouterchat.data.dto.ChatResponse
import com.course.openrouterchat.data.dto.ModelsResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface OpenRouterApi {

    @GET("models")
    suspend fun getModels(): ModelsResponse

    @POST("chat/completions")
    suspend fun createChatCompletion(@Body request: ChatRequest): ChatResponse
}
