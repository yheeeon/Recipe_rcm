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
    private lateinit var database: DatabaseReference //Firebase DB 참조
    private lateinit var tvSelectedTime: TextView
    private lateinit var switchAlarm: Switch
    private var isAlarmOn: Boolean = true // 알림 ON/OFF 상태
    private var alarmHour: Int = 0 // 설정된 알림 시간 (시)
    private var alarmMinute: Int = 0 // 설정된 알림 시간 (분)
    private val sharedPreferences by lazy { getSharedPreferences("PushAlarmPrefs", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.push_alram)

        //firebase DB의 'ingredients' 경로 참조
        database = FirebaseDatabase.getInstance().getReference("ingredients")

        //뷰 초기호
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

        // '알림 설정'버튼 클릭 시 실행
        btnSetAlarm.setOnClickListener {
            alarmHour = timePicker.hour
            alarmMinute = timePicker.minute

            //알람 시간을 Calender 객체로 설정
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, alarmHour)
                set(Calendar.MINUTE, alarmMinute)
                set(Calendar.SECOND, 0)
            }
            //과거 시간이면 내일로 설정
            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.DATE, 1)
            }

            //UI에 설정된 시간 표시
            val formattedTime = String.format("%02d:%02d", alarmHour, alarmMinute)
            tvSelectedTime.text = "설정된 시간: $formattedTime"
            saveAlarmState()

            //알람 설정 및 유통기한 확인 및 푸시 예약
            if (isAlarmOn) {
                setAlarm(calendar.timeInMillis)
                checkExpirationAndNotify(calendar.timeInMillis)
                Toast.makeText(this, "알림이 $formattedTime 에 설정되었습니다.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "알림이 비활성화된 상태입니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //푸시 알림 예약
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
        //절전 모드에서도 정확한 시간에 울리도록 설정
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            timeInMillis,
            pendingIntent
        )
    }
    //유통기한이 오늘까지인 재료 확인 후, 개별 푸시 알림 예약
    private fun checkExpirationAndNotify(alarmTimeInMillis: Long) {
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val todayMillis = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(todayDate)?.time
        //Firebase에서 재료 정보 가져오기
        database.get().addOnSuccessListener { snapshot ->
            var hasValidIngredient = false

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
    //특정 재료에 대해 지정 시간에 알림 예약
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

    // 알림 상태 및 시간 저장
    private fun saveAlarmState() {
        sharedPreferences.edit().apply {
            putInt("hour", alarmHour)
            putInt("minute", alarmMinute)
            putBoolean("isAlarmOn", isAlarmOn)
            apply()
        }
    }

    // 저장된 알림 설정 불러오기
    private fun restoreAlarmState() {
        alarmHour = sharedPreferences.getInt("hour", 12) // 기본값: 12시
        alarmMinute = sharedPreferences.getInt("minute", 0) // 기본값: 0분
        isAlarmOn = sharedPreferences.getBoolean("isAlarmOn", true) // 기본값: ON 상태

        val formattedTime = String.format("%02d:%02d", alarmHour, alarmMinute)
        tvSelectedTime.text = "설정된 시간: $formattedTime"
        switchAlarm.isChecked = isAlarmOn
    }
}

