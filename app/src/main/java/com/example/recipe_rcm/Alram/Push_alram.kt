package com.example.recipe_rcm.Alram

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.*
import com.example.recipe_rcm.R
import java.text.SimpleDateFormat
import java.util.*

class Push_alram : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var tvSelectedTime: TextView
    private lateinit var switchAlarm: Switch
    private var isAlarmOn: Boolean = true // 알림 ON/OFF 상태
    private var alarmHour: Int = 0 // 설정된 알림 시간 (시)
    private var alarmMinute: Int = 0 // 설정된 알림 시간 (분)
    private val sharedPreferences by lazy { getSharedPreferences("PushAlarmPrefs", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.push_alram)

        database = FirebaseDatabase.getInstance().getReference("ingredients")
        tvSelectedTime = findViewById(R.id.tvSelectedTime)
        switchAlarm = findViewById(R.id.switchAlarm)
        val timePicker: TimePicker = findViewById(R.id.timePicker)
        val btnSetAlarm: Button = findViewById(R.id.btnSetAlarm)

        // Android 13 이상 알림 권한 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    100
                )
            }
        }

        // 저장된 알림 상태 및 시간 복원
        restoreAlarmState()

        // TimePicker 초기값 설정
        timePicker.hour = alarmHour
        timePicker.minute = alarmMinute

        // 알림 ON/OFF 스위치 리스너
        switchAlarm.setOnCheckedChangeListener { _, isChecked ->
            isAlarmOn = isChecked
            saveAlarmState() // 상태 저장
            Toast.makeText(
                this,
                if (isChecked) "푸시 알림이 활성화되었습니다." else "푸시 알림이 비활성화되었습니다.",
                Toast.LENGTH_SHORT
            ).show()
        }

        btnSetAlarm.setOnClickListener {
            alarmHour = timePicker.hour
            alarmMinute = timePicker.minute

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, alarmHour)
                set(Calendar.MINUTE, alarmMinute)
                set(Calendar.SECOND, 0)
            }

            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.DATE, 1)
            }

            val formattedTime = String.format("%02d:%02d", alarmHour, alarmMinute)
            tvSelectedTime.text = "설정된 시간: $formattedTime"
            saveAlarmState() // 새로운 알림 시간 저장

            if (isAlarmOn) {
                setAlarm(calendar.timeInMillis)
                checkExpirationAndNotify(calendar.timeInMillis)
                Toast.makeText(this, "알림이 $formattedTime 에 설정되었습니다.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "알림이 비활성화된 상태입니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag", "ScheduleExactAlarm")
    private fun setAlarm(timeInMillis: Long) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            timeInMillis,
            pendingIntent
        )
    }

    private fun checkExpirationAndNotify(alarmTimeInMillis: Long) {
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val todayMillis = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(todayDate)?.time

        database.get().addOnSuccessListener { snapshot ->
            var hasValidIngredient = false // 오늘까지인 재료가 있는지 확인

            snapshot.children.forEach { ingredient ->
                val expirationDate = ingredient.child("expiration").value.toString()
                val expirationStatus = ingredient.child("expirationStatus").value.toString()
                val name = ingredient.child("name").value.toString()

                val expirationMillis =
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(expirationDate)?.time

                if (expirationMillis == todayMillis && expirationStatus == "오늘까지") {
                    hasValidIngredient = true
                    sendExpirationNotificationAtTime(name, alarmTimeInMillis)
                }
            }

            if (!hasValidIngredient) {
                Log.d("PushAlarm", "No ingredients with expiration date today. No notification scheduled.")
            }
        }.addOnFailureListener { exception ->
            Log.e("PushAlarm", "Failed to fetch ingredients from Firebase", exception)
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag", "ScheduleExactAlarm")
    private fun sendExpirationNotificationAtTime(ingredientName: String, alarmTimeInMillis: Long) {
        val intent = Intent(this, NotificationReceiver::class.java).apply {
            putExtra("ingredientName", ingredientName)
        }

        val requestCode = ingredientName.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            alarmTimeInMillis,
            pendingIntent
        )

        Log.d("PushAlarm", "Notification scheduled for $ingredientName at ${Date(alarmTimeInMillis)}")
    }

    // 알림 상태 저장
    private fun saveAlarmState() {
        sharedPreferences.edit().apply {
            putInt("hour", alarmHour)
            putInt("minute", alarmMinute)
            putBoolean("isAlarmOn", isAlarmOn)
            apply()
        }
    }

    // 알림 상태 복원
    private fun restoreAlarmState() {
        alarmHour = sharedPreferences.getInt("hour", 12) // 기본값: 12시
        alarmMinute = sharedPreferences.getInt("minute", 0) // 기본값: 0분
        isAlarmOn = sharedPreferences.getBoolean("isAlarmOn", true) // 기본값: ON 상태

        val formattedTime = String.format("%02d:%02d", alarmHour, alarmMinute)
        tvSelectedTime.text = "설정된 시간: $formattedTime"
        switchAlarm.isChecked = isAlarmOn
    }
}

