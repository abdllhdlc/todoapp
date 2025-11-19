package com.abdullah.todoapp.model

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface ToDoDao {
    @Query("SELECT * FROM todo_items ORDER BY createdAt DESC")
    fun getAllItems(): Flow<List<ToDoItem>>

    @Query("SELECT * FROM todo_items WHERE status = 'TAMAMLANDI' ORDER BY createdAt DESC")
    fun getCompletedItems(): Flow<List<ToDoItem>>

    @Query("SELECT * FROM todo_items WHERE status != 'TAMAMLANDI' ORDER BY createdAt DESC")
    fun getIncompleteItems(): Flow<List<ToDoItem>>

    @Query("SELECT * FROM todo_items WHERE id = :id")
    fun getItemById(id: Long): Flow<ToDoItem?>

    @Query("SELECT * FROM todo_items WHERE dueDate > :currentDate AND status != 'TAMAMLANDI' ORDER BY dueDate ASC")
    fun getUpcomingReminders(currentDate: Date): Flow<List<ToDoItem>>

    @Query("SELECT DISTINCT category FROM todo_items WHERE category IS NOT NULL AND category != ''")
    fun getCategories(): Flow<List<String>>

    @Query("SELECT DISTINCT priority FROM todo_items WHERE priority IS NOT NULL AND priority != ''")
    fun getPriorities(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM todo_items WHERE status = 'TAMAMLANDI'")
    suspend fun getCompletedItemsCount(): Int

    @Query("SELECT COUNT(*) FROM todo_items WHERE status != 'TAMAMLANDI'")
    suspend fun getPendingItemsCount(): Int

    @Query("SELECT COUNT(*) FROM todo_items WHERE dueDate < :currentDate AND status != 'TAMAMLANDI'")
    suspend fun getOverdueItemsCount(currentDate: Date): Int

    @Query("SELECT * FROM todo_items WHERE reminderDate IS NOT NULL ORDER BY reminderDate ASC")
    fun getItemsWithReminders(): Flow<List<ToDoItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ToDoItem)

    @Update
    suspend fun update(item: ToDoItem)

    @Delete
    suspend fun delete(item: ToDoItem)

} 