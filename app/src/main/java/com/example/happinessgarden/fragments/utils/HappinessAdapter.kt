package com.example.happinessgarden.fragments.utils//package com.example.happinessgarden.utils
//
//// class of todo data clas
//// to show in our recycler view
//
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import androidx.recyclerview.widget.RecyclerView
//import com.example.happinessgarden.databinding.IndivTodoItemBinding
//import com.example.happinessgarden.utils.HappinessData
//import android.view.View
//import android.widget.Button
//import com.example.happinessgarden.R
//import android.content.Context
//
//class HappinessAdapter(private val list: MutableList<HappinessData>) : RecyclerView.Adapter<HappinessAdapter.ButtonViewHolder>() {
//    private val TAG = "HappinessAdapter"
//
//    class ButtonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        val button: Button = view.findViewById(R.id.individualButton) // Use your button ID
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ButtonViewHolder {
//        val view = LayoutInflater.from(context).inflate(R.layout.indiv_todo_item, parent, false)
//        return ButtonViewHolder(view)
//    }
//
////    override fun onBindViewHolder(holder: ButtonViewHolder, position: Int) {
////        val todoData = list[position]
////
////        // Log task information
////        Log.d(TAG, "onBindViewHolder: $todoData")
////
////        // Set button text based on the ID
////        holder.button.text = if (todoData.id <= 6) {
////            "🌸" // Flower emoji for buttons with matching IDs
////        } else {
////            "Task ${todoData.id}" // Default task text for other IDs
////        }
////
////        // Set click listener for the button
////        holder.button.setOnClickListener {
////            // Handle button click, e.g., edit the task or show details
////        }
////    }
//
//    override fun getItemCount(): Int {
//        return list.size
//    }
//}