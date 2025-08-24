package com.example.productivityapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.productivityapp.databinding.FragmentHomeBinding
import com.example.productivityapp.util.Event // Import the Event class

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Observe the text LiveData from the ViewModel
        homeViewModel.text.observe(viewLifecycleOwner) {
            binding.textHome.text = it
        }

        // Observe the toast message LiveData from the ViewModel
        homeViewModel.toastMessage.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        }

        // Observe the clear fields event LiveData from the ViewModel
        homeViewModel.clearFieldsEvent.observe(viewLifecycleOwner) { event ->
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
            homeViewModel.saveStudySession(date, topic, duration)
        }

        // Set OnClickListener for the delete all button
        binding.buttonDeleteAll.setOnClickListener { // Corrected ID here
            homeViewModel.deleteAllStudySessions()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
