package mobilesecurity.sit.project.chats.messages

import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import mobilesecurity.sit.project.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.firebase.Timestamp

class MessageAdapter(
    private var messages: List<Message> = listOf()
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val viewTypeIncoming = 1
    private val viewTypeOutgoing = 2

    // Determine the view type of each item
    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        return if (message.senderId == currentUserId) viewTypeOutgoing else viewTypeIncoming
    }

    // Create ViewHolder based on the item view type
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            viewTypeOutgoing -> OutgoingMessageViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_outgoing, parent, false))
            else -> IncomingMessageViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_incoming, parent, false))
        }
    }

    // Bind message data to the ViewHolder
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        if (holder is OutgoingMessageViewHolder) {
            holder.bind(message)
        } else if (holder is IncomingMessageViewHolder) {
            holder.bind(message)
        }
    }

    // Get the count of messages
    override fun getItemCount(): Int = messages.size

    // Update the list of messages and notify the adapter
    fun updateMessages(newMessages: List<Message>) {
        this.messages = newMessages
        notifyDataSetChanged()
    }

    // ViewHolder for outgoing messages
    class OutgoingMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.textViewMessageOutgoing)
        private val timestampTextView: TextView = itemView.findViewById(R.id.textViewTimestampOutgoing)

        fun bind(message: Message) {
            messageTextView.text = message.message
            timestampTextView.text = formatTime(message.timestamp)

            // Make URLs within the message text clickable
            Linkify.addLinks(messageTextView, Linkify.WEB_URLS)
            messageTextView.movementMethod = LinkMovementMethod.getInstance()
        }

        private fun formatTime(timestamp: Date?): String {
            // Format the Date object to only show time in HH:MM format in 24-hour format
            return timestamp?.let {
                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                sdf.format(it)
            } ?: ""
        }
    }

    // ViewHolder for incoming messages
    class IncomingMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // References to the TextViews in the item layout.
        private val messageTextView: TextView = itemView.findViewById(R.id.textViewMessageIncoming)
        private val timestampTextView: TextView = itemView.findViewById(R.id.textViewTimestampIncoming)
        private val nameTextView: TextView = itemView.findViewById(R.id.textViewNameIncoming)

        // The bind function sets the content of each TextView to the properties of the Message object.
        fun bind(message: Message) {
            // Set the message text
            messageTextView.text = message.message
            // If senderName is not null or empty, it is set to the TextView. Otherwise, a placeholder is shown.
            nameTextView.text = message.senderName ?: "Loading name..."
            // Formats and sets the timestamp. If the timestamp is null, "Time Unknown" is displayed.
            timestampTextView.text = formatTime(message.timestamp)

            // Make URLs within the message text clickable
            Linkify.addLinks(messageTextView, Linkify.WEB_URLS)
            messageTextView.movementMethod = LinkMovementMethod.getInstance()

            // Log statement for debugging - logs the message text and the sender's name.
            Log.d("IncomingMessageViewHolder", "Binding message: ${message.message} from senderName: ${message.senderName}")
        }

        // Helper function to format the timestamp of the message.
        private fun formatTime(timestamp: Date?): String {
            return timestamp?.let {
                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                sdf.format(it)
            } ?: "Time Unknown"
        }
    }

}
