package com.example.productivityapp.ui.analytics // Or your chosen package

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.productivityapp.databinding.ItemTopicAnalyticsBinding // Import view binding class

class TopicAnalyticsAdapter :
    ListAdapter<TopicAnalyticsDisplayItem, TopicAnalyticsAdapter.TopicAnalyticsViewHolder>(TopicAnalyticsDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopicAnalyticsViewHolder {
        val binding = ItemTopicAnalyticsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TopicAnalyticsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TopicAnalyticsViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class TopicAnalyticsViewHolder(private val binding: ItemTopicAnalyticsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TopicAnalyticsDisplayItem) {
            binding.textViewTopicName.text = item.topicName
            binding.textViewTotalMinutes.text = "Total: ${item.totalMinutes} minutes" // Example formatting
            binding.textViewFormattedTime.text = item.formattedTime
        }
    }
}

class TopicAnalyticsDiffCallback : DiffUtil.ItemCallback<TopicAnalyticsDisplayItem>() {
    override fun areItemsTheSame(oldItem: TopicAnalyticsDisplayItem, newItem: TopicAnalyticsDisplayItem): Boolean {
        // Assuming topicName is a unique identifier for a row in this context
        return oldItem.topicName == newItem.topicName
    }

    override fun areContentsTheSame(oldItem: TopicAnalyticsDisplayItem, newItem: TopicAnalyticsDisplayItem): Boolean {
        return oldItem == newItem // Data class implements equals
    }
}