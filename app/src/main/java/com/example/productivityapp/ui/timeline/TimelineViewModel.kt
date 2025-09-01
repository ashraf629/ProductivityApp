package com.example.productivityapp.ui.timeline

import android.app.Application
import android.util.Log // Keep for logging if needed
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData // For converting Flow to LiveData
import androidx.lifecycle.viewModelScope // For potential manual refreshes or actions
import com.example.productivityapp.data.AppDatabase
import com.example.productivityapp.data.TimelineEntry
import com.example.productivityapp.data.TimelineRepository
import kotlinx.coroutines.launch // For manual actions if any

class TimelineViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TimelineRepository
    val timelineEntries: LiveData<List<TimelineEntry>> // This will be directly from the repository

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    companion object {
        private const val TAG = "TimelineViewModel"
    }

    init {
        Log.d(TAG, "Initializing TimelineViewModel")
        val timelineEntryDao = AppDatabase.getDatabase(application).timelineEntryDao()
        repository = TimelineRepository(timelineEntryDao)

        // Observe the Flow from the repository and convert it to LiveData
        // This LiveData will automatically update whenever the data in the Room database changes.
        timelineEntries = repository.allEntries.asLiveData() // Magic!
        Log.d(TAG, "timelineEntries LiveData initialized from repository Flow")

        // Example: You might want to observe the flow here if you need to react to its loading states
        // or initial data for _isLoading or _errorMessage, though often the Fragment can handle this
        // based on the list content.
        viewModelScope.launch {
            repository.allEntries.collect { entries ->
                if (entries.isEmpty()) {
                    // This is just an example. `timelineEntries` will also reflect this.
                    // You might set a specific message if the initial load is empty.
                    // However, relying on the Fragment to check timelineEntries.observe is often cleaner.
                    Log.d(TAG, "Collected empty list from repository.allEntries initially or after update.")
                    // _errorMessage.postValue("No entries found.") // Be careful not to overwrite other errors
                } else {
                    Log.d(TAG, "Collected ${entries.size} entries from repository.allEntries.")
                }
                // Typically, you don't need to explicitly set _isLoading here based on Flow collection
                // unless you have very specific loading state needs not covered by UI checks for empty list.
            }
        }
    }

    /**
     * This function can be used to trigger a manual refresh or other actions.
     * With Room's Flow to LiveData, data refreshes automatically.
     * So, this function's role might change (e.g., just for logging or explicit loading state control).
     */
    fun onFragmentReady() {
        Log.d(TAG, "onFragmentReady called. Data should be loading/flowing automatically.")
        // You could set _isLoading true here if you want to show a spinner
        // until the first emission from the Flow populates the list.
        // However, the Fragment observing timelineEntries will get the data when it's ready.
        // _isLoading.value = true // If you want to manually manage a global loading spinner
        // No need to call a separate loadTimelineData() if timelineEntries is already observing the Flow.
    }

    // You can add other methods here if needed, e.g., for filtering, specific queries via repository, etc.

    /**
     * Deletes a specific timeline entry from the database.
     * This will be called from the Fragment when the delete action is triggered.
     *
     * @param entry The TimelineEntry to delete.
     */
    fun deleteTimelineEntry(entry: TimelineEntry) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Attempting to delete entry with ID: ${entry.id}")
                repository.delete(entry)
                Log.i(TAG, "Successfully requested deletion for entry ID: ${entry.id}")
                // Optionally, post a success message (e.g., for a Toast or SnackBar)
                // _userMessage.value = Event("Entry deleted") // Requires Event wrapper for one-time messages
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting entry ID: ${entry.id}", e)
                _errorMessage.value = "Error deleting entry: ${e.message}"
            }
        }
    }

}
