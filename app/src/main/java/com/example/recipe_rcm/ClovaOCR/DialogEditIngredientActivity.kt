package com.example.recipe_rcm.ClovaOCR

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.recipe_rcm.R
import com.example.recipe_rcm.RecipeAPI.model.Ingredient
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class DialogEditIngredientActivity : AppCompatActivity() {

    private lateinit var tvIngredientName: EditText
    private lateinit var etIngredientCount: EditText
    private lateinit var tvExpiration: TextView
    private lateinit var radioGroupStorage: RadioGroup
    private lateinit var btnConfirm: Button
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_edit_ingredient)

        // Firebase 실시간 데이터베이스 초기화
        database = FirebaseDatabase.getInstance().reference

        // 뷰 바인딩
        tvIngredientName = findViewById(R.id.tvIngredientName)
        etIngredientCount = findViewById(R.id.etIngredientCount)
        tvExpiration = findViewById(R.id.tvExpiration)
        radioGroupStorage = findViewById(R.id.radioGroupStorage)
        btnConfirm = findViewById(R.id.btnConfirm)

        // intent로 받은 식재료 정보 세팅
        val name = intent.getStringExtra("name") ?: ""
        val count = intent.getStringExtra("count") ?: ""

        tvIngredientName.setText(name)
        etIngredientCount.setText(count)

        // 날짜 선택 다이얼로그
        tvExpiration.setOnClickListener { showDatePickerDialog() }

        // 확인 버튼 클릭 시 Firebase에 저장
        btnConfirm.setOnClickListener { saveIngredientToFirebase() }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, day ->
                val formattedDate = "$year-${month + 1}-$day"
                tvExpiration.text = formattedDate
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun saveIngredientToFirebase() {
        val name = tvIngredientName.text.toString().trim()
        val count = etIngredientCount.text.toString().trim()
        val expiration = tvExpiration.text.toString().trim()

        val selectedStorageId = radioGroupStorage.checkedRadioButtonId
        val storageStatus = if (selectedStorageId != -1) {
            findViewById<RadioButton>(selectedStorageId).text.toString()
        } else {
            "알 수 없음"
        }

        val expirationStatus = calculateExpirationStatus(expiration)

        if (name.isEmpty() || count.isEmpty() || expiration.isEmpty()) {
            Toast.makeText(this, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val ingredientId = database.push().key
        if (ingredientId != null) {
            val ingredient = Ingredient(
                name = name,
                count = count,
                expiration = expiration,
                expirationStatus = expirationStatus,
                storageStatus = storageStatus
            )
            database.child("ingredients").child(ingredientId)
                .setValue(ingredient)
                .addOnCompleteListener {
                    Toast.makeText(this, "저장 완료", Toast.LENGTH_SHORT).show()
                    finish()
                }
        }
    }

    // 유통기한 상태 계산 함수
    private fun calculateExpirationStatus(expiration: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val expirationDate = sdf.parse(expiration)
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            val diff = expirationDate.time - today.time
            val daysLeft = (diff / (1000 * 60 * 60 * 24)).toInt()

            when {
                daysLeft > 0 -> "$daysLeft 일 남음"
                daysLeft == 0 -> "오늘까지"
                else -> "${-daysLeft}일 지남"
            }
        } catch (e: Exception) {
            "유효하지 않은 날짜"
        }
    }
}
