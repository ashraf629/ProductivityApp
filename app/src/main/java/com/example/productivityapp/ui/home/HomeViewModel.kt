package com.example.productivityapp.ui.home

import android.app.Application
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.productivityapp.util.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.regex.Pattern

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val _text = MutableLiveData<String>().apply {
        value = "Enter your study session details below."
    }
    val text: LiveData<String> = _text

    private val _toastMessage = MutableLiveData<Event<String>>()
    val toastMessage: LiveData<Event<String>> = _toastMessage

    private val _clearFieldsEvent = MutableLiveData<Event<Unit>>()
    val clearFieldsEvent: LiveData<Event<Unit>> = _clearFieldsEvent

    companion object {
        private const val CSV_FILE_NAME = "study_sessions.csv"
        private const val CSV_HEADER = "Date,Topic,Duration\n"
        // Regex for YYYY-MM-DD format
        private val DATE_PATTERN = Pattern.compile(
            "^\\d{4}-\\d{2}-\\d{2}$"
        )
    }

    fun saveStudySession(date: String, topic: String, duration: String) {
        if (date.isBlank() || topic.isBlank() || duration.isBlank()) {
            _toastMessage.value = Event("Please fill all fields")
            return
        }

        if (!DATE_PATTERN.matcher(date).matches()) {
            _toastMessage.value = Event("Invalid date format. Please use YYYY-MM-DD")
            return
        }

        viewModelScope.launch {
            val entry = "$date,$topic,$duration\n"
            try {
                val context = getApplication<Application>().applicationContext
                val documentsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                val targetDir = documentsDir ?: context.filesDir

                if (!targetDir.exists()) {
                    targetDir.mkdirs()
                }

                val file = File(targetDir, CSV_FILE_NAME)
                val fileExists = file.exists()

                withContext(Dispatchers.IO) {
                    FileOutputStream(file, true).use { fos ->
                        if (!fileExists || file.length() == 0L) {
                            fos.write(CSV_HEADER.toByteArray())
                        }
                        fos.write(entry.toByteArray())
                    }
                }
                _toastMessage.value = Event("Data saved to ${file.absolutePath}")
                _clearFieldsEvent.value = Event(Unit)

            } catch (e: IOException) {
                e.printStackTrace()
                _toastMessage.value = Event("Error saving data: ${e.message}")
            }
        }
    }

    fun deleteAllStudySessions() {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>().applicationContext
                val documentsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                val targetDir = documentsDir ?: context.filesDir

                if (!targetDir.exists()) {
                    targetDir.mkdirs()
                }

                val file = File(targetDir, CSV_FILE_NAME)

                withContext(Dispatchers.IO) {
                    // Open in write mode (false for append) to overwrite the file
                    FileOutputStream(file, false).use { fos ->
                        fos.write(CSV_HEADER.toByteArray())
                    }
                }
                _toastMessage.value = Event("All data deleted. File reset to header.")

            } catch (e: IOException) {
                e.printStackTrace()
                _toastMessage.value = Event("Error deleting data: ${e.message}")
            }
        }
    }
}
