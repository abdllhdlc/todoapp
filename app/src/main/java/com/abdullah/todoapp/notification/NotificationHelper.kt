package com.abdullah.todoapp.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.abdullah.todoapp.MainActivity
import com.abdullah.todoapp.R
import com.abdullah.todoapp.model.ToDoItem

object NotificationHelper {
    private const val CHANNEL_ID = "reminder_channel"
    private const val CHANNEL_NAME = "Hatırlatıcılar"
    private const val CHANNEL_DESCRIPTION = "Görev hatırlatıcıları için bildirimler"

    fun showNotification(context: Context, todoItem: ToDoItem, notificationId: Int) {
        try {
            Log.d("NotificationHelper", "Creating notification for todo: ${todoItem.title}")
            
            createNotificationChannel(context)
            
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("todoId", todoItem.id)
                putExtra("fromNotification", true)
                addCategory(Intent.CATEGORY_LAUNCHER)
                action = Intent.ACTION_MAIN
            }
            
            val pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(todoItem.title)
                .setContentText("Hatırlatıcı: ${todoItem.notes}")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .build()

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(notificationId, notification)
            
            Log.d("NotificationHelper", "Notification sent successfully")
        } catch (e: Exception) {
            Log.e("NotificationHelper", "Error showing notification", e)
        }
    }

    private fun createNotificationChannel(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = CHANNEL_DESCRIPTION
                    enableVibration(true)
                    enableLights(true)
                }

                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
                Log.d("NotificationHelper", "Notification channel created")
            }
        } catch (e: Exception) {
            Log.e("NotificationHelper", "Error creating notification channel", e)
        }
    }
} 