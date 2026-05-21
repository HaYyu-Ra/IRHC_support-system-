package com.example.data.network

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- Moshi Data Classes for Gemini API ---

data class GeminiPart(
    @Json(name = "text") val text: String? = null
)

data class GeminiContent(
    @Json(name = "parts") val parts: List<GeminiPart>
)

data class GeminiRequest(
    @Json(name = "contents") val contents: List<GeminiContent>,
    @Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null
)

data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent? = null
)

data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>? = null
)

// --- Retrofit API Service ---

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiNetworkClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val apiService: GeminiApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        retrofit.create(GeminiApiService::class.java)
    }

    suspend fun consultGemini(prompt: String, chatHistory: List<GeminiContent> = emptyList()): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "Note: Gemini API key is not yet configured in AI Studio secrets. Self-test interactive offline consultation mode is active. (To enable actual AI counseling, enter your GEMINI_API_KEY in the Secrets Panel)."
        }

        val systemInstruction = GeminiContent(
            parts = listOf(
                GeminiPart(
                    text = "You are a professional, compassionate, and trustworthy Reproductive Health Expert counselor, representing the Integrated Reproductive Health Awareness and Consultation System (IRHC) in Ethiopia. Provide medical, physical, and psychological guidance on family planning, maternal care, STIs/HIV prevention (testing/VCT), and adolescent reproductive growth safely, clearly, and without judgment. Structure your responses cleanly with concise, bulleted details where appropriate. Limit answers to two clear paragraphs. End with a compassionate sentence."
                )
            )
        )

        // Combine prompt and history if any
        val contents = ArrayList<GeminiContent>()
        contents.addAll(chatHistory)
        contents.add(GeminiContent(parts = listOf(GeminiPart(text = prompt))))

        val request = GeminiRequest(
            contents = contents,
            systemInstruction = systemInstruction
        )

        return try {
            val response = apiService.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "I apologize, I am temporarily unable to answer. Please request clinical assistance from our nearby physicians."
        } catch (e: Exception) {
            "Consultation Service Connection Message: ${e.localizedMessage}. The AI module is currently offline. Please check your network or try again."
        }
    }
}
