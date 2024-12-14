package mobilesecurity.sit.project.parents

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import mobilesecurity.sit.project.R
import com.google.android.material.textfield.TextInputEditText
import mobilesecurity.sit.project.chats.conversations.ConversationActivity
import mobilesecurity.sit.project.mainpage.MainActivity

class TeachersActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TeacherAdapter
    private lateinit var searchTeacherTextInputEditText: TextInputEditText
    private val db = FirebaseFirestore.getInstance() // Firestore instance
    private var teachersList: List<TeacherInfoDataClass> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        supportActionBar?.hide()
        setContentView(R.layout.activity_listofteachers)

        // Initialize RecyclerView and Adapter
        recyclerView = findViewById(R.id.teachersList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TeacherAdapter(emptyList()) { selectedteacher ->
            Toast.makeText(this, "Clicked on: ${selectedteacher.name}", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, TeacherDetailActivity::class.java).apply {
                putExtra("teacher_name", selectedteacher.name)
                putExtra("teacher_email", selectedteacher.email)
                putExtra("teacher_role", selectedteacher.role)
                putExtra("teacher_subject", selectedteacher.subject)
                putExtra("teacher_bio", selectedteacher.bio)
                putExtra("teacher_hp", selectedteacher.hp)
                putExtra("profile_image_url", selectedteacher.profileImg)
            }
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        // Setup search functionality
        searchTeacherTextInputEditText = findViewById(R.id.searchTeacherTextInputEditText)
        searchTeacherTextInputEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterTeachers(s.toString())
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        // Fetch teachers from database
        loadTeachers()

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

    private fun filterTeachers(text: String) {
        val filteredList = teachersList.filter { teacher ->
            teacher.name.contains(text, ignoreCase = true)
        }
        adapter.updateTeachers(filteredList)
    }

    private fun loadTeachers() {
        db.collection("users")
            .whereEqualTo("role", "Teacher")
            .get()
            .addOnSuccessListener { documents ->
                teachersList = documents.toObjects(TeacherInfoDataClass::class.java)
                adapter.updateTeachers(teachersList)
            }
            .addOnFailureListener { exception ->
                Log.d("FirestoreError", "Error getting documents: ", exception)
            }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
