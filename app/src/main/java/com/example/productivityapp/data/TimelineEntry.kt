package com.example.productivityapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_sessions")
data class TimelineEntry(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0, // Auto-generating primary key, 'var' because Room needs to set it

    val date: String,
    val topic: String,
    val duration: Int // Number of minutes
)

