package com.example.recipe_rcm.ClovaOCR

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recipe_rcm.R
import com.google.gson.JsonObject
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.InputStream

class OcrResultActivity : AppCompatActivity() {

    private lateinit var capturedImageView: ImageView
    private lateinit var ocrRecyclerView: RecyclerView
    private lateinit var ocrAdapter: OcrResultAdapter
    private val ocrItemList = mutableListOf<OcrItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ocr_result)
        //view 초기화
        capturedImageView = findViewById(R.id.capturedImageView)
        ocrRecyclerView = findViewById(R.id.ocrRecyclerView)
        // RecyclerView 설정
        ocrAdapter = OcrResultAdapter(this, ocrItemList)
        ocrRecyclerView.layoutManager = LinearLayoutManager(this)
        ocrRecyclerView.adapter = ocrAdapter
        // 전달받은 이미지 URI 로드 및 OCR 수행
        val imageUri = intent.getStringExtra("image_uri")
        if (!imageUri.isNullOrEmpty()) {
            val bitmap = getBitmapFromUri(Uri.parse(imageUri))
            if (bitmap != null) {
                capturedImageView.setImageBitmap(bitmap)
                performOcr(bitmap)
            }
        }
    }
    // URI로부터 Bitmap 이미지로 변환
    private fun getBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            Log.e("OcrResultActivity", "이미지 변환 오류: ${e.localizedMessage}")
            null
        }
    }
    // HTML 태그 제거 함수 (OCR 결과에 포함된 태그 제거)
    private fun removeHtmlTags(input: String): String {
        return input.replace(Regex("<[^>]*>"), "")
    }
    // OCR 처리 및 Retrofit 요청
    private fun performOcr(bitmap: Bitmap) {
        val base64Image = bitmapToBase64(bitmap)

        if (base64Image.isEmpty()) {
            return
        }
        // Clova OCR API에 보낼 JSON 문자열 구성
        val requestJson = """
    {
        "version": "V2",
        "requestId": "test-${System.currentTimeMillis()}",
        "timestamp": ${System.currentTimeMillis()},
        "images": [
            {
                "format": "jpg",
                "name": "receipt",
                "data": "$base64Image"
            }
        ]
    }
    """.trimIndent()

        Log.d("OCR_DEBUG", "Request JSON: $requestJson")

        val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), requestJson)
        val ocrService = RetrofitClient.ocrService
        // 비동기 OCR 요청
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response: Response<OcrResponse> = ocrService.getOcrResult(requestBody)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val ocrResponse = response.body()
                        val extractedItems = ocrResponse?.images?.getOrNull(0)?.receipt?.result?.subResults
                            ?.flatMap { it.items ?: emptyList() }
                            ?.map { item ->
                                OcrItem(
                                    name = removeHtmlTags(item.name?.text ?: "알 수 없음"), // HTML 태그 제거
                                    count = item.count?.text ?: "1"
                                )
                            } ?: emptyList()

                        // OCR 결과를 Flask 서버로 전송
                        sendDataToFlaskServer(extractedItems)

                        ocrItemList.clear()
                        ocrItemList.addAll(extractedItems)
                        ocrAdapter.notifyDataSetChanged()
                    } else {
                        showToastAndLog("OCR 요청 실패: ${response.errorBody()?.string()}")
                    }
                }
            } catch (e: Exception) {
                showToastAndLog("OCR 요청 중 오류 발생: ${e.localizedMessage}")
            }
        }
    }

    // Flask 서버로 추출된 식재료 데이터 전송
    private fun sendDataToFlaskServer(items: List<OcrItem>) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient()
                val jsonObject = JsonObject()
                val jsonArray = JsonArray()
                items.forEach { jsonArray.add(it.name) }
                jsonObject.add("products", jsonArray)

                val jsonBody = jsonObject.toString()
                Log.d("OCR_DEBUG", "Flask 전송 데이터: $jsonBody")

                val body = RequestBody.create("application/json".toMediaTypeOrNull(), jsonBody)
                val request = Request.Builder()
                    .url("/ocr") // ✅Flask 서버 URL, ex:"http://000.000.00.000:5000/ocr"
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    // 서버 응답에서 식품만 추출
                    val foodItems = parseFoodItemsFromResponse(responseBody)
                    withContext(Dispatchers.Main) {
                        ocrItemList.clear()
                        ocrItemList.addAll(foodItems)
                        ocrAdapter.notifyDataSetChanged()

                        showToastAndLog("서버로 데이터 전송 성공 및 식품 목록 표시")
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        showToastAndLog("서버 응답 실패: ${response.code}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToastAndLog("Flask 서버 전송 중 오류 발생: ${e.localizedMessage}")
                }
            }
        }
    }
    // 서버 응답 JSON 파싱하여 식품 목록만 추출
    private fun parseFoodItemsFromResponse(responseBody: String?): List<OcrItem> {
        val foodItemList = mutableListOf<OcrItem>()
        try {
            val jsonObject = JsonParser.parseString(responseBody).asJsonObject
            if (jsonObject.get("status").asString == "success") {
                val classifiedProducts = jsonObject.getAsJsonArray("classified_products")
                classifiedProducts?.forEach { product ->
                    val productObject = product.asJsonObject
                    val name = productObject.get("original_name").asString
                    val foodItem = OcrItem(name, "1")  // 기본적으로 "1" 개수 설정
                    foodItemList.add(foodItem)
                }
            }
        } catch (e: Exception) {
            Log.e("OcrResultActivity", "식품 항목 파싱 오류: ${e.localizedMessage}")
        }
        return foodItemList
    }

    // Bitmap 이미지를 Base64 인코딩
    private fun bitmapToBase64(bitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
    }

    // 로그 및 Toast 메시지 출력
    private fun showToastAndLog(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
        Log.e("OcrResultActivity", message)
    }
}
