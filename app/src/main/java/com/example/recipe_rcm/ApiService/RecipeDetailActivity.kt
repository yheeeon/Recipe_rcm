package com.example.recipe_rcm.ApiService

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.recipe_rcm.R
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class RecipeDetailActivity : AppCompatActivity() {

    private lateinit var favoriteButton: Button
    private lateinit var recipe: Recipe
    private val databaseReference: DatabaseReference =
        FirebaseDatabase.getInstance().reference.child("favorites")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recipe_item) // 올바른 레이아웃 파일로 변경

        // 레시피 데이터 가져오기
        recipe = intent.getParcelableExtra<Recipe>("recipe") ?: return

        // UI 업데이트
        findViewById<TextView>(R.id.recipeName).text = recipe.name
        findViewById<TextView>(R.id.recipeCategory).text = "카테고리: ${recipe.category}"
        findViewById<TextView>(R.id.recipeMethod).text = "조리법: ${recipe.method}"
        findViewById<TextView>(R.id.recipeCalories).text = "열량: ${recipe.calories}"
        findViewById<TextView>(R.id.recipeIngredients).text = "재료: ${recipe.ingredients}"

        // Glide를 사용해 메인 이미지 표시
        Glide.with(this)
            .load(recipe.imageUrl)
            .into(findViewById(R.id.recipeImage))

        // 만드는 순서 동적 추가
        val stepsLayout = findViewById<LinearLayout>(R.id.stepsLayout)
        for ((index, step) in recipe.steps.withIndex()) {
            val stepTextView = TextView(this).apply {
                text = "${index + 1}. $step"
                textSize = 16f
                setPadding(0, 8, 0, 8)
            }
            stepsLayout.addView(stepTextView)
        }

        // 즐겨찾기 버튼 처리
        favoriteButton = findViewById(R.id.favorite_button)

        // Firebase에서 즐겨찾기 상태 가져오기
        checkFavoriteStatus()

        // 즐겨찾기 버튼 클릭 처리
        favoriteButton.setOnClickListener {
            toggleFavorite()
        }
    }

    // Firebase에서 즐겨찾기 상태 확인
    private fun checkFavoriteStatus() {
        databaseReference.child(recipe.name).get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {
                recipe.isFavorite = true
            } else {
                recipe.isFavorite = false
            }
            updateFavoriteButton()
        }
    }

    // 즐겨찾기 상태에 따라 버튼 텍스트 업데이트
    private fun updateFavoriteButton() {
        if (recipe.isFavorite) {
            favoriteButton.text = "즐겨찾기 삭제"
            favoriteButton.setTextColor(Color.RED)
        } else {
            favoriteButton.text = "즐겨찾기 추가"
            favoriteButton.setTextColor(Color.GREEN)
        }
    }

    // 즐겨찾기 상태 토글 및 Firebase 업데이트
    private fun toggleFavorite() {
        if (recipe.isFavorite) {
            // 즐겨찾기 삭제
            databaseReference.child(recipe.name).removeValue()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        recipe.isFavorite = false
                        updateFavoriteButton()
                        Toast.makeText(this, "즐겨찾기에서 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "즐겨찾기 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            // 즐겨찾기 추가
            databaseReference.child(recipe.name).setValue(recipe)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        recipe.isFavorite = true
                        updateFavoriteButton()
                        Toast.makeText(this, "즐겨찾기에 추가되었습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "즐겨찾기 추가에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}




