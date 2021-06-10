package br.com.awesley.pesagem.utils

import android.R
import android.util.Log

import android.view.LayoutInflater
import android.view.View

import android.view.ViewGroup

import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView


/**
 * Provide views to RecyclerView with data from mDataSet.
 */
class CustomAdapter
/**
 * Initialize the dataset of the Adapter.
 *
 * @param dataSet String[] containing the data to populate views to be used by RecyclerView.
 */(private val mDataSet: Array<String>) :
    RecyclerView.Adapter<CustomAdapter.ViewHolder>() {
    // BEGIN_INCLUDE(recyclerViewSampleViewHolder)
    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val textView: TextView

        init {
            // Define click listener for the ViewHolder's View.
            v.setOnClickListener { Log.d(TAG, "Element $adapterPosition clicked.") }
            textView = v.findViewById(R.id.title)
        }
    }

    // BEGIN_INCLUDE(recyclerViewOnCreateViewHolder)
    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view.
        val v: View = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.simple_list_item_1, viewGroup, false)
        return ViewHolder(v)
    }

    // END_INCLUDE(recyclerViewOnCreateViewHolder)
    // BEGIN_INCLUDE(recyclerViewOnBindViewHolder)
    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        Log.d(TAG, "Element $position set.")

        // Get element from your dataset at this position and replace the contents of the view
        // with that element
        viewHolder.textView.text = mDataSet[position]
    }

    // END_INCLUDE(recyclerViewOnBindViewHolder)
    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return mDataSet.size
    }

    companion object {
        private const val TAG = "CustomAdapter"
    }
    // END_INCLUDE(recyclerViewSampleViewHolder)
}