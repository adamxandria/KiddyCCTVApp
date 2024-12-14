package mobilesecurity.sit.project.userauth

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.api.Distribution.BucketOptions.Linear
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import mobilesecurity.sit.project.R

class ParentSignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var profileImageUri: Uri? = null

    @SuppressLint("MissingInflatedId", "WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        supportActionBar?.hide()
        setContentView(R.layout.activity_sign_up)

        val backImageView = findViewById<ImageView>(R.id.backImageView)
        val image: ImageView = findViewById(R.id.uploadImageView)
        val uploadBtn: LinearLayout = findViewById(R.id.uploadButton)
        val emailEditText = findViewById<TextInputEditText>(R.id.signupEmailTextInputEditText)
        val passwordEditText = findViewById<TextInputEditText>(R.id.signupPassTextInputEditText)
        val childIDEditText = findViewById<TextInputEditText>(R.id.childIdTextInputEditText)
        val nameEditText = findViewById<TextInputEditText>(R.id.parentNameTextInputEditText)
        val signUpButton = findViewById<Button>(R.id.registerButton)

        backImageView.setOnClickListener {
            finish()
        }

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                image.setImageURI(uri)
                profileImageUri = uri
            }
        }

        uploadBtn.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        signUpButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val childID = childIDEditText.text.toString().trim()
            val name = nameEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || childID.isEmpty() || name.isEmpty()) {
                Toast.makeText(this, "Please enter all details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerParent(email, password, name, childID)
        }
    }

    private fun registerParent(email: String, password: String, name: String, childID: String) {
        firestore.collection("Children").document(childID).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                uploadProfileImageAndSaveUser(email, password, name, childID)
                            } else {
                                handleSignUpFailure(task.exception)
                            }
                        }
                } else {
                    Toast.makeText(this, "Child ID is invalid", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to check Child ID: ${exception.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadProfileImageAndSaveUser(email: String, password: String, name: String, childID: String) {
        profileImageUri?.let { uri ->
            val storageRef = storage.reference.child("profileImages/${System.currentTimeMillis()}.jpg")
            storageRef.putFile(uri).addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    saveUserToFirestore(email, name, childID, downloadUri.toString())
                }.addOnFailureListener {
                    Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
                }
            }
        } ?: run {
            saveUserToFirestore(email, name, childID, null)
        }
    }

    private fun saveUserToFirestore(email: String, name: String, childID: String, imageUrl: String?) {
        val userData = hashMapOf(
            "name" to name,
            "email" to email,
            "childID" to childID,
            "role" to "Parent",
            "profileImg" to imageUrl.orEmpty() // Storing empty string if imageUrl is null
        )
        auth.currentUser?.let { user ->
            firestore.collection("users").document(user.uid).set(userData).addOnSuccessListener {
                navigateToLoginActivity()
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save parent data: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToLoginActivity() {
        Toast.makeText(this, "Signup successful", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, ParentLoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun handleSignUpFailure(exception: Exception?) {
        val message = when (exception) {
            is FirebaseAuthInvalidCredentialsException -> "Invalid credentials"
            is FirebaseAuthUserCollisionException -> "User already exists"
            else -> "Signup failed: ${exception?.localizedMessage}"
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
