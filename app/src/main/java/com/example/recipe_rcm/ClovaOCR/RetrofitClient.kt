import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "https://f5hfnd2eb6.apigw.ntruss.com/custom/v1/37636/1ff001d2a243205ad4d84609e4dcbc517854ef5f1e3babdc1d2e9b2779cd77fd/"
    private const val OCR_SECRET_KEY = "=" //OCR SECRET KEY
    // OkHttpClient 설정: HTTP 요청 시 인터셉터로 헤더에 OCR 시크릿 키 추가
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
    // Retrofit 인스턴스 생성 및 OCR API 서비스 인터페이스 생성
    val ocrService: OcrService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()
        .create(OcrService::class.java)
}
