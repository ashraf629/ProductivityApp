package com.example.productivityapp.ui.analytics

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.productivityapp.databinding.FragmentAnalyticsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope // Scope for file writing, consider viewModelScope or lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.io.IOException

class AnalyticsFragment : Fragment() {

    private var _binding: FragmentAnalyticsBinding? = null
    private val binding get() = _binding!!

    private lateinit var analyticsViewModel: AnalyticsViewModel

    // Activity Result Launcher for file permissions (if needed for older Android versions)
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.i("Permission", "Storage permission granted")
                // Permission is granted. You might want to trigger the export again
                // or inform the user they can now press the button.
                Toast.makeText(context, "Storage permission granted. Please try exporting again.", Toast.LENGTH_LONG).show()
            } else {
                Log.e("Permission", "Storage permission denied")
                Toast.makeText(context, "Storage permission is required to export data.", Toast.LENGTH_LONG).show()
                analyticsViewModel.exportFinished(success = false, message = "Storage permission denied.")
            }
        }

    // Activity Result Launcher for creating a document (SAF - Storage Access Framework)
    // This gives the user control over where to save the file.
    private val createDocumentLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri: Uri? ->
            uri?.let {
                // We got a URI from the user, now write the CSV content to it
                val csvContent = analyticsViewModel.csvContentToSave.value?.peekContent() // Get non-consumed content
                if (csvContent != null) {
                    writeCsvToUri(it, csvContent)
                } else {
                    Toast.makeText(requireContext(), "Error: No CSV data to save.", Toast.LENGTH_SHORT).show()
                    analyticsViewModel.exportFinished(success = false, message = "No CSV data found to save.")
                }
            } ?: run {
                // User cancelled the file selection
                Toast.makeText(requireContext(), "Export cancelled by user.", Toast.LENGTH_SHORT).show()
                analyticsViewModel.exportFinished(success = false, message = "Export cancelled by user.")
            }
        }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        analyticsViewModel = ViewModelProvider(this)[AnalyticsViewModel::class.java]
        _binding = FragmentAnalyticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonExportToCsv.setOnClickListener {
            Log.d("AnalyticsFragment", "Export button clicked")
            // For Android 10+ using SAF, direct permission check might not be needed for ACTION_CREATE_DOCUMENT
            // However, if you were to use MediaStore for Downloads directly without SAF (older approach),
            // you might need it for < Q. Let's proceed with SAF which is generally better.
            analyticsViewModel.exportTimelineDataToCsv()
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        analyticsViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBarExport.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.buttonExportToCsv.isEnabled = !isLoading
            if (isLoading) {
                binding.textViewAnalyticsStatus.text = "Exporting data..."
            }
        }

        analyticsViewModel.userMessage.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { message ->
                binding.textViewAnalyticsStatus.text = message // Update status text view
                Toast.makeText(context, message, Toast.LENGTH_LONG).show() // Also show a toast
            }
        }

        analyticsViewModel.csvContentToSave.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { _ -> // Content is used in createDocumentLauncher callback
                Log.d("AnalyticsFragment", "Received CSV content, launching file creation intent.")
                // Generate a default filename with timestamp
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val fileName = "ProductivityData_$timeStamp.csv"
                createDocumentLauncher.launch(fileName) // Launch SAF to let user pick location and name
            }
        }
    }

    private fun writeCsvToUri(uri: Uri, csvContent: String) {
        // Use a background thread for file I/O
        // GlobalScope is generally discouraged; prefer lifecycleScope or viewModelScope if appropriate,
        // but for a one-off ActivityResult callback, this can be acceptable if handled carefully.
        // A better approach would be to pass this work back to ViewModel with the Uri.
        // For simplicity here in fragment:
        GlobalScope.launch(Dispatchers.IO) {
            try {
                requireContext().contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(csvContent.toByteArray())
                    outputStream.flush()
                    Log.i("AnalyticsFragment", "Successfully wrote CSV to URI: $uri")
                    withContext(Dispatchers.Main) {
                        analyticsViewModel.exportFinished(success = true, message = "Data exported successfully to selected file.")
                    }
                } ?: throw IOException("Failed to get output stream for URI: $uri")
            } catch (e: IOException) {
                Log.e("AnalyticsFragment", "Error writing CSV to URI: $uri", e)
                withContext(Dispatchers.Main) {
                    analyticsViewModel.exportFinished(success = false, message = "Error saving file: ${e.message}")
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
