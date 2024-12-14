package mobilesecurity.sit.project.chats.conversations

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mobilesecurity.sit.project.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ConversationAdapter(
    var conversations: MutableList<Conversation>,
    private val listener: (String) -> Unit,
    private val onDeleteConversation: (String) -> Unit
) : RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_conversation, parent, false)
        return ConversationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        val conversation = conversations[position]
        holder.bind(conversation , listener,  onDeleteConversation)
    }

    override fun getItemCount(): Int = conversations.size

    fun updateConversations(newConversations: List<Conversation>) {
        conversations.clear()
        conversations.addAll(newConversations)
        notifyDataSetChanged()
    }

    class ConversationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.textViewChatName)
        private val lastMessageTextView: TextView = itemView.findViewById(R.id.textViewLastMessage)
        private val timeSentTextView: TextView = itemView.findViewById(R.id.textViewTimeSent)
        private val deleteButton: Button = itemView.findViewById(R.id.deleteConversationButton) // Reference to the delete button

        fun bind(conversation: Conversation, listener: (String) -> Unit, onDeleteConversation: (String) -> Unit)  {
            val participantNames = conversation.participantNames
            val displayName = when {
                !conversation.groupName.isNullOrEmpty() -> conversation.groupName
                !participantNames.isNullOrEmpty() -> participantNames.joinToString(", ")
                else -> "Unknown"
            }

            deleteButton.setOnClickListener {
                onDeleteConversation(conversation.chatId ?: "")
            }

            nameTextView.text = displayName

            // Adjusting how the last message and timestamp are displayed
            if (conversation.lastMessage.isNullOrEmpty()) {
                lastMessageTextView.text = "Currently no message"
                timeSentTextView.text = "" // Don't show any time if there is no message
            } else {
                lastMessageTextView.text = conversation.lastMessage
                timeSentTextView.text = formatTime(conversation.lastMessageTS)
            }

            Log.d("ConversationAdapter", "Display name: $displayName")
            itemView.setOnClickListener { listener(conversation.chatId?:"") }
        }

        private fun formatTime(timestamp: Date?): String {
            return timestamp?.let {
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(it)
            } ?: "Unknown Time"
        }
    }



}
