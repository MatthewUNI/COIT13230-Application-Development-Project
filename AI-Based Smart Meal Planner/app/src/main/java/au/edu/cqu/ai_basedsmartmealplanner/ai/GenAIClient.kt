package au.edu.cqu.ai_basedsmartmealplanner.ai

import au.edu.cqu.ai_basedsmartmealplanner.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface GenAIApiService {

    @Headers("Content-Type: application/json")
    @POST("v1beta/models/gemini-3.5-flash-lite:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: @JvmSuppressWildcards Map<String, Any>
    ): Response<GeminiResponse>
}

object GenAIClient {

    private const val BASE_URL =
        "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: GenAIApiService =
        retrofit.create(GenAIApiService::class.java)

    suspend fun fetchMealPlanAsync(prompt: String): GeminiResponse? {

        return withContext(Dispatchers.IO) {
            try {

                val requestPayload: Map<String, Any> = mapOf(
                    "contents" to listOf(
                        mapOf(
                            "parts" to listOf(
                                mapOf(
                                    "text" to prompt
                                )
                            )
                        )
                    )
                )

                val response = apiService.generateContent(
                    apiKey = BuildConfig.GEMINI_API_KEY,
                    request = requestPayload
                )

                if (response.isSuccessful) {
                    response.body()
                } else {
                    println(
                        "Gemini API Error: ${response.code()} " +
                                response.errorBody()?.string()
                    )
                    null
                }

            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}