package com.example.productivityapp.ui.timeline // Or your chosen package

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.productivityapp.databinding.FragmentTimelineBinding // Ensure this is correct
import com.example.productivityapp.data.TimelineEntry

class TimelineFragment : Fragment(), TimelineAdapter.OnItemDeleteListener {

    private var _binding: FragmentTimelineBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private lateinit var timelineViewModel: TimelineViewModel
    private lateinit var timelineAdapter: TimelineAdapter // Declare adapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Initialize ViewModel
        timelineViewModel = ViewModelProvider(this)[TimelineViewModel::class.java]

        _binding = FragmentTimelineBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupRecyclerView() // Call new function to setup RecyclerView

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("TimelineFragment", "onViewCreated called")

        observeViewModel() // Call new function to observe ViewModel LiveData

        // Trigger data loading from ViewModel (using the method we added for testing)
        timelineViewModel.onFragmentReady()
        Log.d("TimelineFragment", "Requested data load from ViewModel")
    }

    private fun setupRecyclerView() {
        timelineAdapter = TimelineAdapter(this) // Initialize the adapter
        binding.recyclerViewTimeline.apply {
            adapter = timelineAdapter
            layoutManager = LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true // <-- ADD THIS LINE
            }
        }
        Log.d("TimelineFragment", "RecyclerView setup complete")
    }

    private fun observeViewModel() {
        timelineViewModel.timelineEntries.observe(viewLifecycleOwner) { entries ->
            Log.d("TimelineFragment", "Observed ${entries.size} entries from ViewModel")
            // Submit the list of entries to the adapter
            timelineAdapter.submitList(entries.toList()) {
                // This optional callback runs after the list diffing and updates are complete.
                // It's a good place to scroll after the list has been updated.
                if (entries.isNotEmpty() && binding.recyclerViewTimeline.layoutManager?.isSmoothScrolling == false) {
                    // Post the scroll to the RecyclerView's message queue to ensure it happens
                    // after the layout pass.
                    binding.recyclerViewTimeline.post {
                        binding.recyclerViewTimeline.smoothScrollToPosition(entries.size - 1)
                        Log.d("TimelineFragment", "Scrolled to position ${entries.size - 1}")
                    }
                }
            }
            if (entries.isNotEmpty()) {
                Log.d("TimelineFragment", "First entry topic (if exists): ${entries[0].topic}")
            } else {
                Log.d("TimelineFragment", "Entries list is empty.")
            }
        }

        timelineViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            Log.d("TimelineFragment", "isLoading state: $isLoading")
            // UI updates for loading state will be in Phase 4
        }

        timelineViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage != null) {
                Log.e("TimelineFragment", "Error message from ViewModel: $errorMessage")
                // UI updates for error messages will be in Phase 4
            } else {
                Log.d("TimelineFragment", "Error message cleared or null")
            }
        }
        Log.d("TimelineFragment", "ViewModel observers setup complete")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Important to prevent memory leaks
        Log.d("TimelineFragment", "onDestroyView called, binding set to null")
    }

    override fun onDeleteClick(entry: TimelineEntry) {
        Log.d("TimelineFragment", "Delete clicked for entry ID: ${entry.id}, Topic: ${entry.topic}")
        // Show a confirmation dialog before deleting (Recommended)
        // For simplicity, directly calling delete here. Add AlertDialog for better UX.
        timelineViewModel.deleteTimelineEntry(entry)
    }
}
