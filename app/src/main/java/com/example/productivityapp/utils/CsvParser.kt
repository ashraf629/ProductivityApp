package com.example.productivityapp.utils

import com.example.productivityapp.data.TimelineEntry
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

object CsvParser {

    fun parseCsvFromFile(csvFile: File): List<TimelineEntry> {
        val entries = mutableListOf<TimelineEntry>()

        if (!csvFile.exists() || !csvFile.canRead()) {
            // If the file doesn't exist or can't be read, return an empty list.
            // The ViewModel will handle informing the user.
            return entries
        }

        try {
            FileInputStream(csvFile).use { fis ->
                BufferedReader(InputStreamReader(fis)).use { reader ->
                    // 1. Read and discard the header line
                    reader.readLine() // Assuming the first line is always the header

                    // 2. Read subsequent lines for data
                    var line: String?
                    while (reader.readLine().also { currentLine -> line = currentLine } != null) {
                        val tokens = line?.split(",") // Assuming comma-separated
                        if (tokens != null && tokens.size >= 3) {
                            // Basic validation for non-empty tokens
                            val date = tokens[0].trim()
                            val topic = tokens[1].trim()
                            val duration = tokens[2].trim()

                            if (date.isNotEmpty() && topic.isNotEmpty() && duration.isNotEmpty()) {
                                entries.add(TimelineEntry(date, topic, duration))
                            } else {
                                // Optionally log a warning for malformed lines
                                println("CsvParser: Skipped malformed line: $line")
                            }
                        } else {
                            // Optionally log a warning for lines with insufficient tokens
                            println("CsvParser: Skipped line with insufficient tokens: $line")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Log the exception. The ViewModel can decide how to inform the user.
            e.printStackTrace()
            // Optionally, clear entries if a partial read is not desired on error
            // entries.clear()
        }
        return entries
    }
}
