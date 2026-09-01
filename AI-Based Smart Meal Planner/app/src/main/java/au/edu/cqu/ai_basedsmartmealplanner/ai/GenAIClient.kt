package au.edu.cqu.ai_basedsmartmealplanner.ai

import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface GeminiApiService {
    @Headers("Content-Type: application/json")
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: @JvmSuppressWildcards Map<String, Any>
    ): retrofit2.Response<GeminiResponse>
}
object GenAIClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: GeminiApiService = retrofit.create(GeminiApiService::class.java)
}