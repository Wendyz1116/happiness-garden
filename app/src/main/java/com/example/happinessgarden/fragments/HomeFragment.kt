package com.example.happinessgarden.fragments

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.happinessgarden.R
import com.example.happinessgarden.databinding.FragmentHomeBinding
import com.example.happinessgarden.utils.HappinessData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment(), EntryPopupFragment.OnPopupNextBtnClickListener{

    private lateinit var mAuth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private lateinit var navController: NavController
    private lateinit var binding: FragmentHomeBinding
    private var popupFragment: EntryPopupFragment? = null
    private lateinit var entryList: MutableList<HappinessData>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)

        binding.signOutButton.setOnClickListener {
            signOutUser()
        }

        // Logic for previous and next btns
        binding.prevBtn.setOnClickListener {
            if (currentPage > 0) {
                currentPage--
                populateGrid()
            }
        }

        binding.nextBtn.setOnClickListener {
            if ((currentPage + 1) * itemsPerPage < entryList.size) {
                currentPage++
                populateGrid()
            }
        }

        // getting data from firebase
        getDataFromFirebase()
        registerEvents()
    }

    // Show the add entry popup when the addBtn is clicked
    private fun registerEvents(){
        // When the add btn is clicked, the popupFragment = AddTodoFragment item will show
        // but also want to send some data to popupFragment
        binding.addEntryBtn.setOnClickListener {
            // If popup fragment already exists, remove it to prevent creating multiple instances
            if (popupFragment != null)
                childFragmentManager.beginTransaction().remove(popupFragment!!).commit()

            // Create new instance in `new` mode
            popupFragment = EntryPopupFragment.newInstance(
                happyId = null,
                gratefulNotes = null,
                worryNotes = null,
                mode = EntryPopupFragment.MODE_NEW
            )
            popupFragment!!.listener = this
            popupFragment!!.show(
                childFragmentManager,
                EntryPopupFragment.TAG
            )
        }
    }

    private fun init(view: View) {
        navController = Navigation.findNavController(view)
        mAuth = FirebaseAuth.getInstance()
        // Create a task child that comes after the reference
        // under the tasks, we'll have the authentication id
        databaseRef = FirebaseDatabase.getInstance().reference
            .child("JournalEntries").child(mAuth.currentUser?.uid.toString())

        entryList = mutableListOf()
        binding.buttonGrid.removeAllViews() // Clear garden

    }


    override fun saveEntry(gratefulNotes: String, worryNotes: String, gratefulNotesText: EditText, worryNotesText: EditText) {

        // Get current date and time
        val currentDate = SimpleDateFormat("MM.dd.yy", Locale.getDefault()).format(Date())

        // Create a map including date
        val happinessEntry = mapOf(
            "gratefulNotes" to gratefulNotes,
            "worryNotes" to worryNotes,
            "dateCreated" to currentDate
        )

        databaseRef.push().setValue(happinessEntry).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "Entry added!", Toast.LENGTH_SHORT).show()
                gratefulNotesText.text = null
                worryNotesText.text = null
                getDataFromFirebase()
            } else {
                Toast.makeText(context, task.exception?.message, Toast.LENGTH_SHORT).show()
            }
            popupFragment?.dismiss()
        }
    }

    override fun updateEntry(happinessData: HappinessData, gratefulNotesText: EditText, worryNotesText: EditText) {
        val gratefulNotes = mapOf(
            "gratefulNotes" to happinessData.gratefulNotes,
            "worryNotes" to happinessData.worryNotes
        )

        // Update the entry in Firebase using the ID
        databaseRef.child(happinessData.happyId).updateChildren(gratefulNotes).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(context, "Entry updated", Toast.LENGTH_SHORT).show()
                gratefulNotesText.text = null
                worryNotesText.text = null
                getDataFromFirebase() // Refresh data to show updated entries
            } else {
                Toast.makeText(context, it.exception?.message, Toast.LENGTH_SHORT).show()
            }
            popupFragment?.dismiss()
        }
    }

    // fetch data from firebase
    private fun getDataFromFirebase(){
        databaseRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                entryList.clear()
                for (taskSnapshot in snapshot.children) {
                    val happyId = taskSnapshot.key ?: continue
                    val gratefulNotes = taskSnapshot.child("gratefulNotes").value.toString()
                    val worryNotes = taskSnapshot.child("worryNotes").value.toString()
                    val dateCreated = taskSnapshot.child("dateCreated").value.toString()
                    entryList.add(HappinessData(happyId, gratefulNotes, worryNotes, dateCreated))
                }
                populateGrid()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }

        })
    }

    fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    private var currentPage = 0
    private val itemsPerPage = 15

    private fun populateGrid() {
        // Clear existing flowers
        binding.buttonGrid.removeAllViews()

        // Determine the start and end indices for the current page
        val startIndex = currentPage * itemsPerPage
        val endIndex = minOf(startIndex + itemsPerPage, entryList.size)

        // add flowers for each entry on the current page
        for (index in startIndex until endIndex) {
            val entryData = entryList[index]
            val button = Button(context).apply {
                text = "🌻"
                background = null
                textSize = 38f

                layoutParams = GridLayout.LayoutParams().apply {
                    gravity = Gravity.CENTER

                    val widthInPx = dpToPx(70)
                    width = widthInPx
                    // width = ViewGroup.LayoutParams.WRAP_CONTENT
                    val heightInPx = dpToPx(70)
                    height = heightInPx
                    columnSpec = GridLayout.spec((index - startIndex) % 5)
                    rowSpec = GridLayout.spec((index - startIndex) / 5)
                    setMargins(0, -3, 0, 0)

                }
                setOnClickListener {
                    // Open popup in view mode when click on a flower
                    val currentPopupFragment = popupFragment // Store old popup in a local variable
                    if (currentPopupFragment != null) {
                        childFragmentManager.beginTransaction().remove(currentPopupFragment).commit()
                    }

                    // Create a new instance of EntryPopupFragment in view mode
                    popupFragment = EntryPopupFragment.newInstance(
                        dateCreated = entryData.dateCreated,
                        happyId = entryData.happyId,
                        gratefulNotes = entryData.gratefulNotes,
                        worryNotes = entryData.worryNotes,
                        mode = EntryPopupFragment.MODE_VIEW
                    )
                    popupFragment!!.listener = this@HomeFragment
                    popupFragment!!.show(childFragmentManager, EntryPopupFragment.TAG)
                }
            }
            binding.buttonGrid.addView(button)
        }

        // Show the prev and next btn depending on the current page
        binding.prevBtn.visibility = if (currentPage > 0) View.VISIBLE else View.GONE
        binding.nextBtn.visibility = if (endIndex < entryList.size) View.VISIBLE else View.GONE
    }

    private fun signOutUser() {
        // Sign out from Firebase Auth
        mAuth.signOut()

        // navigate to SignInFragment
        Toast.makeText(context, "Signed out successfully", Toast.LENGTH_SHORT).show()
        navController.navigate(R.id.action_homeFragment_to_signInFragment)
    }
}