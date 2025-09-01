package com.example.productivityapp.ui.timeline // Or your chosen package

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.productivityapp.data.TimelineEntry // Import your data class
import com.example.productivityapp.databinding.ItemTimelineEntryBinding // Import the ViewBinding class

class TimelineAdapter(
    private val deleteListener: OnItemDeleteListener
) : ListAdapter<TimelineEntry, TimelineAdapter.TimelineViewHolder>(TimelineDiffCallback()) {

    interface OnItemDeleteListener {
        fun onDeleteClick(entry: TimelineEntry)
    }
    // ViewHolder class: Holds references to the views for a single list item
    class TimelineViewHolder(
        private val binding: ItemTimelineEntryBinding,
        private val listener: OnItemDeleteListener
    ) : RecyclerView.ViewHolder(binding.root) {

        private var currentTimelineEntry: TimelineEntry? = null

        init {
            binding.buttonDeleteItem.setOnClickListener {
                currentTimelineEntry?.let { entry ->
                    listener.onDeleteClick(entry)
                }
            }
        }
        fun bind(entry: TimelineEntry) {
            currentTimelineEntry = entry // Store the current entry for the click listener
            binding.textViewItemDate.text = entry.date
            binding.textViewItemTopic.text = entry.topic
            // Format the duration Int to a String for display
            binding.textViewItemDuration.text = "${entry.duration} minutes"

            // Note: Delete button click listener is set in init block
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimelineViewHolder {
        val binding = ItemTimelineEntryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TimelineViewHolder(binding, deleteListener)
    }

    override fun onBindViewHolder(holder: TimelineViewHolder, position: Int) {
        val currentEntry = getItem(position)
        if (currentEntry != null) { // Good practice to check for null, though ListAdapter usually handles it
            holder.bind(currentEntry)
        }
    }

    class TimelineDiffCallback : DiffUtil.ItemCallback<TimelineEntry>() {
        override fun areItemsTheSame(oldItem: TimelineEntry, newItem: TimelineEntry): Boolean {
            return oldItem.id === newItem.id
        }

        override fun areContentsTheSame(oldItem: TimelineEntry, newItem: TimelineEntry): Boolean {
            return oldItem == newItem // Data class equals() compares all properties
        }
    }
}

