package mobilesecurity.sit.project.chats.conversations

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import mobilesecurity.sit.project.R // Make sure this import matches your project's structure
import mobilesecurity.sit.project.mainpage.MainActivity

class NewConversationActivity : AppCompatActivity() {

    private val activityScope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var firestore: FirebaseFirestore

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        supportActionBar?.hide()
        setContentView(R.layout.activity_new_conversation)

        firestore = FirebaseFirestore.getInstance()

        val createConversationInput = findViewById<TextInputEditText>(R.id.createConversationTextInputEditText)
        val createConversationButton = findViewById<Button>(R.id.createConversationButton)

        createConversationButton.setOnClickListener {
            val participantName = createConversationInput.text.toString().trim()
            if (participantName.isNotEmpty()) {
                Log.d("NewConvActivity", "Creating new chat for: $participantName")
                createNewChat(participantName)
            } else {
                Toast.makeText(this, "Please enter a participant name", Toast.LENGTH_SHORT).show()
            }
        }

        val ivBackArrow = findViewById<ImageView>(R.id.ivBackArrow)
        ivBackArrow.setOnClickListener {
            // Explicitly navigate back to MainActivity
            val intent = Intent(this, ConversationActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
        }


        //navigation bar:

        val bottomNavigationView =findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {

                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.message -> {
                    val intent = Intent(this, ConversationActivity::class.java)
                    startActivity(intent)
                    true
                }

                else -> false
            }

        }


    }

    private fun createNewChat(participantName: String) {
        activityScope.launch {
            val participantUserId = resolveParticipantNameToUserId(participantName)
            if (participantUserId == null) {
                Toast.makeText(this@NewConversationActivity, "Participant not found", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            if (currentUserId == null) {
                Toast.makeText(this@NewConversationActivity, "User not logged in", Toast.LENGTH_SHORT).show()
                return@launch
            }

            // Check if a conversation between these two users already exists
            val existingConversationId = checkExistingConversation(currentUserId, participantUserId)
            if (existingConversationId != null) {
                Toast.makeText(this@NewConversationActivity, "Chat already exists", Toast.LENGTH_SHORT).show()
            } else {
                // Proceed with creating a new conversation
                createConversation(participantUserId, currentUserId, participantName)
            }
        }
    }

    private suspend fun checkExistingConversation(currentUserId: String, participantUserId: String): String? = withContext(Dispatchers.IO) {
        val querySnapshot = firestore.collection("Conversations")
            .whereEqualTo("groupName", "")
            .whereArrayContainsAny("participants", listOf(currentUserId, participantUserId))
            .get()
            .await()

        for (document in querySnapshot.documents) {
            val participants = document["participants"] as? List<String>
            if (participants?.containsAll(listOf(currentUserId, participantUserId)) == true) {
                return@withContext document.id // Conversation exists
            }
        }
        return@withContext null // No existing conversation found
    }

    private fun createConversation(participantUserId: String, currentUserId: String, participantName: String) {
        val participants = listOf(currentUserId, participantUserId)
        val newConversation = hashMapOf(
            "participants" to participants,
            "groupName" to ""
        )

        firestore.collection("Conversations").add(newConversation)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(this@NewConversationActivity, "Chat created successfully", Toast.LENGTH_SHORT).show()

                // Update each participant's userConversations array
                participants.forEach { userId ->
                    updateUserConversations(userId, documentReference.id)
                }

                // Navigate to ConversationActivity
                val intent = Intent(this@NewConversationActivity, ConversationActivity::class.java).apply {
                    // Assuming 'conversationId' is the key you'll use to retrieve the conversation ID in ConversationActivity
                    putExtra("conversationId", documentReference.id)
                }
                startActivity(intent)
                finish() // Optionally, call finish() if you don't want users to return to this activity with the back button
            }
            .addOnFailureListener { e ->
                Toast.makeText(this@NewConversationActivity, "Failed to create chat", Toast.LENGTH_SHORT).show()
            }
    }



    private fun updateUserConversations(userId: String, conversationId: String) {
        val userRef = firestore.collection("users").document(userId)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val userConversations = snapshot.get("userConversations") as? MutableList<String> ?: mutableListOf()
            Log.d("NewConvActivity", "Before update: $userConversations")
            userConversations.add(conversationId)
            Log.d("NewConvActivity", "After update: $userConversations")
            transaction.update(userRef, "userConversations", userConversations)
        }.addOnSuccessListener {
            Log.d("NewConvActivity", "User $userId userConversations updated successfully.")
        }.addOnFailureListener { e ->
            Log.e("NewConvActivity", "Error updating user $userId userConversations", e)
        }
    }

    private suspend fun resolveParticipantNameToUserId(participantName: String): String? = withContext(Dispatchers.IO) {
        val usersCollection = FirebaseFirestore.getInstance().collection("users")

        try {
            val userSearch = usersCollection.whereEqualTo("name", participantName).limit(1).get().await()
            if (!userSearch.isEmpty) {
                val userId = userSearch.documents.first().id
                Log.d("NewConvActivity", "Found in Parents: $userId")
                return@withContext userId
            }

        } catch (e: Exception) {
            Log.e("NewConvActivity", "Error searching for user: ${e.message}")
        }
        Log.d("NewConvActivity", "No user found for: $participantName")
        return@withContext null
    }

    override fun onDestroy() {
        super.onDestroy()
        activityScope.cancel() // Cancel the scope to prevent memory leaks
    }
}