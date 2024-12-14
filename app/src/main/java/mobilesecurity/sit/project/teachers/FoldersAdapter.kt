package mobilesecurity.sit.project.teachers

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mobilesecurity.sit.project.R

class FoldersAdapter(
     private var folders: MutableList<Folder>,
     private val listener: (Folder) -> Unit) :
     RecyclerView.Adapter<FoldersAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val folderIcon: ImageView = view.findViewById(R.id.imageViewFolderIcon)
        val folderName: TextView = view.findViewById(R.id.textViewFolderName)
        val forwardArrow: ImageView = view.findViewById(R.id.imageViewForwardArrow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_picturefolder, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val folder = folders[position]
        holder.folderName.text =
            folder.name // Ensure this matches the property of your Folder data class
        // Set static resources for icon and forward arrow if they don't change
        holder.folderIcon.setImageResource(R.drawable.kidsactivities)
        holder.forwardArrow.setImageResource(R.drawable.forward_arrow)
        holder.itemView.setOnClickListener {
            listener(folder) // Invoke the click listener passed into the adapter
        }
    }

        override fun getItemCount(): Int = folders.size

        fun addFolder(folder: Folder) {
            folders.add(folder)
            notifyItemInserted(folders.size - 1)
        }

        fun updateFolders(newFolders: List<Folder>) {
            Log.d("FoldersAdapter", "Updating folders with IDs: ${newFolders.map { it.id }}")
            folders.clear()
            folders.addAll(newFolders)
            notifyDataSetChanged() // Refresh the entire list
        }

    }

