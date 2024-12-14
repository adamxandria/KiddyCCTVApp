package mobilesecurity.sit.project.chats.conversations

import androidx.annotation.Keep
import java.util.Date

@Keep data class Conversation(
    var chatId: String? = null,
    var groupName: String? = null,
    var lastMessage: String? = null,
    var lastMessageSenderId: String? = null,
    var lastMessageTS: Date? = null,
    var participants: List<String>? = null,
    var participantNames: List<String>? = null // Add this to hold participant names
) {
    

    // No-argument constructor for Firestore deserialization
    constructor() : this(null, null, null, null, null)
}