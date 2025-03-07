package com.example.recipe_rcm

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class Splash : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash)  // 스플래시 화면 레이아웃 설정

        // Handler를 사용하여 2초 후에 다음 액티비티로 이동
        Handler(Looper.getMainLooper()).postDelayed({
            // Splash 화면이 끝난 후 ExplainActivity로 이동
            val intent = Intent(this, Explain::class.java)
            startActivity(intent)
            finish()  // 현재 SplashActivity를 종료하여 뒤로 가기 방지
        }, 2000)  // 3초 동안 Splash 화면을 표시
    }
}
