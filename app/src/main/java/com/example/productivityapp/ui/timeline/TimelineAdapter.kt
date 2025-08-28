package com.example.productivityapp.ui.timeline // Or your chosen package

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.productivityapp.data.TimelineEntry // Import your data class
import com.example.productivityapp.databinding.ItemTimelineEntryBinding // Import the ViewBinding class

class TimelineAdapter : ListAdapter<TimelineEntry, TimelineAdapter.TimelineViewHolder>(TimelineDiffCallback()) {

    // ViewHolder class: Holds references to the views for a single list item
    class TimelineViewHolder(
        private val binding: ItemTimelineEntryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        // Updated bind method to set actual data
        fun bind(entry: TimelineEntry) {
            binding.textViewItemDate.text = entry.date
            binding.textViewItemTopic.text = entry.topic
            binding.textViewItemDuration.text = entry.duration
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimelineViewHolder {
        val binding = ItemTimelineEntryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TimelineViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TimelineViewHolder, position: Int) {
        val currentEntry = getItem(position)
        if (currentEntry != null) { // Good practice to check for null, though ListAdapter usually handles it
            holder.bind(currentEntry)
        }
    }

    class TimelineDiffCallback : DiffUtil.ItemCallback<TimelineEntry>() {
        override fun areItemsTheSame(oldItem: TimelineEntry, newItem: TimelineEntry): Boolean {
            // For now, using referential equality. If you add unique IDs, compare them.
            // A more robust way if date+topic is unique enough for your data:
            // return oldItem.date == newItem.date && oldItem.topic == newItem.topic
            return oldItem === newItem // Keeping it simple as before for now
        }

        override fun areContentsTheSame(oldItem: TimelineEntry, newItem: TimelineEntry): Boolean {
            return oldItem == newItem // Data class equals() compares all properties
        }
    }
}

