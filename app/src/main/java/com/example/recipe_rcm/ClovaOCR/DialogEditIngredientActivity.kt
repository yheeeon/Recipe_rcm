package com.example.recipe_rcm.ClovaOCR

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.recipe_rcm.R
import com.example.recipe_rcm.RecipeAPI.model.Ingredient
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
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

        //intent로 받은 식재료 정보를 입력 필드 채움
        val name = intent.getStringExtra("name") ?: ""
        val count = intent.getStringExtra("count") ?: ""

        tvIngredientName.setText(name)
        etIngredientCount.setText(count)

        //유통기한 textvew 클릭 -> 날짜 선택 다이얼로그 표시
        tvExpiration.setOnClickListener { showDatePickerDialog() }
        //저장 버튼 클릭 -> Firebase에 저장
        btnConfirm.setOnClickListener { saveIngredientToFirebase() }
    }
    //날짜 선택 다이얼로그 표시, 선택한 날짜를 textview에 표시
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(this, { _, year, month, day ->
            val formattedDate = "$year-${month + 1}-$day"
            tvExpiration.text = formattedDate
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        datePickerDialog.show()
    }

    //입력한 정보를 실시간 Firebase에 저장
    private fun saveIngredientToFirebase() {
        val name = tvIngredientName.text.toString().trim()
        val count = etIngredientCount.text.toString().trim()
        val expiration = tvExpiration.text.toString().trim()

        val selectedStorageId = radioGroupStorage.checkedRadioButtonId
        val storage = findViewById<RadioButton>(selectedStorageId)?.text?.toString() ?: "알 수 없음"

        if (name.isEmpty() || count.isEmpty() || expiration.isEmpty()) {
            Toast.makeText(this, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }
        //고유 키를 생성하여 ingredients 경로에 데이터 저장
        val ingredientId = database.push().key
        if (ingredientId != null) {
            database.child("ingredients").child(ingredientId).setValue(Ingredient(name, count, expiration, storage))
                .addOnCompleteListener {
                    Toast.makeText(this, "저장 완료", Toast.LENGTH_SHORT).show()
                    finish()
                }
        }
    }
}
