package com.example.productivityapp.ui.dataentry

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat         // <-- Import
import androidx.core.view.WindowInsetsCompat // <-- Import
import androidx.core.view.updatePadding      // <-- Import
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.productivityapp.databinding.FragmentDataentryBinding
import kotlin.math.max // <-- Import for maxOf if not already implicitly available

class DataEntryFragment : Fragment() {

    private var _binding: FragmentDataentryBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private lateinit var dataEntryViewModel: DataEntryViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dataEntryViewModel =
            ViewModelProvider(this).get(DataEntryViewModel::class.java)

        _binding = FragmentDataentryBinding.inflate(inflater, container, false)
        // We'll return binding.root here, and apply insets in onViewCreated
        return binding.root
    }

    // Add onViewCreated if it doesn't exist, or modify it if it does
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Apply insets to the root view of DataEntryFragment
        // This is binding.root because FragmentDataentryBinding's root is the ConstraintLayout
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime()) // For keyboard insets

            // Apply padding to the root layout to avoid system bars and keyboard
            v.updatePadding(
                left = systemBars.left,
                top = systemBars.top,
                right = systemBars.right,
                // Use the larger of system bar bottom or IME bottom for padding.
                // This ensures content shifts correctly when the keyboard appears.
                bottom = max(systemBars.bottom, ime.bottom)
            )

            // Return the insets to allow them to be propagated to child views if necessary,
            // though for a root view handling like this, often CONSUMED is also an option
            // if you are sure no children need to react to these specific insets further.
            // For general safety and to match BottomNavigationView behavior, returning insets is good.
            insets
        }

        // --- Your existing logic from onCreateView that manipulates views should ideally be here ---
        // --- or called from here if it depends on the views being fully created and laid out. ---

        // Observe the text LiveData from the ViewModel
        dataEntryViewModel.text.observe(viewLifecycleOwner) {
            binding.textHome.text = it
        }

        // Observe the toast message LiveData from the ViewModel
        dataEntryViewModel.toastMessage.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        }

        // Observe the clear fields event LiveData from the ViewModel
        dataEntryViewModel.clearFieldsEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                binding.editTextDate.text.clear()
                binding.editTextTopic.text.clear()
                binding.editTextDuration.text.clear()
            }
        }

        // Set OnClickListener for the save button
        binding.buttonSave.setOnClickListener {
            val date = binding.editTextDate.text.toString().trim()
            val topic = binding.editTextTopic.text.toString().trim()
            val duration = binding.editTextDuration.text.toString().trim()
            dataEntryViewModel.saveStudySession(date, topic, duration)
        }

        // Set OnClickListener for the delete all button
        binding.buttonDeleteAll.setOnClickListener {
            dataEntryViewModel.deleteAllStudySessions()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
