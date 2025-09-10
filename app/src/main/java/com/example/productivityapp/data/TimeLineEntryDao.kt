package com.example.productivityapp.data

import androidx.lifecycle.LiveData // Can be used, but Flow is often preferred for new projects
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow // For reactive updates from Room

@Dao
interface TimelineEntryDao {

    /**
     * Inserts a new study session entry into the database. If an entry with the same
     * primary key already exists, it will be replaced.
     *
     * @param entry The TimelineEntry to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: TimelineEntry)

    /**
     * Retrieves all study session entries from the database, ordered by date (descending)
     * and then by ID (descending) to show newest entries first.
     *
     * @return A Flow that emits a list of TimelineEntry objects.
     */
    @Query("SELECT * FROM study_sessions ORDER BY date DESC, id DESC")
    fun getAllEntries(): Flow<List<TimelineEntry>>

    /**
     * Retrieves a specific study session entry by its ID.
     *
     * @param entryId The ID of the entry to retrieve.
     * @return The TimelineEntry if found, or null otherwise.
     */
    @Query("SELECT * FROM study_sessions WHERE id = :entryId")
    suspend fun getEntryById(entryId: Long): TimelineEntry?

    /**
     * Deletes a specific study session entry from the database.
     * Room identifies the entry to delete based on its primary key.
     *
     * @param entry The TimelineEntry to delete.
     */
    @Delete
    suspend fun deleteEntry(entry: TimelineEntry)

    /**
     * Updates an existing study session entry in the database.
     * Room identifies the entry to update based on its primary key.
     *
     * @param entry The TimelineEntry to update.
     */
    @Update
    suspend fun updateEntry(entry: TimelineEntry)

    /**
     * Deletes all entries from the study_sessions table.
     */
    @Query("DELETE FROM study_sessions")
    suspend fun deleteAllEntries()

    @Query("SELECT DISTINCT topic FROM study_sessions ORDER BY topic ASC")
    fun getAllDistinctTopics(): LiveData<List<String>>

}

