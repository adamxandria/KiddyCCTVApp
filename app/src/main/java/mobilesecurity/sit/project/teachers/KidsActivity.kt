
package mobilesecurity.sit.project.teachers

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import mobilesecurity.sit.project.R
import mobilesecurity.sit.project.chats.conversations.ConversationActivity
import mobilesecurity.sit.project.databinding.ActivityKidsactivitiesBinding
import mobilesecurity.sit.project.mainpage.MainActivity


class KidsActivity : AppCompatActivity() {


    private lateinit var binding: ActivityKidsactivitiesBinding
    private val db by lazy { FirebaseFirestore.getInstance() }
    private lateinit var adapter: FoldersAdapter
    private var foldersList: MutableList<Folder> = mutableListOf()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        supportActionBar?.hide()
        binding = ActivityKidsactivitiesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        fetchFolders()

        binding.addFolderButton.setOnClickListener {
            showAddFolderDialog() // Show the dialog when the add button is clicked

        adapter.addFolder(Folder("testId", "Test Folder"))

        }

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

    private fun setupRecyclerView() {
        adapter = FoldersAdapter(mutableListOf()) { folder ->
            Log.d("KidsActivity", "Folder clicked: Name=${folder.name}, ID=${folder.id}")

            val intent = Intent(this, UploadActivity::class.java).apply {
                putExtra("FOLDER_NAME", folder.name)
                putExtra("folderId", folder.id)
            }
            startActivity(intent)
            // Here you can implement click actions for each folder item, like opening a detail view
            Toast.makeText(this, "Clicked on ${folder.name}", Toast.LENGTH_SHORT).show()
        }

        binding.recyclerViewFolders.layoutManager = LinearLayoutManager(this)
        val itemDecoration = SpacesItemDecoration(20)
        binding.recyclerViewFolders.addItemDecoration(itemDecoration)
        binding.recyclerViewFolders.adapter = adapter
    }

    private fun fetchFolders() {
        db.collection("folders")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val folders = querySnapshot.documents.map { document ->
                    Folder(id = document.id, name = document.getString("name") ?: "")
                }
                adapter.updateFolders(folders)
                // Add a log to check if IDs are fetched
                folders.forEach { folder ->
                    Log.d("KidsActivity", "Fetched folder: ${folder.name} with ID: ${folder.id}")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("KidsActivity", "Error fetching folders: ", exception)
            }
    }



    private fun showAddFolderDialog() {
        val editText = EditText(this).apply {
            hint = "Enter folder name"
        }
        AlertDialog.Builder(this)
            .setTitle("Add New Folder")
            .setView(editText)
            .setPositiveButton("Add") { dialog, which ->
                val folderName = editText.text.toString().trim()
                if (folderName.isNotEmpty()) {
                    addFolderToFirestore(folderName)
                } else {
                    Toast.makeText(this, "Folder name cannot be empty.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addFolderToFirestore(folderName: String) {
        val folderData = hashMapOf("name" to folderName)
        db.collection("folders").add(folderData)
            .addOnSuccessListener { documentReference ->
                val newFolder = Folder(documentReference.id, folderName)
                adapter.addFolder(newFolder)
                Log.d("KidsActivity", "Folder added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w("KidsActivity", "Error adding folder", e)
                Toast.makeText(this, "Error adding folder: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }
    }




