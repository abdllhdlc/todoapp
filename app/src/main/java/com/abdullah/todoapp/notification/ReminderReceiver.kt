package com.abdullah.todoapp.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.abdullah.todoapp.model.ToDoItem
import com.google.gson.Gson
import com.abdullah.todoapp.model.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ReminderReceiver", "Received broadcast: ${intent.action}")
        
        try {
            val todoItemJson = intent.getStringExtra("todoItem")
            if (todoItemJson != null) {
                val todoItem = Gson().fromJson(todoItemJson, ToDoItem::class.java)
                Log.d("ReminderReceiver", "Showing notification for todo: ${todoItem.title}")
                
                NotificationHelper.showNotification(
                    context = context,
                    todoItem = todoItem,
                    notificationId = todoItem.id.toInt()
                )

                // Auto-clear reminderDate so it disappears from Mevcut Hatırlatıcılar
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = AppDatabase.getDatabase(context)
                        val dao = db.todoDao()
                        dao.update(todoItem.copy(reminderDate = null))
                    } catch (e: Exception) {
                        Log.e("ReminderReceiver", "Error clearing reminderDate", e)
                    }
                }
            } else {
                Log.e("ReminderReceiver", "todoItem extra is null")
            }
        } catch (e: Exception) {
            Log.e("ReminderReceiver", "Error showing notification", e)
        }
    }
} 