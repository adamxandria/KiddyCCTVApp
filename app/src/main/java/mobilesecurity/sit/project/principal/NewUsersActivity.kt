package mobilesecurity.sit.project.principal

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import mobilesecurity.sit.project.R
import mobilesecurity.sit.project.chats.conversations.ConversationActivity
import mobilesecurity.sit.project.mainpage.MainActivity
import java.util.UUID

class NewUsersActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var profileImageUri: Uri? = null
    private lateinit var classGroupEditText: TextInputEditText
    private lateinit var childIDEditText: TextInputEditText
    private lateinit var emailTextInputLayout: TextInputLayout
    private lateinit var phoneNumberTextInputLayout: TextInputLayout
    private lateinit var passwordTextInputLayout: TextInputLayout
    private lateinit var childIdTextInputLayout: TextInputLayout
    private lateinit var classGroupTextInputLayout: TextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        supportActionBar?.hide()
        setContentView(R.layout.activity_new_user)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        val fullNameEditText = findViewById<TextInputEditText>(R.id.fullNameTextInputEditText)
        val nickNameEditText = findViewById<TextInputEditText>(R.id.nameTextInputEditText)
        val emailEditText = findViewById<TextInputEditText>(R.id.emailTextInputEditText)
        val phoneNumberEditText = findViewById<TextInputEditText>(R.id.hpTextInputEditText)
        val roleAutoCompleteTextView = findViewById<MaterialAutoCompleteTextView>(R.id.roleAutoCompleteTextView)
        val genderAutoCompleteTextView = findViewById<MaterialAutoCompleteTextView>(R.id.genderAutoCompleteTextView)
        val addressEditText = findViewById<TextInputEditText>(R.id.addressTextInputEditText)
        val passwordEditText = findViewById<TextInputEditText>(R.id.passwordTextInputEditText)
        val uploadImageView = findViewById<ImageView>(R.id.uploadImageView)
        val uploadButton = findViewById<LinearLayout>(R.id.uploadButton)
        val saveButton = findViewById<Button>(R.id.saveButton)
        val backButton: ImageView = findViewById(R.id.ivBackArrow)
        emailTextInputLayout = findViewById(R.id.emailTextInputLayout)
        phoneNumberTextInputLayout = findViewById(R.id.hpTextInputLayout)
        passwordTextInputLayout = findViewById(R.id.passwordTextInputLayout)
        childIdTextInputLayout = findViewById(R.id.childIdTextInputLayout)
        classGroupTextInputLayout = findViewById(R.id.classGroupTextInputLayout)
        classGroupEditText = findViewById(R.id.classGroupTextInputEditText)
        childIDEditText = findViewById(R.id.childIdTextInputEditText)

        backButton.setOnClickListener { finish() }

        val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                uploadImageView.setImageURI(uri)
                profileImageUri = uri
            }
        }

        uploadButton.setOnClickListener { galleryLauncher.launch("image/*") }

        setupGenderDropdown(genderAutoCompleteTextView)
        setupRoleDropdown(roleAutoCompleteTextView)

        roleAutoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            val selectedRole = roleAutoCompleteTextView.adapter.getItem(position) as String
            handleRoleSelection(selectedRole)
        }

        saveButton.setOnClickListener {
            val name = nickNameEditText.text.toString().trim()
            val role = roleAutoCompleteTextView.text.toString().trim()
            val fullName = fullNameEditText.text.toString().trim()
            val hp = if (role != "Children") phoneNumberEditText.text.toString().trim() else ""
            val gender = genderAutoCompleteTextView.text.toString().trim()
            val address = addressEditText.text.toString().trim()
            val email = if (role != "Children") emailEditText.text.toString().trim() else ""
            val password = if (role != "Children") passwordEditText.text.toString().trim() else ""
            val classGroup =
                if (role == "Children") classGroupEditText.text.toString().trim() else ""
            val childID = if (role == "Parent") childIDEditText.text.toString().trim() else ""

            // Validate only the fields that are relevant for the selected role
            if (fullName.isEmpty() || name.isEmpty() || gender.isEmpty() || role.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            if (role != "Children" && (email.isEmpty() || password.isEmpty() || hp.isEmpty())) {
                Toast.makeText(
                    this,
                    "Please fill in all required fields for Parent/Other roles",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (role == "Children" && classGroup.isEmpty()) {
                Toast.makeText(
                    this,
                    "Please enter the class group for Children",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (role == "Parent" && childID.isEmpty()) {
                Toast.makeText(this, "Please enter the Child ID for Parent", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            // Continue with the user creation process as all the required fields for the selected role are filled in
            uploadProfileImageAndSaveUser(
                email,
                name,
                role,
                fullName,
                hp,
                gender,
                address,
                password,
                classGroup,
                childID
            )
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

    private fun handleRoleSelection(selectedRole: String) {
        when (selectedRole) {
            "Children" -> {
                emailTextInputLayout.visibility = View.GONE
                phoneNumberTextInputLayout.visibility = View.GONE
                passwordTextInputLayout.visibility = View.GONE
                childIdTextInputLayout.visibility = View.GONE
                classGroupTextInputLayout.visibility = View.VISIBLE
            }
            "Parent" -> {
                emailTextInputLayout.visibility = View.VISIBLE
                phoneNumberTextInputLayout.visibility = View.VISIBLE
                passwordTextInputLayout.visibility = View.VISIBLE
                classGroupTextInputLayout.visibility = View.GONE
                childIdTextInputLayout.visibility = View.VISIBLE
            }
            else -> {
                emailTextInputLayout.visibility = View.VISIBLE
                phoneNumberTextInputLayout.visibility = View.VISIBLE
                passwordTextInputLayout.visibility = View.VISIBLE
                classGroupTextInputLayout.visibility = View.GONE
                childIdTextInputLayout.visibility = View.GONE
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

    private fun uploadProfileImageAndSaveUser(
        email: String,
        name: String,
        role: String,
        fullName: String,
        hp: String,
        gender: String,
        address: String,
        password: String,
        classGroup: String,
        childID: String
    ) {
        if (profileImageUri != null) {
            val storageRef = storage.reference.child("profileImages/${System.currentTimeMillis()}.jpg")
            storageRef.putFile(profileImageUri!!).addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    createUser(email, password, name, role, fullName, hp, gender, address, downloadUri.toString(), classGroup, childID)
                }.addOnFailureListener {
                    Toast.makeText(this, "Image upload failed: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            createUser(email, password, name, role, fullName, hp, gender, address, null, classGroup, childID)
        }
    }


    // Firestore authentication
    private fun createUser(
        email: String,
        password: String,
        name: String,
        role: String,
        fullName: String,
        hp: String,
        gender: String,
        address: String,
        imageUrl: String?,
        classGroup: String,
        childID: String
    ) {
        if (role == "Children") {
            // For children, directly save the user data to Firestore without creating an Auth user
            val userId = UUID.randomUUID().toString() // Generates a unique identifier
            saveUserToFirestore(userId, "", name, role, fullName, "", gender, address, imageUrl, classGroup, "")
        } else {
            // For roles other than "Children", proceed with creating an authentication user
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: ""
                    saveUserToFirestore(userId, email, name, role, fullName, hp, gender, address, imageUrl, "", childID)
                } else {
                    Toast.makeText(this, "Failed to create user: ${task.exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Firestore collection
    private fun saveUserToFirestore(
        userId: String,
        email: String,
        name: String,
        role: String,
        fullName: String,
        hp: String,
        gender: String,
        address: String,
        imageUrl: String?,
        classGroup: String,
        childID: String
    ) {
        val userMap = hashMapOf(
            "userId" to userId,
            "name" to name,
            "fullName" to fullName,
            "role" to role,
            "gender" to gender,
            "address" to address,
            "profileImg" to (imageUrl ?: "")
        )

        // Include these fields only for specific roles
        if (role != "Children") {
            userMap["email"] = email
            userMap["hp"] = hp
            // Do not store the password in Firestore
        }

        if (role == "Children") {
            userMap["classGroup"] = classGroup
        } else if (role == "Parent") {
            userMap["childID"] = childID
        }

        // Save to Firestore
        firestore.collection("users").document(userId)
            .set(userMap)
            .addOnSuccessListener {
                Toast.makeText(this, "User data saved successfully", Toast.LENGTH_SHORT).show()
                clearForm()
                // Redirect or update UI accordingly
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error saving user data: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearForm() {
        findViewById<TextInputEditText>(R.id.fullNameTextInputEditText).text?.clear()
        findViewById<TextInputEditText>(R.id.nameTextInputEditText).text?.clear()
        emailTextInputLayout.editText?.text?.clear()
        phoneNumberTextInputLayout.editText?.text?.clear()
        passwordTextInputLayout.editText?.text?.clear()
        findViewById<MaterialAutoCompleteTextView>(R.id.roleAutoCompleteTextView).setText("")
        findViewById<MaterialAutoCompleteTextView>(R.id.genderAutoCompleteTextView).setText("")
        findViewById<TextInputEditText>(R.id.addressTextInputEditText).text?.clear()
        classGroupTextInputLayout.editText?.text?.clear()
        childIdTextInputLayout.editText?.text?.clear()
    }

}
