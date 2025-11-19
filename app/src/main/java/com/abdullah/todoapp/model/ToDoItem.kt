package com.abdullah.todoapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.*

@Entity(tableName = "todo_items")
@TypeConverters(Converters::class)
data class ToDoItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val priority: String,
    val category: String,
    val status: String = "DEVAM_EDİYOR",
    val completedAt: Date? = null,
    val completionNote: String = "",
    val dueDate: Date? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val notes: String = "",
    val recurrenceType: String = "",
    val recurrenceEndDate: Date? = null,
    val reminderDate: Date? = null,
    val tags: String = "",
    val subtasks: String = "",
    val attachments: String = "",
    val color: Int? = null
)

@Entity(tableName = "tags")
data class Tag(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val color: Int
)

data class Attachment(
    val id: Int,
    val uri: String,
    val type: AttachmentType,
    val name: String,
    val size: Long,
    val createdAt: Date = Date()
)

enum class Priority {
    DÜŞÜK, ORTA, YÜKSEK
}

enum class Category {
    İŞ, KİŞİSEL, ALIŞVERİŞ, SAĞLIK, EĞİTİM, DİĞER
}

enum class AttachmentType {
    IMAGE, DOCUMENT, AUDIO, VIDEO, OTHER
}

