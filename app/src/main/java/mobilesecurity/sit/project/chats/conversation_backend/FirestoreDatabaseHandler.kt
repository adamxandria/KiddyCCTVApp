package mobilesecurity.sit.project.chats.conversation_backend

import com.google.firebase.firestore.FirebaseFirestore

class FirestoreDatabaseHandler(conversationStuff: ConversationStuff) {

    // Get the instance of the Firestore database
    private val db = FirebaseFirestore.getInstance()

    fun addNotification(notif: Conver) {
        // Create a new document in the "notifications" collection
        db.collection("conversation").add(notif)
            .addOnSuccessListener { documentReference ->
                println("DocumentSnapshot written with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                // Handle failure, for example, log the error
                e.printStackTrace()
            }
    }
}

