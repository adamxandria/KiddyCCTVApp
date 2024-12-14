package mobilesecurity.sit.project.chats.conversations

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import mobilesecurity.sit.project.R
import mobilesecurity.sit.project.chats.messages.MessageActivity
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import com.google.android.gms.tasks.Tasks
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FieldValue
import mobilesecurity.sit.project.chats.conversations.NewConversationActivity
import mobilesecurity.sit.project.mainpage.MainActivity

class ConversationActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var conversationsAdapter: ConversationAdapter
    private lateinit var firestore: FirebaseFirestore
    private var conversationsListener: ListenerRegistration? = null
    private var fetchedConversations = mutableListOf<Conversation>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        supportActionBar?.hide()
        setContentView(R.layout.activity_conversation)


        initializeRecyclerView()
        initializeFirebaseReferences()
        loadConversations()

        val ivBackArrow = findViewById<ImageView>(R.id.ivBackArrow)
        ivBackArrow.setOnClickListener {
            // Explicitly navigate back to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
        }

        //initialise message button
        val createConversationButton = findViewById<Button>(R.id.createConversationButton)
        createConversationButton.setOnClickListener {
            // Intent to start ConversationActivity
            val intent = Intent(this, NewConversationActivity::class.java)
            startActivity(intent)
        }

        val createGroupButton = findViewById<Button>(R.id.createGroupButton)
        createGroupButton.setOnClickListener {
            // Intent to start ConversationActivity
            val intent = Intent(this, NewGroupConversationActivity::class.java)
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


    private fun deleteConversation(conversationId: String) {
        // First, fetch the conversation to get the list of participant IDs
        firestore.collection("Conversations").document(conversationId).get()
            .addOnSuccessListener { documentSnapshot ->
                val conversation = documentSnapshot.toObject(Conversation::class.java)
                conversation?.let {
                    // Perform a batch write to delete the conversation and update each participant's userConversations field
                    val batch = firestore.batch()

                    // Delete the conversation document
                    val conversationRef = firestore.collection("Conversations").document(conversationId)
                    batch.delete(conversationRef)

                    // Remove the conversationId from each participant's userConversations array
                    it.participants?.forEach { userId ->
                        val userRef = firestore.collection("users").document(userId)
                        batch.update(userRef, "userConversations", FieldValue.arrayRemove(conversationId))
                    }

                    // Commit the batch
                    batch.commit().addOnSuccessListener {
                        Log.d("ConversationActivity", "Conversation and references successfully deleted")
                        // Update UI accordingly, e.g., remove the conversation from the list and notify the adapter
                        fetchedConversations.removeAll { conv -> conv.chatId == conversationId }
                        conversationsAdapter.updateConversations(fetchedConversations)
                    }.addOnFailureListener { e ->
                        Log.e("ConversationActivity", "Error deleting conversation and references", e)
                    }
                }
            }.addOnFailureListener { e ->
                Log.e("ConversationActivity", "Error fetching conversation to delete", e)
            }
    }

    override fun onResume() {
        super.onResume()
        // Consider removing this if you decide to use real-time updates with Firestore listeners
        // to avoid unnecessary network calls.
        loadConversations()
    }

    override fun onPause() {
        super.onPause()
        conversationsListener?.remove() // Ensure to detach the listener
    }

    private fun initializeRecyclerView() {
        recyclerView = findViewById(R.id.recyclerViewConversations)
        recyclerView.layoutManager = LinearLayoutManager(this)
        conversationsAdapter = ConversationAdapter(
            mutableListOf(),
            { conversationId -> navigateToMessageActivity(conversationId) },
            { conversationId -> deleteConversation(conversationId) }// Pass the delete method
        )
        recyclerView.adapter = conversationsAdapter
    }

    private fun initializeFirebaseReferences() {
        firestore = FirebaseFirestore.getInstance()
    }

    private fun loadConversations() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        Log.d("ConversationActivity", "Current user ID: $currentUserId")

        // Listen for real-time updates on the user's document to get the list of conversation IDs
        val userRef = firestore.collection("users").document(currentUserId)
        conversationsListener = userRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("ConversationActivity", "Listen failed.", e)
                return@addSnapshotListener
            }

            val userConversations = snapshot?.get("userConversations") as? List<String> ?: listOf()
            Log.d("ConversationActivity", "User conversations IDs: $userConversations")

            if (userConversations.isEmpty()) {
                Log.d("ConversationActivity", "No conversations found for user.")
                conversationsAdapter.updateConversations(emptyList())
            } else {
                // For each conversation ID, listen for real-time updates
                userConversations.forEach { conversationId ->
                    val conversationRef = firestore.collection("Conversations").document(conversationId)
                    conversationRef.addSnapshotListener { convoSnapshot, convoError ->
                        if (convoError != null) {
                            Log.w("ConversationActivity", "Conversation listen failed.", convoError)
                            return@addSnapshotListener
                        }

                        val conversation = convoSnapshot?.toObject(Conversation::class.java)?.apply {
                            this.chatId = conversationId
                        }

                        conversation?.let { conv ->
                            // Update or add the conversation in the local list
                            val index = fetchedConversations.indexOfFirst { it.chatId == conversationId }
                            if (index != -1) {
                                fetchedConversations[index] = conv
                            } else {
                                fetchedConversations.add(conv)
                            }

                            // Fetch participant names if necessary
                            val participantIds = conv.participants?.filter { id -> id != currentUserId } ?: listOf()
                            val nameFetchTasks = participantIds.map { id ->
                                firestore.collection("users").document(id).get().continueWith { task ->
                                    task.result.getString("name") ?: "Unknown"
                                }
                            }

                            Tasks.whenAllSuccess<String>(nameFetchTasks).addOnSuccessListener { names ->
                                conv.participantNames = names.filterNot { it.isBlank() }
                                // Update the adapter with the modified list
                                conversationsAdapter.updateConversations(ArrayList(fetchedConversations))
                            }
                        }
                    }
                }
            }
        }
    }



    private fun navigateToMessageActivity(conversationId: String) {
        val intent = Intent(this, MessageActivity::class.java).apply {
            putExtra("conversationId", conversationId)
        }
        startActivity(intent)
    }
}
