package au.edu.cqu.ai_basedsmartmealplanner.ai

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Defining the POST request to the external AI service
interface GenAIApiService {
    @retrofit2.http.Headers("Content-Type: application/json")
    @retrofit2.http.POST("v1beta/models/gemini-3.6-flash:generateContent")
    suspend fun generateContent(
        @retrofit2.http.Query("key") apiKey: String,
        @retrofit2.http.Body request: Any
    ): retrofit2.Response<GeminiResponse>
}
object GenAIClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: GenAIApiService = retrofit.create(GenAIApiService::class.java)

    // Executes the network call safely on a background thread
    suspend fun fetchMealPlanAsync(prompt: String): GeminiResponse? {
        return withContext(Dispatchers.IO) {
            try {
                val payload = mapOf("prompt" to prompt)
                val response = apiService.generateContent(apiKey = "YOUR_API_KEY", request = payload)
                if (response.isSuccessful) response.body() else null
            } catch (e: Exception) {
                // Catch network timeouts or JSON parsing errors to prevent UI crashes
                e.printStackTrace()
                null
            }
        }
    }
}