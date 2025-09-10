package com.example.productivityapp.ui.analytics

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.map
import com.example.productivityapp.data.AppDatabase
import com.example.productivityapp.data.TimelineEntry
import com.example.productivityapp.data.TimelineRepository
import com.example.productivityapp.data.TopicAnalyticsItem
import com.example.productivityapp.utils.Event
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TimelineRepository

    // To signal the Fragment to start the file saving process with the generated CSV content
    private val _csvContentToSave = MutableLiveData<Event<String>>()
    val csvContentToSave: LiveData<Event<String>> = _csvContentToSave

    // To provide a message back to the UI (e.g., success or error)
    private val _userMessage = MutableLiveData<Event<String>>()
    val userMessage: LiveData<Event<String>> = _userMessage

    // To indicate loading state
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    val topicAnalyticsList: LiveData<List<TopicAnalyticsItem>>
    val topicAnalyticsWithFormattedHours: LiveData<List<TopicAnalyticsDisplayItem>>


    companion object {
        private const val TAG = "AnalyticsViewModel"
    }

    init {
        val timelineEntryDao = AppDatabase.getDatabase(application).timelineEntryDao()
        repository = TimelineRepository(timelineEntryDao)
        Log.d(TAG, "AnalyticsViewModel initialized with repository.")

        topicAnalyticsList = repository.topicAnalytics
        topicAnalyticsWithFormattedHours = topicAnalyticsList.map { list ->
            list.map { item ->
                TopicAnalyticsDisplayItem(
                    topicName = item.topicName,
                    totalMinutes = item.totalMinutes,
                    formattedTime = formatMinutesToHoursMinutes(item.totalMinutes)
                )
            }
        }
    }

    fun formatMinutesToHoursMinutes(totalMinutes: Int): String {
        if (totalMinutes < 0) return "N/A"
        if (totalMinutes == 0) return "0m"
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return when {
            hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
            hours > 0 -> "${hours}h"
            else -> "${minutes}m"
        }
    }

    fun exportTimelineDataToCsv() {
        if (_isLoading.value == true) {
            _userMessage.value = Event("Export already in progress.")
            return
        }
        _isLoading.value = true
        var entries: List<TimelineEntry> = emptyList()

        viewModelScope.launch {
            try {
                entries = repository.allEntries.first() // Get the current list of entries once

                if (entries.isEmpty()) {
                    _userMessage.value = Event("No data available to export.")
                    return@launch
                }

                val csvData = generateCsvString(entries)
                _csvContentToSave.value = Event(csvData) // Signal Fragment to save this content
                // Success message will be handled by Fragment after successful file save

            } catch (e: Exception) {
                Log.e(TAG, "Error preparing data for CSV export", e)
                _userMessage.value = Event("Error preparing data for export: ${e.message}")
            } finally {
                if (_csvContentToSave.value == null){
                    _isLoading.value = false
                }
            }
        }
    }

    private fun generateCsvString(entries: List<TimelineEntry>): String {
        val stringBuilder = StringBuilder()
        // CSV Header
        stringBuilder.append("Date,Topic,Duration\n") // Define your header

        // CSV Rows
        entries.forEach { entry ->
            // Basic CSV encoding: ensure no commas in your data or handle them (e.g., by quoting)
            // For simplicity, assuming topic does not contain commas or newlines.
            val topic = entry.topic.replace("\"", "\"\"") // Escape double quotes for basic CSV
            stringBuilder.append("${entry.date},\"${topic}\",${entry.duration}\n")
        }
        Log.d(TAG, "Generated CSV String: \n$stringBuilder")
        return stringBuilder.toString()
    }

    // Called by the Fragment after it finishes the save operation
    fun exportFinished(success: Boolean, message: String? = null) {
        _isLoading.value = false
        if (success) {
            _userMessage.value = Event(message ?: "Data exported successfully.")
        } else {
            _userMessage.value = Event(message ?: "Failed to export data.")
        }
    }
}

data class TopicAnalyticsDisplayItem(
    val topicName: String,
    val totalMinutes: Int,
    val formattedTime: String
)
