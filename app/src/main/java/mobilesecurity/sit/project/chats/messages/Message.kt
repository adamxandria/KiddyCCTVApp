package mobilesecurity.sit.project.chats.messages

import androidx.annotation.Keep
import java.util.Date
// Data class representing a message
@Keep
data class Message(
    val senderId: String? = null,  // ID of the user who sent the message
    val message: String? = null,  // The message text
    var timestamp: Date? = null, // Timestamp of the message
    var senderName: String? = null // This will hold the sender's name
)
