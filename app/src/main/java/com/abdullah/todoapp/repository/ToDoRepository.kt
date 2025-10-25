package com.abdullah.todoapp.repository

import android.app.Application
import com.abdullah.todoapp.model.*
import com.abdullah.todoapp.model.AppDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.*

class ToDoRepository(application: Application) {
    private val todoDao = AppDatabase.getDatabase(application).todoDao()

    fun getAllItems(): Flow<List<ToDoItem>> = todoDao.getAllItems()
    fun getCompletedItems(): Flow<List<ToDoItem>> = todoDao.getCompletedItems()
    fun getIncompleteItems(): Flow<List<ToDoItem>> = todoDao.getIncompleteItems()

    fun getItemById(id: Long): Flow<ToDoItem?> = todoDao.getItemById(id)

    suspend fun addItem(item: ToDoItem) = todoDao.insert(item)
    suspend fun updateItem(item: ToDoItem) = todoDao.update(item)
    suspend fun removeItem(id: Long) {
        val item = todoDao.getItemById(id).first()
        item?.let { todoDao.delete(it) }
    }

    suspend fun addAttachment(todoId: Long, attachmentStr: String) {
        val item = todoDao.getItemById(todoId).first()
        item?.let {
            val currentAttachments = it.attachments.split(",").filter { it.isNotEmpty() }
            val updatedItem = it.copy(
                attachments = (currentAttachments + attachmentStr).joinToString(","),
                updatedAt = Date()
            )
            todoDao.update(updatedItem)
        }
    }

    suspend fun removeAttachments(todoId: Long, attachmentIds: Collection<Int>) {
        val item = todoDao.getItemById(todoId).first()
        item?.let {
            val idsSet = attachmentIds.toSet()
            val updatedAttachments = it.attachments.split(",")
                .filter { it.isNotEmpty() && it.split(":")[0].toInt() !in idsSet }
            val updatedItem = it.copy(
                attachments = updatedAttachments.joinToString(","),
                updatedAt = Date()
            )
            todoDao.update(updatedItem)
        }
    }

    suspend fun getCompletedItemsCount(): Int = todoDao.getCompletedItemsCount()
    suspend fun getPendingItemsCount(): Int = todoDao.getPendingItemsCount()
    suspend fun getOverdueItemsCount(): Int = todoDao.getOverdueItemsCount(Date())

    fun getItemsWithReminders(): Flow<List<ToDoItem>> = todoDao.getItemsWithReminders()
} 