package mobilesecurity.sit.project.chats.messages

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location

import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import mobilesecurity.sit.project.R
import mobilesecurity.sit.project.chats.conversations.ConversationActivity
import com.google.android.gms.location.LocationRequest
import mobilesecurity.sit.project.mainpage.MainActivity
import java.util.*

class MessageActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var messagesAdapter: MessageAdapter
    private lateinit var firestore: FirebaseFirestore
    private lateinit var editTextMessage: EditText
    private lateinit var buttonSend: ImageView
    private lateinit var buttonSendLocation: ImageView
    private var conversationId: String? = null
    private val senderNamesCache = mutableMapOf<String, String>()
    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        conversationId = intent.getStringExtra("conversationId")
        if (conversationId == null) {
            Toast.makeText(this, "Conversation not found.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        val ivBackArrow = findViewById<ImageView>(R.id.ivBackArrow)
        ivBackArrow.setOnClickListener {
            // Explicitly navigate back to MainActivity
            val intent = Intent(this, ConversationActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
        }

        buttonSendLocation = findViewById(R.id.buttonSendLoc)
        buttonSendLocation.setOnClickListener {
            checkLocationPermissionAndSend()
        }

        initializeUI()
        fetchAndSetConversationName(conversationId!!)
        loadMessages()
    }

    private fun checkLocationPermissionAndSend() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), REQUEST_LOCATION_PERMISSION)
        } else {
            sendCurrentLocation()
        }
    }

    private fun sendCurrentLocation() {
        // Check if the location permissions are granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), REQUEST_LOCATION_PERMISSION)
            return
        }


        // Permissions are granted, proceed with getting the location
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val cancellationTokenSource = CancellationTokenSource()

        fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.token)
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    // Construct the Google Maps link
                    val latLng = "${it.latitude},${it.longitude}"
                    val mapsLink = "https://www.google.com/maps/search/?api=1&query=$latLng"
                    editTextMessage.setText(mapsLink)
                    sendMessage() // Now sendMessage reads the link from the EditText
                } ?: run {
                    // Handle the case where location is null
                    Toast.makeText(this, "Failed to get current location.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                // Handle any errors fetching the location
                Log.e("MessageActivity", "Failed to get current location", e)
                Toast.makeText(this, "Failed to get current location: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permissions are granted
                sendCurrentLocation()
            } else {
                // Permissions are denied
                Toast.makeText(this, "Location permission is required to send your current location.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun fetchAndSetConversationName(convoId: String) {
        firestore.collection("Conversations").document(convoId).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    Log.d("MessageActivity", "Conversation data fetched successfully")
                    val groupName = documentSnapshot.getString("groupName")
                    if (!groupName.isNullOrEmpty()) {
                        // If groupName exists, set it as the conversation name.
                        setConversationName(groupName)
                    } else {
                        Log.d("MessageActivity", "Conversation data does not exist")
                        // No groupName, attempt to set a participant's name.
                        // This step requires fetching participants' details, which is not fully implemented here.
                        // You'll need to adjust this logic based on your Firestore schema.
                        val participantId = documentSnapshot.get("participants") as? List<String> ?: listOf()
                        if (participantId.isNotEmpty()) {
                            // Assuming you have a way to fetch a participant's name by their ID.
                            fetchParticipantNameExcludingCurrentUser(participantId) { name ->
                                setConversationName(name)
                            }
                            Log.d("MessageActivity", "Manage to reach here")
                        }

                    }
                } else {
                    Log.d("MessageActivity", "No such conversation exists")
                }
            }
            .addOnFailureListener { e ->
                Log.e("MessageActivity", "Error fetching conversation details: ${e.message}", e)
            }
    }


    private fun setConversationName(name: String) {
        val conversationNameTextView: TextView = findViewById(R.id.conversationName)
        conversationNameTextView.text = name
    }

    private fun loadMessages() {
        conversationId?.let { convoId ->
            firestore.collection("Conversations").document(convoId)
                .collection("Messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Toast.makeText(this@MessageActivity, "Failed to load messages: ${e.message}", Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }

                    val messageList = mutableListOf<Message>()
                    val fetchTasks = mutableListOf<Task<*>>()
                    snapshot?.documents?.forEach { document ->
                        val message = document.toObject(Message::class.java)
                        if (message != null && message.senderId != null) {
                            messageList.add(message)
                            if (!senderNamesCache.containsKey(message.senderId)) {
                                val fetchTask = firestore.collection("users").document(message.senderId).get()
                                    .addOnSuccessListener { userDocument ->
                                        val senderName = userDocument.getString("name") ?: "Unknown"
                                        senderNamesCache[message.senderId] = senderName
                                        message.senderName = senderName
                                        // This ensures we update the adapter on the UI thread as soon as the name is fetched.
                                        this@MessageActivity.runOnUiThread {
                                            messagesAdapter.notifyItemChanged(messageList.indexOf(message))
                                        }
                                    }
                                fetchTasks.add(fetchTask)
                            } else {
                                message.senderName = senderNamesCache[message.senderId]
                            }
                        }
                    }
                    messagesAdapter.updateMessages(messageList)
                    // After all the fetch tasks are completed, update the entire adapter.
                    Tasks.whenAllComplete(fetchTasks).addOnCompleteListener {
                        this@MessageActivity.runOnUiThread {
                            messagesAdapter.notifyDataSetChanged()
                        }
                    }
                }
        }
    }




    private fun fetchParticipantNameExcludingCurrentUser(participantIds: List<String>, callback: (String) -> Unit) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) {
            callback("Unknown User")
            return
        }

        // Filter out the current user ID from the participant IDs list
        val otherParticipantIds = participantIds.filterNot { it == currentUserId }

        // In a simple scenario, you may just want to look at the first other participant.
        // You can modify this logic if you need to handle multiple other participants.
        val otherParticipantId = otherParticipantIds.firstOrNull()
        if (otherParticipantId != null) {
            // Access the users collection and retrieve the user's name
            firestore.collection("users").document(otherParticipantId).get()
                .addOnSuccessListener { documentSnapshot ->
                    Log.d("MessageActivity", "Participant data fetched successfully")
                    val name = documentSnapshot.getString("name") ?: "Unknown"
                    callback(name)
                }
                .addOnFailureListener { e ->
                    Log.e("MessageActivity", "Error fetching user details: ${e.message}", e)
                    callback("Error")
                }
        } else {
            // No other participant found or only the current user is in the conversation.
            callback("No Other Participants")
        }
    }


    private fun initializeUI() {
        recyclerView = findViewById(R.id.recyclerViewMessages)
        recyclerView.layoutManager = LinearLayoutManager(this)
        messagesAdapter = MessageAdapter()
        recyclerView.adapter = messagesAdapter

        firestore = FirebaseFirestore.getInstance()

        editTextMessage = findViewById(R.id.editTextMessage)
        buttonSend = findViewById(R.id.buttonSend)
        buttonSend.setOnClickListener { sendMessage() }
    }

    private fun sendMessage() {
        val messageText = editTextMessage.text.toString().trim()
        if (messageText.isNotEmpty()) {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            // Prepare the new message with a server-side timestamp
            val newMessage = hashMapOf(
                "senderId" to currentUserId,
                "message" to messageText,
                "timestamp" to FieldValue.serverTimestamp() // Use server-side timestamp
            )

            conversationId?.let { convoId ->
                firestore.collection("Conversations").document(convoId)
                    .collection("Messages").add(newMessage)
                    .addOnSuccessListener {
                        editTextMessage.text.clear()
                        // Update the conversation with the last message details
                        updateConversation(convoId, messageText, currentUserId)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to send message: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
        editTextMessage.text.clear()
    }

    private fun updateConversation(convoId: String, lastMessage: String, senderId: String) {
        val conversationUpdate = hashMapOf(
            "lastMessage" to lastMessage,
            "lastMessageSenderId" to senderId,
            "lastMessageTS" to FieldValue.serverTimestamp() // Use server-side timestamp for consistency
        )

        firestore.collection("Conversations").document(convoId)
            .update(conversationUpdate)
            .addOnSuccessListener {
                Log.d("MessageActivity", "Conversation updated successfully with server timestamp.")
            }
            .addOnFailureListener { e ->
                Log.e("MessageActivity", "Error updating conversation: ${e.message}")
            }
    }
}
