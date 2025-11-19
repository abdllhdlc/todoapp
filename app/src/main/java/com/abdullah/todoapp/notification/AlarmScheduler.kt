package com.abdullah.todoapp.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.abdullah.todoapp.model.ToDoItem
import com.google.gson.Gson
import java.util.Calendar
import java.util.TimeZone

class AlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val gson = Gson()

    fun scheduleReminder(todoItem: ToDoItem, reminderTime: Long) {
        try {
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                action = "com.abdullah.todoapp.REMINDER_ALARM"
                putExtra("todoItem", gson.toJson(todoItem))
                addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                todoItem.id.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Zaman dilimini düzelt
            val calendar = Calendar.getInstance(TimeZone.getDefault()).apply {
                timeInMillis = reminderTime
            }

            Log.d("AlarmScheduler", "Scheduling reminder for todo: ${todoItem.title} at ${calendar.time}")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setAlarmClock(
                        AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent),
                        pendingIntent
                    )
                    Log.d("AlarmScheduler", "Exact alarm scheduled using setAlarmClock")
                } else {
                    Log.e("AlarmScheduler", "Cannot schedule exact alarms")
                }
            } else {
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent),
                    pendingIntent
                )
                Log.d("AlarmScheduler", "Exact alarm scheduled using setAlarmClock")
            }
        } catch (e: Exception) {
            Log.e("AlarmScheduler", "Error scheduling reminder", e)
        }
    }

    fun cancelReminder(todoItem: ToDoItem) {
        try {
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                action = "com.abdullah.todoapp.REMINDER_ALARM"
                addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                todoItem.id.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
            Log.d("AlarmScheduler", "Cancelled reminder for todo: ${todoItem.title}")
        } catch (e: Exception) {
            Log.e("AlarmScheduler", "Error cancelling reminder", e)
        }
    }

    // Removed unused deadline scheduling helper
} 