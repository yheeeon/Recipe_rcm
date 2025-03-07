package com.example.recipe_rcm

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class Explain : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.explain)  //
        // btn_next 버튼 클릭 시 MainActivity로 이동
        val btnNext = findViewById<Button>(R.id.btn_next)
        btnNext.setOnClickListener {

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

            finish()  // 현재 ExplainActivity 종료 (뒤로 가기 방지)
        }
    }
}
