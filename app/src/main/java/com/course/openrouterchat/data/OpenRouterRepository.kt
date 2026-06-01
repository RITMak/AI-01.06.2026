package com.course.openrouterchat.data

import com.course.openrouterchat.BuildConfig
import com.course.openrouterchat.data.api.OpenRouterApi
import com.course.openrouterchat.data.dto.ChatMessageDto
import com.course.openrouterchat.data.dto.ChatRequest
import com.course.openrouterchat.data.dto.ErrorResponse
import com.course.openrouterchat.data.dto.ModelDto
import com.course.openrouterchat.data.model.ModelUi
import com.squareup.moshi.Moshi
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class OpenRouterRepository(
    private val api: OpenRouterApi = ApiClient.openRouterApi,
    private val moshi: Moshi = ApiClient.moshiInstance,
) {

    fun ensureApiKey(): Result<Unit> {
        if (BuildConfig.OPENROUTER_API_KEY.isBlank()) {
            return Result.failure(
                ApiException("Добавьте OPENROUTER_API_KEY в local.properties"),
            )
        }
        return Result.success(Unit)
    }

    suspend fun getFreeModels(): Result<List<ModelUi>> {
        return try {
            ensureApiKey().getOrThrow()
            val models = api.getModels().data
            val free = models
                .filter(::isFreeModel)
                .sortedBy { it.name ?: it.id }
                .map { dto ->
                    ModelUi(
                        id = dto.id,
                        displayName = dto.name?.takeIf { it.isNotBlank() } ?: dto.id,
                    )
                }
            Result.success(free)
        } catch (t: Throwable) {
            Result.failure(mapThrowable(t))
        }
    }

    suspend fun sendChat(modelId: String, userMessage: String): Result<String> {
        return try {
            ensureApiKey().getOrThrow()
            val response = api.createChatCompletion(
                ChatRequest(
                    model = modelId,
                    messages = listOf(
                        ChatMessageDto(role = "user", content = userMessage),
                    ),
                    stream = false,
                ),
            )
            val content = response.choices
                ?.firstOrNull()
                ?.message
                ?.content
                ?.trim()
            if (content.isNullOrEmpty()) {
                throw ApiException("Модель вернула пустой ответ")
            }
            Result.success(content)
        } catch (t: Throwable) {
            Result.failure(mapThrowable(t))
        }
    }

    private fun isFreeModel(model: ModelDto): Boolean {
        val prompt = model.pricing?.prompt?.toDoubleOrNull() ?: 1.0
        val completion = model.pricing?.completion?.toDoubleOrNull() ?: 1.0
        return prompt == 0.0 && completion == 0.0
    }

    private fun mapThrowable(throwable: Throwable): ApiException {
        if (throwable is ApiException) return throwable

        when (throwable) {
            is UnknownHostException,
            is SocketTimeoutException,
            is IOException,
            -> return ApiException("Нет соединения", throwable)

            is HttpException -> {
                val code = throwable.code()
                val message = parseErrorMessage(throwable) ?: throwable.message()
                return when (code) {
                    401 -> ApiException("Неверный API-ключ", throwable)
                    else -> ApiException(
                        message?.takeIf { it.isNotBlank() }
                            ?: "Ошибка сервера ($code)",
                        throwable,
                    )
                }
            }
        }

        return ApiException(
            throwable.message ?: "Неизвестная ошибка",
            throwable,
        )
    }

    private fun parseErrorMessage(httpException: HttpException): String? {
        val body = httpException.response()?.errorBody()?.string() ?: return null
        return try {
            moshi.adapter(ErrorResponse::class.java).fromJson(body)?.error?.message
        } catch (_: Exception) {
            null
        }
    }
}

class ApiException(
    override val message: String,
    override val cause: Throwable? = null,
) : Exception(message, cause)
