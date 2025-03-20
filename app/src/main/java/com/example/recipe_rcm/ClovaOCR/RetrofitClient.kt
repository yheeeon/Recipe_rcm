import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "https://f5hfnd2eb6.apigw.ntruss.com/custom/v1/37636/1ff001d2a243205ad4d84609e4dcbc517854ef5f1e3babdc1d2e9b2779cd77fd/"
    private const val OCR_SECRET_KEY = "" //✅OCR SECRET KEY

    private val client = OkHttpClient.Builder()
        .addInterceptor(Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("X-OCR-SECRET", OCR_SECRET_KEY)
                .build()
            chain.proceed(request)
        })
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    val ocrService: OcrService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()
        .create(OcrService::class.java)
}
