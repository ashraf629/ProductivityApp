package com.example.productivityapp.ui.timeline

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
// import androidx.lifecycle.ViewModel

import android.app.Application
import android.os.Environment
import android.util.Log // For logging
import androidx.lifecycle.AndroidViewModel
import com.example.productivityapp.data.TimelineEntry // Ensure this import is correct
import com.example.productivityapp.utils.CsvParser // Ensure this import is correct
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
class TimelineViewModel(application: Application) : AndroidViewModel(application) {

//    private val _text = MutableLiveData<String>().apply {
//        value = "This is timeline Fragment"
//    }
//    val text: LiveData<String> = _text

    private val _timelineEntries = MutableLiveData<List<TimelineEntry>>()
    val timelineEntries: LiveData<List<TimelineEntry>> = _timelineEntries

    // Optional: LiveData for loading state and error messages (we'll use these more in later steps)
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    companion object {
        private const val TAG = "TimelineViewModel" // For Logcat
        private const val CSV_FILE_NAME = "study_sessions.csv" // Consistent with DataEntryViewModel
    }

    fun loadTimelineData() {
        Log.d(TAG, "loadTimelineData called")
        _isLoading.value = true
        _errorMessage.value = null // Clear previous error

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val context = getApplication<Application>().applicationContext
                val documentsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                val targetDir = documentsDir ?: context.filesDir
                val csvFileToRead = File(targetDir, CSV_FILE_NAME)

                Log.d(TAG, "Attempting to read from: ${csvFileToRead.absolutePath}")

                if (csvFileToRead.exists()) {
                    Log.d(TAG, "File exists. Parsing...")
                    val entries = CsvParser.parseCsvFromFile(csvFileToRead)
                    _timelineEntries.postValue(entries) // Post to LiveData

                    // --- Logging the loaded data ---
                    if (entries.isNotEmpty()) {
                        Log.i(TAG, "Successfully parsed ${entries.size} entries:")
                        entries.forEachIndexed { index, entry ->
                            Log.d(TAG, "Entry ${index + 1}: Date=${entry.date}, Topic=${entry.topic}, Duration=${entry.duration}")
                        }
                    } else {
                        Log.i(TAG, "CSV file parsed, but no entries found (file might be empty or only contain header).")
                        _errorMessage.postValue("No study sessions found in the file.")
                    }
                    // --- End of logging ---

                } else {
                    Log.w(TAG, "CSV file does not exist at ${csvFileToRead.absolutePath}")
                    _timelineEntries.postValue(emptyList()) // Post empty list
                    _errorMessage.postValue("Data file not found. Add some entries first.")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error loading timeline data", e)
                _errorMessage.postValue("Error loading data: ${e.message}")
                _timelineEntries.postValue(emptyList()) // Post empty list on error
            } finally {
                _isLoading.postValue(false)
                Log.d(TAG, "loadTimelineData finished")
            }
        }
    }

    // Dummy function to trigger loading for testing (e.g., from Fragment's onViewCreated)
    // In a real scenario, this might be called automatically or based on user action.
    fun onFragmentReady() {
        Log.d(TAG, "onFragmentReady called, initiating data load.")
        loadTimelineData()
    }
}

