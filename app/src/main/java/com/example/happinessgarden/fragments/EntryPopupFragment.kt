package com.example.happinessgarden.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.happinessgarden.databinding.FragmentEntryPopupBinding
import com.example.happinessgarden.utils.HappinessData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EntryPopupFragment : DialogFragment() {
    private lateinit var binding: FragmentEntryPopupBinding
    private var happinessData: HappinessData? = null

    enum class EntryMode {
        NEW, VIEW
    }

    internal var listener: OnPopupNextBtnClickListener? = null

    // companion object for creating new instances of the fragment
    companion object {
        const val TAG = "EntryPopupFragment"
        const val MODE_NEW = "new"
        const val MODE_VIEW = "view"

        @JvmStatic
        fun newInstance(happyId: String? = null, gratefulNotes: String? = null, worryNotes: String? = null, dateCreated: String? = null, mode: String) =
            EntryPopupFragment().apply {
                arguments = Bundle().apply {
                    putString("happyId", happyId)
                    putString("gratefulNotes", gratefulNotes)
                    putString("worryNotes", worryNotes)
                    putString("dateCreated", dateCreated)
                    putString("mode", mode)
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEntryPopupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mode = arguments?.getString("mode") ?: MODE_NEW
        val entryMode = if (mode == MODE_NEW) EntryMode.NEW else EntryMode.VIEW

        updateButtonVisibility(entryMode)

        when (entryMode) {
            EntryMode.NEW -> {
                binding.gratefulNotesText.setText("")
                binding.gratefulNotesText.isEnabled = true
                binding.worryNotesText.setText("")
                binding.worryNotesText.isEnabled = true
                val currentDate = SimpleDateFormat("MM.dd.yy", Locale.getDefault()).format(Date())
                binding.dateText.text = currentDate
            }
            EntryMode.VIEW -> {
                happinessData = HappinessData(
                    arguments?.getString("happyId").orEmpty(),
                    arguments?.getString("gratefulNotes").orEmpty(),
                    arguments?.getString("worryNotes").orEmpty(),
                    arguments?.getString("dateCreated").orEmpty()
                )
                binding.gratefulNotesText.setText(happinessData?.gratefulNotes)
                binding.gratefulNotesText.isEnabled = false
                binding.worryNotesText.setText(happinessData?.worryNotes)
                binding.worryNotesText.isEnabled = false
                binding.dateText.text = happinessData?.dateCreated
            }
        }

        binding.addEntryBtn.setOnClickListener {
            // Info about what to do value to add
            val gratefulNotes = binding.gratefulNotesText.text.toString()
            val worryNotes = binding.worryNotesText.text.toString()

            if (gratefulNotes.isNotEmpty() || worryNotes.isNotEmpty()) {
                if (happinessData == null) {
                    listener?.saveEntry(gratefulNotes, worryNotes, binding.gratefulNotesText, binding.worryNotesText)
                }
            } else {
                Toast.makeText(context, "Please enter some information", Toast.LENGTH_SHORT).show()
            }
        }

        binding.editEntryBtn.setOnClickListener {
            if (happinessData != null) {
                // Enable text fields for editing
                binding.gratefulNotesText.isEnabled = true
                binding.worryNotesText.isEnabled = true
                binding.editEntryBtn.text = "Save" // Change button text to "Save"
                binding.editEntryBtn.setOnClickListener {
                    // Save the updated notes
                    val updatedGratefulNotes = binding.gratefulNotesText.text.toString()
                    val updatedWorryNotes = binding.worryNotesText.text.toString()

                    if (updatedGratefulNotes.isNotEmpty() || updatedWorryNotes.isNotEmpty()) {
                        happinessData?.gratefulNotes = updatedGratefulNotes
                        happinessData?.worryNotes = updatedWorryNotes
                        listener?.updateEntry(happinessData!!, binding.gratefulNotesText, binding.worryNotesText) // Update the task
                        dismiss()
                    } else {
                        Toast.makeText(context, "Please enter some information", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.popupCloseBtn.setOnClickListener {
            dismiss()
        }
    }

    private fun updateButtonVisibility(mode: EntryMode) {
        when (mode) {
            EntryMode.NEW -> {
                binding.addEntryBtn.visibility = View.VISIBLE
                binding.editEntryBtn.visibility = View.GONE
            }
            EntryMode.VIEW -> {
                binding.addEntryBtn.visibility = View.GONE
                binding.editEntryBtn.visibility = View.VISIBLE
            }
        }
    }

    interface OnPopupNextBtnClickListener{
        fun saveEntry(gratefulNotes: String, worryNotes: String, gratefulNotesText: EditText, worryNotesText: EditText)
        // give information about the original text in todoData
        fun updateEntry(happinessData: HappinessData, gratefulNotesText: EditText, worryNotesText: EditText)
    }
}
