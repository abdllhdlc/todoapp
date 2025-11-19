package com.abdullah.todoapp.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.abdullah.todoapp.model.*
import com.abdullah.todoapp.notification.AlarmScheduler
import com.abdullah.todoapp.repository.ToDoRepository
import com.abdullah.todoapp.util.FileUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class ToDoViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ToDoRepository(application)

    private val _sortOrder = MutableStateFlow(SortOrder.CREATED_AT_DESC)
    private val _showCompleted = MutableStateFlow(false)
    private val _completedCount = MutableStateFlow(0)
    private val _pendingCount = MutableStateFlow(0)
    private val _overdueCount = MutableStateFlow(0)

    val allItems: Flow<List<ToDoItem>> = repository.getAllItems()
        // Removed in-memory search/category/priority filters (unused by UI)
        .combine(_showCompleted) { items, showCompleted ->
            if (showCompleted) items
            else items.filter { it.status != "TAMAMLANDI" }
        }
        .combine(_sortOrder) { items, sortOrder ->
            when (sortOrder) {
                SortOrder.CREATED_AT_DESC -> items.sortedByDescending { it.createdAt }
                SortOrder.CREATED_AT_ASC -> items.sortedBy { it.createdAt }
                SortOrder.DUE_DATE_ASC -> items.sortedBy { it.dueDate ?: Date(Long.MAX_VALUE) }
                SortOrder.DUE_DATE_DESC -> items.sortedByDescending { it.dueDate ?: Date(0) }
                SortOrder.PRIORITY_HIGH -> items.sortedWith(compareByDescending<ToDoItem> { 
                    when(it.priority) {
                        Priority.YÜKSEK.name -> 3
                        Priority.ORTA.name -> 2
                        Priority.DÜŞÜK.name -> 1
                        else -> 0
                    }
                })
                SortOrder.PRIORITY_LOW -> items.sortedWith(compareBy<ToDoItem> { 
                    when(it.priority) {
                        Priority.YÜKSEK.name -> 3
                        Priority.ORTA.name -> 2
                        Priority.DÜŞÜK.name -> 1
                        else -> 0
                    }
                })
            }
        }

    val showCompleted: StateFlow<Boolean> = _showCompleted.asStateFlow()
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    val itemsWithReminders: Flow<List<ToDoItem>> = repository.getItemsWithReminders()

    init {
        updateCounts()
    }

    private fun updateCounts() {
        viewModelScope.launch {
            _completedCount.value = repository.getCompletedItemsCount()
            _pendingCount.value = repository.getPendingItemsCount()
            _overdueCount.value = repository.getOverdueItemsCount()
        }
    }

    fun getTodoById(id: Long): Flow<ToDoItem?> = repository.getItemById(id)

    fun addItem(
        title: String,
        description: String,
        priority: String,
        category: String,
        dueDate: Date? = null,
        notes: String = "",
        tags: List<String> = emptyList(),
        subtasks: List<String> = emptyList(),
        attachments: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            val item = ToDoItem(
                title = title,
                description = description,
                priority = priority,
                category = category,
                dueDate = dueDate,
                notes = notes,
                tags = tags.joinToString(","),
                subtasks = subtasks.joinToString(","),
                attachments = attachments.joinToString(","),
                createdAt = Date(),
                updatedAt = Date()
            )
            repository.addItem(item)
            updateCounts()
        }
    }

    fun updateItem(item: ToDoItem) {
        viewModelScope.launch {
            repository.updateItem(item)
            updateCounts()
        }
    }

    fun removeItem(id: Long) {
        viewModelScope.launch {
            repository.removeItem(id)
            updateCounts()
        }
    }

    // Removed setters for unused filters

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    fun toggleShowCompleted() {
        _showCompleted.value = !_showCompleted.value
    }

    fun addAttachment(todoId: Long, uri: Uri) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val file = FileUtils.getFileFromUri(context, uri)
            val attachment = Attachment(
                id = System.currentTimeMillis().toInt(),
                uri = file.absolutePath,
                type = when {
                    file.name.endsWith(".jpg", ignoreCase = true) ||
                    file.name.endsWith(".jpeg", ignoreCase = true) ||
                    file.name.endsWith(".png", ignoreCase = true) -> AttachmentType.IMAGE
                    file.name.endsWith(".mp3", ignoreCase = true) ||
                    file.name.endsWith(".wav", ignoreCase = true) -> AttachmentType.AUDIO
                    file.name.endsWith(".mp4", ignoreCase = true) ||
                    file.name.endsWith(".avi", ignoreCase = true) -> AttachmentType.VIDEO
                    else -> AttachmentType.DOCUMENT
                },
                name = file.name,
                size = file.length()
            )
            val attachmentStr = "${attachment.id}:${attachment.uri}:${attachment.type}:${attachment.name}:${attachment.size}"
            repository.addAttachment(todoId, attachmentStr)
        }
    }

    fun removeAttachments(todoId: Long, attachmentIds: Collection<Int>) {
        viewModelScope.launch {
            repository.removeAttachments(todoId, attachmentIds)
        }
    }

    fun cancelReminder(item: ToDoItem) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            AlarmScheduler(context).cancelReminder(item)
            // clear reminderDate in DB for visibility
            repository.updateItem(item.copy(reminderDate = null, updatedAt = Date()))
        }
    }
}