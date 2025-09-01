package com.example.productivityapp.ui.timeline

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.productivityapp.databinding.FragmentTimelineBinding
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

class TimelineFragment : Fragment() {

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

        setupRecyclerView() // Call new function to setup RecyclerView

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("TimelineFragment", "onViewCreated called")

        // Apply insets to the RecyclerView
        ViewCompat.setOnApplyWindowInsetsListener(binding.recyclerViewTimeline) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime()) // For keyboard

            // RecyclerView needs padding at the top for status bar,
            // and bottom for navigation bar/IME.
            v.updatePadding(
                // left = systemBars.left, // Usually not needed for vertical lists
                top = systemBars.top,
                // right = systemBars.right,
                // Choose the larger of systemBars.bottom or ime.bottom
                // This handles the case where the keyboard might appear over the nav bar
                bottom = maxOf(systemBars.bottom, ime.bottom)
            )
            // Do NOT consume all insets if other views in the hierarchy might need them,
            // but for a RecyclerView that's the main scrolling content, this is often fine.
            // However, to be safer and allow BottomNavigationView to also get insets if needed:
            insets // Return the insets to allow propagation
        }

        observeViewModel() // Call new function to observe ViewModel LiveData

        // Trigger data loading from ViewModel (using the method we added for testing)
        timelineViewModel.onFragmentReady()
        Log.d("TimelineFragment", "Requested data load from ViewModel")
    }

    private fun setupRecyclerView() {
        timelineAdapter = TimelineAdapter() // Initialize the adapter
        binding.recyclerViewTimeline.apply {
            adapter = timelineAdapter
            layoutManager = LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true
            }
            clipToPadding = false
        }
        Log.d("TimelineFragment", "RecyclerView setup complete")
    }

    private fun observeViewModel() {
        timelineViewModel.timelineEntries.observe(viewLifecycleOwner) { entries ->
            Log.d("TimelineFragment", "Observed ${entries.size} entries from ViewModel")
            // Submit the list of entries to the adapter
            timelineAdapter.submitList(entries) {
                // This optional callback runs after the list diffing and updates are complete.
                // It's a good place to scroll after the list has been updated.
                Log.d("TimelineFragment", "submitList callback: entries.size = ${entries.size}")
                if (entries.isNotEmpty()) {
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
}
