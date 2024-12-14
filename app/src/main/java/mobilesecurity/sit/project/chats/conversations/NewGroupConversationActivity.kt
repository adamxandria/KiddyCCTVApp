package mobilesecurity.sit.project.chats.conversations

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import mobilesecurity.sit.project.R
import mobilesecurity.sit.project.mainpage.MainActivity

class NewGroupConversationActivity : AppCompatActivity() {

    private lateinit var addParticipantInput: TextInputEditText
    private lateinit var addParticipantButton: Button
    private lateinit var createGroupButton: Button
    private lateinit var groupNameInput: TextInputEditText
    private lateinit var participantsAdapter: NewGroupConversationAdapter
    private val firestore = FirebaseFirestore.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    private val participants = mutableListOf<Participant>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_group_conversation)

        if (currentUserId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        fetchCurrentUserNameAndAddToParticipants(currentUserId) // Add the current user to the participants list

        val ivBackArrow = findViewById<ImageView>(R.id.ivBackArrow)
        ivBackArrow.setOnClickListener {
            // Explicitly navigate back to MainActivity
            val intent = Intent(this, ConversationActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
        }

        setupViews()
        setupRecyclerView()

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

    private fun fetchCurrentUserNameAndAddToParticipants(userId: String) {
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { documentSnapshot ->
                val userName = documentSnapshot.getString("name") ?: "Unknown User"
                // Add the current user to the participants list with their name
                val currentUserParticipant = Participant(id = userId, name = userName)
                participants.add(currentUserParticipant)
                // Since this is the initial setup, no need to notify the adapter as it hasn't been set up yet
            }
            .addOnFailureListener { e ->
                Log.e("NewGroupConvActivity", "Error fetching current user's name", e)
                Toast.makeText(this, "Failed to fetch user details", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupViews() {
        addParticipantInput = findViewById(R.id.addParticipantTextInputEditText)
        addParticipantButton = findViewById(R.id.addParticipantButton)
        createGroupButton = findViewById(R.id.createGroupButton)
        groupNameInput = findViewById(R.id.groupNameInputEditText)

        addParticipantButton.setOnClickListener { addParticipant() }
        createGroupButton.setOnClickListener { createGroupChat() }
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerViewConversations)
        recyclerView.layoutManager = LinearLayoutManager(this)
        participantsAdapter = NewGroupConversationAdapter(participants)
        recyclerView.adapter = participantsAdapter
    }

    private fun addParticipant() {
        val participantName = addParticipantInput.text.toString().trim()

        if (participantName.isEmpty()) {
            Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show()
            return
        }

        // Search for the user by name
        firestore.collection("users")
            .whereEqualTo("name", participantName)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                } else {
                    val userId = documents.documents[0].id
                    if (participants.any { participant -> participant.id == userId }) {
                        Toast.makeText(this, "User already added", Toast.LENGTH_SHORT).show()
                    } else {
                        val userName = documents.documents[0].getString("name")
                        participants.add(Participant(id = userId, name = userName))
                        participantsAdapter.notifyItemInserted(participants.size - 1)
                        addParticipantInput.text = null // Clear the input field
                    }
                }
            }
    }

    private fun createGroupChat() {
        val groupName = groupNameInput.text.toString().trim()
        if (groupName.isEmpty()) {
            Toast.makeText(this, "Please enter a group name", Toast.LENGTH_SHORT).show()
            return
        }

        if (participants.size < 2) {
            Toast.makeText(this, "Add at least one more participant", Toast.LENGTH_SHORT).show()
            return
        }

        // Extract participant IDs from the participants list
        val participantIds = participants.map { it.id }

        val newConversation = hashMapOf(
            "groupName" to groupName,
            // Use the extracted participant IDs here
            "participants" to participantIds
        )

        firestore.collection("Conversations").add(newConversation)
            .addOnSuccessListener { documentReference ->
                val conversationId = documentReference.id
                updateParticipantsUserConversations(conversationId)
                Toast.makeText(this, "Group chat created successfully", Toast.LENGTH_SHORT).show()
                finish() // Close the activity
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to create group chat", Toast.LENGTH_SHORT).show()
            }
    }


    private fun updateParticipantsUserConversations(conversationId: String) {
        // Iterate over each Participant object in the participants list
        participants.forEach { participant ->
            // Use the id property of the Participant object to reference the user document
            val userRef = firestore.collection("users").document(participant.id)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                val userConversations = snapshot.get("userConversations") as? MutableList<String> ?: mutableListOf()
                userConversations.add(conversationId)
                transaction.update(userRef, "userConversations", userConversations)
            }.addOnSuccessListener {
                // Handle success, e.g., logging or UI update
            }.addOnFailureListener { e ->
                // Handle failure, e.g., logging or showing an error message
            }
        }
    }
}
