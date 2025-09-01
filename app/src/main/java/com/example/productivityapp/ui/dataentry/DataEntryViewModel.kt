package com.example.productivityapp.ui.dataentry

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.productivityapp.data.AppDatabase // Import your AppDatabase
import com.example.productivityapp.data.TimelineEntry // Import your Entity
import com.example.productivityapp.data.TimelineRepository // Import your Repository
import com.example.productivityapp.utils.Event
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class DataEntryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TimelineRepository

    private val _text = MutableLiveData<String>().apply {
        value = "Enter your study session details below."
    }
    val text: LiveData<String> = _text

    private val _toastMessage = MutableLiveData<Event<String>>()
    val toastMessage: LiveData<Event<String>> = _toastMessage

    private val _clearFieldsEvent = MutableLiveData<Event<Unit>>()
    val clearFieldsEvent: LiveData<Event<Unit>> = _clearFieldsEvent

    companion object {
        // Regex for YYYY-MM-DD format
        private val DATE_PATTERN = Pattern.compile(
            "^\\d{4}-\\d{2}-\\d{2}$"
        )
    }

    init {
        // Initialize the repository
        val timelineEntryDao = AppDatabase.getDatabase(application).timelineEntryDao()
        repository = TimelineRepository(timelineEntryDao)
    }

    fun saveStudySession(dateStr: String, topicStr: String, durationStr: String) {
        if (dateStr.isBlank() || topicStr.isBlank() || durationStr.isBlank()) {
            _toastMessage.value = Event("Please fill all fields")
            return
        }

        if (!DATE_PATTERN.matcher(dateStr).matches()) {
            _toastMessage.value = Event("Invalid date format. Please use YYYY-MM-DD")
            return
        }

        val durationInt = durationStr.toIntOrNull()
        if (durationInt == null || durationInt <= 0) { // Also check if duration is positive
            _toastMessage.value = Event("Duration must be a valid positive number (minutes)")
            return
        }

        viewModelScope.launch {
            try {
                // Create TimelineEntry with Int duration
                val newEntry = TimelineEntry(date = dateStr, topic = topicStr, duration = durationInt)
                repository.insert(newEntry) // Call repository's insert method
                _toastMessage.value = Event("Data saved successfully!")
                _clearFieldsEvent.value = Event(Unit) // Trigger clearing fields in UI
            } catch (e: Exception) {
                // More specific error handling can be added if needed
                _toastMessage.value = Event("Error saving data: ${e.message}")
                e.printStackTrace() // Log the full error for debugging
            }
        }
    }

    fun deleteAllStudySessions() {
        viewModelScope.launch {
            try {
                repository.deleteAll() // Call repository's deleteAll method
                _toastMessage.value = Event("All data deleted successfully!")
            } catch (e: Exception) {
                _toastMessage.value = Event("Error deleting data: ${e.message}")
                e.printStackTrace() // Log the full error
            }
        }
    }
}

