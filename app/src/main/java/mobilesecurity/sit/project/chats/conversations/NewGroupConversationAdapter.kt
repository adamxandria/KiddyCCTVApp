package mobilesecurity.sit.project.chats.conversations

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mobilesecurity.sit.project.R

class NewGroupConversationAdapter(private val participants: MutableList<Participant>) :
    RecyclerView.Adapter<NewGroupConversationAdapter.ParticipantViewHolder>() {

    // Provide a reference to the views for each data item
    class ParticipantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var participantNameTextView: TextView = itemView.findViewById(R.id.textViewParticipantName)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipantViewHolder {
        // create a new view
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_new_conversation_participant, parent, false)
        return ParticipantViewHolder(itemView)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ParticipantViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        val participant = participants[position]
        holder.participantNameTextView.text = participant.name
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = participants.size

    // Method to add a participant to the list
//    fun addParticipant(participantName: String) {
//        if (!participants.contains(participantName)) {
//            participants.add(participantName)
//            notifyItemInserted(participants.size - 1)
//        }
//    }
//
//    // Method to remove a participant from the list
//    fun removeParticipant(position: Int) {
//        if (position < participants.size) {
//            participants.removeAt(position)
//            notifyItemRemoved(position)
//        }
//    }
}
