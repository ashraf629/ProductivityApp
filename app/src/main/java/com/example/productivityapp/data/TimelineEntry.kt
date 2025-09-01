package com.example.productivityapp.data

data class TimelineEntry(
    val date: String,
    val topic: String,
    val duration: String
) {
    val id: String
        get() = "$date-$topic-$duration"
}
