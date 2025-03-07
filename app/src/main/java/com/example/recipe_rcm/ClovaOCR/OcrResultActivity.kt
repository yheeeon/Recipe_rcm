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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
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

        capturedImageView = findViewById(R.id.capturedImageView)
        ocrRecyclerView = findViewById(R.id.ocrRecyclerView)

        ocrAdapter = OcrResultAdapter(this, ocrItemList)
        ocrRecyclerView.layoutManager = LinearLayoutManager(this)
        ocrRecyclerView.adapter = ocrAdapter

        val imageUri = intent.getStringExtra("image_uri")
        if (!imageUri.isNullOrEmpty()) {
            val bitmap = getBitmapFromUri(Uri.parse(imageUri))
            if (bitmap != null) {
                capturedImageView.setImageBitmap(bitmap)
                performOcr(bitmap)
            } else {
                showToastAndLog("이미지 로드 실패")
            }
        }
    }

    private fun getBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            Log.e("OcrResultActivity", "이미지 변환 오류: ${e.localizedMessage}")
            null
        }
    }

    private fun performOcr(bitmap: Bitmap) {
        val base64Image = bitmapToBase64(bitmap)

        if (base64Image.isEmpty()) {
            showToastAndLog("Base64 변환 실패")
            return
        }

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
                                    name = item.name?.text ?: "알 수 없음",
                                    count = item.count?.text ?: "1"
                                )
                            } ?: emptyList()

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

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
    }

    private fun showToastAndLog(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        Log.e("OcrResultActivity", message)
    }
}
