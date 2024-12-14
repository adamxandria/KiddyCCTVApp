package mobilesecurity.sit.project.principal

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import mobilesecurity.sit.project.R
import mobilesecurity.sit.project.chats.conversations.ConversationActivity
import mobilesecurity.sit.project.mainpage.MainActivity

class ManageRoleActivity : AppCompatActivity() {
    private val TAG = "ManageRoleActivity"
    private val firestoreDb = Firebase.firestore // Initialize Firestore
    private lateinit var userAdapter: UserAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        supportActionBar?.hide()
        setContentView(R.layout.activity_manage_role)

        // Initialize RecyclerView and UserAdapter
        recyclerView = findViewById(R.id.rvAdminList) // Replace 'rvAdminList' with your actual RecyclerView ID
        userAdapter = UserAdapter(mutableListOf()) { userId ->
            // Handle the user click with the user ID
            val intent = Intent(this@ManageRoleActivity, ViewUserActivity::class.java).apply {
                putExtra("user_id", userId)
            }
            startActivity(intent)
        }


        // Set up RecyclerView with the adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = userAdapter

        val role = intent.getStringExtra("role") ?: let {
            Log.e(TAG, "No role specified.")
            finish()
            return
        }
        val roleTitle = intent.getStringExtra("roleTitle")
        // Set the page title
        val pageTitleTextView: TextView = findViewById(R.id.tvAdminsHeader)
        pageTitleTextView.text = roleTitle ?: "Users"
        val backButton: ImageView = findViewById(R.id.ivBackArrow)
        backButton.setOnClickListener {
            finish()
        }

        // Fetch users with the specified role
        fetchUsersByRole(role)

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

    override fun onResume() {
        super.onResume()
        // Optional: Check if 'role' is still valid or if it needs to be fetched again.
        val role = intent.getStringExtra("role")
        if (!role.isNullOrEmpty()) {
            fetchUsersByRole(role)
        }
    }

    // Assuming this method fetches users based on their role and updates the RecyclerView
    private fun fetchUsersByRole(role: String) {
        val firestoreDb = FirebaseFirestore.getInstance() // Initialize Firestore if not already

        firestoreDb.collection("users")
            .whereEqualTo("role", role)
            .get() // Using get() for a single fetch
            .addOnSuccessListener { snapshot ->
                if (snapshot != null && !snapshot.isEmpty) {
                    // Assign the document ID to the User object's id property
                    val users = snapshot.documents.mapNotNull { document ->
                        document.toObject(User::class.java)?.apply { id = document.id }
                    }.toMutableList()

                    updateRecyclerView(users)
                } else {
                    Log.d(TAG, "No users found for role: $role")
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error fetching users: ", e)
            }
    }

    private fun updateRecyclerView(users: MutableList<User>) {
        val userAdapter = UserAdapter(users) { userId ->
            // Handle the user click with the user ID
            val intent = Intent(this, ViewUserActivity::class.java).apply {
                putExtra("user_id", userId)
            }
            startActivity(intent)
        }

        val recyclerView: RecyclerView = findViewById(R.id.rvAdminList) // Replace with your RecyclerView ID
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = userAdapter
    }

}