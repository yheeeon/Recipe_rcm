package com.example.recipe_rcm.RecipeAPI

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.recipe_rcm.RecipeAPI.model.Ingredient
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UpdateExpirationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    // 워크 실행 시 호출되는 함수, 백그라운드에서 만료 상태를 갱신
    override suspend fun doWork(): Result {
        try {
            val database = FirebaseDatabase.getInstance().getReference("ingredients")
            // DB에서 재료 데이터 한 번 읽기
            database.get().addOnSuccessListener { snapshot ->
                for (child in snapshot.children) {
                    val ingredient = child.getValue(Ingredient::class.java)
                    if (ingredient != null) {
                        val updatedExpirationStatus = calculateExpirationStatus(ingredient.expiration)
                        val updatedIngredient = ingredient.copy(expirationStatus = updatedExpirationStatus)
                        child.ref.setValue(updatedIngredient) // Firebase에 갱신
                    }
                }
            }.addOnFailureListener {
                // 실패 처리
            }
            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure()
        }
    }
    // 만료일 문자열을 받아 오늘 날짜와 비교해 상태 문자열 반환
    private fun calculateExpirationStatus(expiration: String): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val expirationDate = sdf.parse(expiration) ?: return "유효하지 않은 날짜"
        val today = Date()

        val diff = expirationDate.time - today.time
        val daysLeft = (diff / (1000 * 60 * 60 * 24)).toInt()
        // 남은 일수에 따라 상태 문자열 반환
        return when {
            daysLeft > 0 -> "$daysLeft 일 남음"
            daysLeft == 0 -> "오늘까지"
            else -> "${-daysLeft}일 지남"
        }
    }
}
