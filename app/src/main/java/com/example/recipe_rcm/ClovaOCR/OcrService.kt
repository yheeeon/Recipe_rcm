import com.example.recipe_rcm.ClovaOCR.OcrResponse
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface OcrService {
    @Headers("Content-Type: application/json")
    @POST("document/receipt")
    suspend fun getOcrResult(@Body requestBody: RequestBody): Response<OcrResponse>
}
