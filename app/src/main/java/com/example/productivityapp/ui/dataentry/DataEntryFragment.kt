package com.example.productivityapp.ui.dataentry

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.productivityapp.databinding.FragmentDataentryBinding
// Added imports for date formatting
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DataEntryFragment : Fragment() {

    private var _binding: FragmentDataentryBinding? = null
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
        val root: View = binding.root

        // *** It's better to move UI setup that depends on the view (binding) to onViewCreated ***

        return root
    }

    // *** New method: onViewCreated for view-dependent setup ***
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set today's date as default for the date EditText
        setDefaultDate() // Call the new function

        // Observe the text LiveData from the ViewModel (for the status TextView)
        dataEntryViewModel.text.observe(viewLifecycleOwner) {
            binding.textHome.text = it // text_home is your status TextView
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
                // binding.editTextDate.text.clear() // No need to clear, just reset to today
                binding.editTextTopic.text.clear()
                binding.editTextDuration.text.clear()
                setDefaultDate() // Reset to today's date after clearing other fields
            }
        }

        // Set OnClickListener for the save button
        binding.buttonSave.setOnClickListener {
            val date = binding.editTextDate.text.toString().trim()
            val topic = binding.editTextTopic.text.toString().trim()
            val duration = binding.editTextDuration.text.toString().trim()
            dataEntryViewModel.saveStudySession(date, topic, duration)
        }

        // Set OnClickListener for the delete all button (if you re-enable it)
        // val deleteButton = view.findViewById<Button>(R.id.button_delete_all) // Example if not using binding directly for commented out views
        // deleteButton?.setOnClickListener {
        // dataEntryViewModel.deleteAllStudySessions()
        // }
    }

    // *** New function to set the default date ***
    private fun setDefaultDate() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayDateString = dateFormat.format(calendar.time)
        binding.editTextDate.setText(todayDateString)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
