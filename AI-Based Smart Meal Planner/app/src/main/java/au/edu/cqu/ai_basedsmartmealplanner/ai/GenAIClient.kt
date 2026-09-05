package au.edu.cqu.ai_basedsmartmealplanner.ai

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Defining the POST request to the external AI service
interface GenAIApiService {
    @Headers("Content-Type: application/json")
    @POST("v1/models/gemini-1.5:generateContent")
    suspend fun generateMealPlan(@Body promptPayload: Map<String, String>): retrofit2.Response<String>
}

object GenAIClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: GenAIApiService = retrofit.create(GenAIApiService::class.java)

    // Executes the network call safely on a background thread
    suspend fun fetchMealPlanAsync(prompt: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val payload = mapOf("prompt" to prompt)
                val response = apiService.generateMealPlan(payload)
                if (response.isSuccessful) response.body() else null
            } catch (e: Exception) {
                // Catch network timeouts or JSON parsing errors to prevent UI crashes
                e.printStackTrace()
                null
            }
        }
    }
}