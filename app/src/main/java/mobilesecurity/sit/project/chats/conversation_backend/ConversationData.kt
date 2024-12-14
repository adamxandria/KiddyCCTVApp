package mobilesecurity.sit.project.chats.conversation_backend

data class ConversationData(
    val conversationStruct: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
