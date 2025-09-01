package com.example.productivityapp.data

// No need to import LiveData here if Flow is the primary type exposed
import kotlinx.coroutines.flow.Flow

/**
 * Repository module for handling data operations.
 * This class abstracts the data source (Room Database) from the ViewModels.
 * It provides a clean API for data access to the rest of the application.
 */
class TimelineRepository(private val timelineEntryDao: TimelineEntryDao) {

    /**
     * Retrieves all timeline entries as a Flow.
     * The Flow will automatically update when the data in the database changes.
     */
    val allEntries: Flow<List<TimelineEntry>> = timelineEntryDao.getAllEntries()

    /**
     * Inserts a new timeline entry into the database.
     * This is a suspend function and should be called from a coroutine.
     *
     * @param entry The TimelineEntry to insert.
     */
    suspend fun insert(entry: TimelineEntry) {
        timelineEntryDao.insertEntry(entry)
    }

    /**
     * Deletes a timeline entry from the database.
     * This is a suspend function and should be called from a coroutine.
     *
     * @param entry The TimelineEntry to delete.
     */
    suspend fun delete(entry: TimelineEntry) {
        timelineEntryDao.deleteEntry(entry)
    }

    /**
     * Deletes all timeline entries from the database.
     * This is a suspend function and should be called from a coroutine.
     */
    suspend fun deleteAll() {
        timelineEntryDao.deleteAllEntries()
    }

    /**
     * Retrieves a specific timeline entry by its ID.
     * This is a suspend function and should be called from a coroutine.
     *
     * @param entryId The ID of the entry to retrieve.
     * @return The TimelineEntry if found, or null otherwise.
     */
    suspend fun getEntryById(entryId: Long): TimelineEntry? {
        return timelineEntryDao.getEntryById(entryId)
    }

    /**
     * Updates an existing timeline entry in the database.
     * This is a suspend function and should be called from a coroutine.
     *
     * @param entry The TimelineEntry to update.
     */
    suspend fun update(entry: TimelineEntry) {
        timelineEntryDao.updateEntry(entry)
    }
}


