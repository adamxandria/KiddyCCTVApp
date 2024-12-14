package mobilesecurity.sit.project.principal

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.FirebaseFirestore
import mobilesecurity.sit.project.R
import com.google.firebase.firestore.SetOptions
import mobilesecurity.sit.project.chats.conversations.ConversationActivity
import mobilesecurity.sit.project.mainpage.MainActivity

class ViewUserActivity : AppCompatActivity() {

    private lateinit var firestoreDb: FirebaseFirestore
    // Add lateinit vars for your TextInputLayouts
    private lateinit var emailTextInputLayout: TextInputLayout
    private lateinit var phoneNumberTextInputLayout: TextInputLayout
    private lateinit var childIdTextInputLayout: TextInputLayout
    private lateinit var classGroupTextInputLayout: TextInputLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        supportActionBar?.hide()
        setContentView(R.layout.activity_view_user) // Make sure this matches your layout file

        firestoreDb = FirebaseFirestore.getInstance()

        // In ViewUserActivity
        val userId = intent.getStringExtra("user_id")
        if (userId == null || userId.isEmpty()) {
            Log.e("ViewUserActivity", "Error: User ID is null or empty.")
            Toast.makeText(this, "Error: User ID is not provided.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Views
        val fullNameEditText = findViewById<TextInputEditText>(R.id.fullNameTextInputEditText)
        val nicknameEditText = findViewById<TextInputEditText>(R.id.nameTextInputEditText)
        val emailLabelEditText = findViewById<TextInputEditText>(R.id.emailTextInputEditText)
        val phoneNumberEditText = findViewById<TextInputEditText>(R.id.hpTextInputEditText)
        val genderAutoCompleteTextView = findViewById<MaterialAutoCompleteTextView>(R.id.genderAutoCompleteTextView)
        val addressEditText = findViewById<TextInputEditText>(R.id.addressTextInputEditText)
        val roleAutoCompleteTextView = findViewById<MaterialAutoCompleteTextView>(R.id.roleAutoCompleteTextView)
        val saveButton = findViewById<Button>(R.id.saveButton)
        val classGroupEditText = findViewById<TextInputEditText>(R.id.classGroupTextInputEditText)
        val childIDEditText = findViewById<TextInputEditText>(R.id.childIdTextInputEditText)
        // Initialize your TextInputLayouts
        emailTextInputLayout = findViewById(R.id.emailTextInputLayout)
        phoneNumberTextInputLayout = findViewById(R.id.hpTextInputLayout)
        childIdTextInputLayout = findViewById(R.id.childIdTextInputLayout)
        classGroupTextInputLayout = findViewById(R.id.classGroupTextInputLayout)

        // Now you can use userId to fetch user data
        Log.d("ViewUserActivity", "Received User ID: $userId")
        loadUserData(
            userId,
            emailLabelEditText,
            phoneNumberEditText,
            classGroupEditText,
            childIDEditText)

        val backButton = findViewById<ImageView>(R.id.ivBackArrow)
        backButton.setOnClickListener {
            finish() // This will close the current activity and return to the previous one
        }

        setupGenderDropdown(genderAutoCompleteTextView)
        setupRoleDropdown(roleAutoCompleteTextView)

        roleAutoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            val selectedRole = roleAutoCompleteTextView.adapter.getItem(position) as String
            updateViewVisibilityBasedOnRole(selectedRole)
        }

        saveButton.setOnClickListener {
            val name = fullNameEditText.text.toString()
            val nickname = nicknameEditText.text.toString()
            val email = emailLabelEditText.text.toString()
            val phone = phoneNumberEditText.text.toString()
            val gender = genderAutoCompleteTextView.text.toString()
            val role = roleAutoCompleteTextView.text.toString()
            val address = addressEditText.text.toString()

            saveUserData(userId, name, nickname, email, phone, gender, role, address)
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

    private fun updateViewVisibilityBasedOnRole(role: String) {
        when (role) {
            "Children" -> {
                emailTextInputLayout.visibility = View.GONE
                phoneNumberTextInputLayout.visibility = View.GONE
                childIdTextInputLayout.visibility = View.VISIBLE
                classGroupTextInputLayout.visibility = View.VISIBLE
            }
            "Parent" -> {
                emailTextInputLayout.visibility = View.VISIBLE
                phoneNumberTextInputLayout.visibility = View.VISIBLE
                childIdTextInputLayout.visibility = View.VISIBLE
                classGroupTextInputLayout.visibility = View.GONE
            }
            else -> {
                emailTextInputLayout.visibility = View.VISIBLE
                phoneNumberTextInputLayout.visibility = View.VISIBLE
                childIdTextInputLayout.visibility = View.GONE
                classGroupTextInputLayout.visibility = View.GONE
            }
        }
    }


    private fun setupGenderDropdown(genderAutoCompleteTextView: MaterialAutoCompleteTextView) {
        val genders = resources.getStringArray(R.array.gender_options)
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, genders)
        genderAutoCompleteTextView.setAdapter(adapter)
    }

    private fun setupRoleDropdown(roleAutoCompleteTextView: MaterialAutoCompleteTextView) {
        val roles = resources.getStringArray(R.array.role_options)
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, roles)
        roleAutoCompleteTextView.setAdapter(adapter)
    }

    private fun loadUserData(
        userId: String,
        emailLabelEditText: TextInputEditText,
        phoneNumberEditText: TextInputEditText,
        classGroupEditText: TextInputEditText,
        childIDEditText: TextInputEditText) {
        if (userId.isBlank()) {
            Toast.makeText(this, "Invalid user ID.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        firestoreDb.collection("users").document(userId).get()
            .addOnSuccessListener { documentSnapshot ->
                val user = documentSnapshot.toObject(User::class.java)
                user?.let {
                    populateUIBasedOnRole(it, userId)
                    updateViewVisibilityBasedOnRole(
                        user.role
                    )
                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    "Failed to load user data: ${it.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
    private fun populateUIBasedOnRole(user: User, userId: String) {
        // Populate different fields based on the user's role
        findViewById<TextInputEditText>(R.id.fullNameTextInputEditText).setText(user.fullName)
        findViewById<TextInputEditText>(R.id.nameTextInputEditText).setText(user.name)
        findViewById<TextInputEditText>(R.id.emailTextInputEditText).setText(user.email)
        findViewById<TextInputEditText>(R.id.hpTextInputEditText).setText(user.hp)
        findViewById<MaterialAutoCompleteTextView>(R.id.genderAutoCompleteTextView).setText(user.gender, false)
        findViewById<MaterialAutoCompleteTextView>(R.id.roleAutoCompleteTextView).setText(user.role, false)
        findViewById<TextInputEditText>(R.id.addressTextInputEditText).setText(user.address)
        if (user.role == "Children") {
            findViewById<TextInputEditText>(R.id.classGroupTextInputEditText).setText(user.classGroup)
            val childIDEditText = findViewById<TextInputEditText>(R.id.childIdTextInputEditText)
            childIDEditText.setText(userId)
            childIDEditText.isEnabled = false
        } else if (user.role == "Parent") {
            findViewById<TextInputEditText>(R.id.childIdTextInputEditText).setText(user.childID)
        }
    }

    private fun saveUserData(userId: String, fullName: String, nickname: String, email: String, phone: String, gender: String, role: String, address: String) {
        val updatedUser = hashMapOf(
            "fullName" to fullName,
            "name" to nickname,
            "email" to email,
            "hp" to phone,
            "gender" to gender,
            "role" to role,
            "address" to address
        )

        firestoreDb.collection("users").document(userId)
            .set(updatedUser, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "User data updated successfully.", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update user data: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }


}
