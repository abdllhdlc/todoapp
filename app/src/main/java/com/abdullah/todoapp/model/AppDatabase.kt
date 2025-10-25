package com.abdullah.todoapp.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [ToDoItem::class, Tag::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun todoDao(): ToDoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Geçici tablo oluştur
                db.execSQL("""
                    CREATE TABLE todo_items_temp (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        priority TEXT NOT NULL,
                        category TEXT NOT NULL,
                        status TEXT NOT NULL,
                        completedAt INTEGER,
                        completionNote TEXT NOT NULL,
                        dueDate INTEGER,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        notes TEXT NOT NULL,
                        recurrenceType TEXT NOT NULL,
                        recurrenceEndDate INTEGER,
                        reminderDate INTEGER,
                        tags TEXT NOT NULL,
                        subtasks TEXT NOT NULL,
                        attachments TEXT NOT NULL,
                        color INTEGER
                    )
                """)

                // Eski verileri yeni tabloya kopyala
                db.execSQL("""
                    INSERT INTO todo_items_temp (
                        id, title, description, priority, category, status,
                        completedAt, completionNote, dueDate, createdAt, updatedAt,
                        notes, recurrenceType, recurrenceEndDate, reminderDate,
                        tags, subtasks, attachments, color
                    )
                    SELECT 
                        id, title, description, priority, category,
                        CASE WHEN isCompleted = 1 THEN 'TAMAMLANDI' ELSE 'DEVAM_EDİYOR' END,
                        NULL, '', dueDate, createdAt, updatedAt,
                        notes, recurrenceType, recurrenceEndDate, reminderDate,
                        tags, subtasks, attachments, color
                    FROM todo_items
                """)

                // Eski tabloyu sil
                db.execSQL("DROP TABLE todo_items")

                // Yeni tabloyu yeniden adlandır
                db.execSQL("ALTER TABLE todo_items_temp RENAME TO todo_items")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "todo_database"
                )
                .addMigrations(MIGRATION_4_5)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}