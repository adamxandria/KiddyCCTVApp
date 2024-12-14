package mobilesecurity.sit.project.parents

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User
import mobilesecurity.sit.project.R
import mobilesecurity.sit.project.chats.conversations.ConversationActivity
import mobilesecurity.sit.project.mainpage.MainActivity

class ParentKidsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private var dataList = ArrayList<DataClass>()
    private lateinit var adapter: MyAdapter
    private val firestoreInstance = FirebaseFirestore.getInstance()
    private lateinit var fab: FloatingActionButton

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        supportActionBar?.hide()
        setContentView(R.layout.activity_parent_activities)
        recyclerView = findViewById(R.id.recyclerView)

        val backImageView = findViewById<ImageView>(R.id.backParentActivityImageView)
        backImageView.setOnClickListener {
            // This will close the current activity and take you back to the previous activity
            finish()
        }

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        adapter = MyAdapter(dataList, this)
        recyclerView.adapter = adapter

        fetchChildClassAndData()

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

    private fun fetchChildClassAndData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return // Exit if userID is null
        if (userId == null) {
            Log.e("ParentKidsActivity", "User ID is null")
            return
        }
        Log.d("ParentKidsActivity", "Fetching data for user ID: $userId")

        firestoreInstance.collection("users").document(userId).get().addOnSuccessListener { parentSnapshot ->
            val childId = parentSnapshot.getString("childID")
            if (childId == null) {
                Log.e("ParentKidsActivity", "Child ID is null")
                return@addOnSuccessListener
            }
            firestoreInstance.collection("Children").document(childId).get().addOnSuccessListener { childSnapshot ->
                val childClass = childSnapshot.getString("class")
                if (childClass == null) {
                    Log.e("ParentKidsActivity", "Child class is null")
                    return@addOnSuccessListener
                }
                Log.d("ParentKidsActivity", "Found Child Class: $childClass")

                findFolderIdAndFetchImages(childClass)
            }.addOnFailureListener {
                Log.e("ParentKidsActivity", "Failed to fetch child info", it)
            }
        }.addOnFailureListener {
            Log.e("ParentKidsActivity", "Failed to fetch parent info", it)
        }
    }


    private fun findFolderIdAndFetchImages(childClass: String) {
        Log.d("ParentKidsActivity", "Searching for folder with class name: $childClass")
        firestoreInstance.collection("folders")
            .whereEqualTo("name", childClass)
            .limit(1)
            .get()
            .addOnSuccessListener { folders ->
                if (folders.isEmpty) {
                    Log.e("ParentKidsActivity", "No folder found for class: $childClass")
                    return@addOnSuccessListener
                }
                val folderId = folders.documents.firstOrNull()?.id
                if (folderId == null) {
                    Log.e("ParentKidsActivity", "Folder ID is null for class: $childClass")
                    return@addOnSuccessListener
                }
                Log.d("ParentKidsActivity", "Found folder ID: $folderId for class: $childClass")
                fetchImages(folderId)
            }.addOnFailureListener {
                Log.e("ParentKidsActivity", "Failed to find folder for class: $childClass", it)
            }
    }

    private fun fetchImages(folderId: String) {
        Log.d("ParentKidsActivity", "Fetching images for folder ID: $folderId")
        firestoreInstance.collection("images")
            .whereEqualTo("folderId", folderId)
            .get()
            .addOnSuccessListener { images ->
                if (images.isEmpty) {
                    Log.e("ParentKidsActivity", "No images found for folder ID: $folderId")
                    return@addOnSuccessListener
                }
                for (document in images) {
                    val imageUrl = document.getString("url")
                    val description = document.getString("description") ?: "No Description"
                    if (imageUrl == null) {
                        Log.w("ParentKidsActivity", "Found an image document without a URL")
                        continue
                    }
                    Log.d("ParentKidsActivity", "Adding image with URL: $imageUrl")
                    dataList.add(DataClass(imageUrl, description))
                }
                adapter.notifyDataSetChanged()
            }.addOnFailureListener {
                Log.e("ParentKidsActivity", "Failed to fetch images for folder ID: $folderId", it)
            }
    }
}






