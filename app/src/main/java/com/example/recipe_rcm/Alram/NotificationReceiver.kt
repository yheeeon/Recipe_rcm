package com.example.recipe_rcm.Alram

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val channelId = "expiration_channel"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        //알림 채널
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "유통기한 알림",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // intent에서 받은 재료 이름 가져오기(비어있으면 기본 값 사용)
        val ingredientName =
            intent.getStringExtra("ingredientName")?.takeIf { it.isNotEmpty() } ?: "알 수 없는 재료"

        Log.d("PushAlarm", "Received Ingredient Name: $ingredientName")

        //알림 생성
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("유통기한 알림")
            .setContentText("$ingredientName 의 유통기한이 오늘입니다!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(ingredientName.hashCode(), notification)
    }
}