package mobilesecurity.sit.project.userauth

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import mobilesecurity.sit.project.R
import mobilesecurity.sit.project.mainpage.MainActivity

class StaffLoginActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        supportActionBar?.hide()
        setContentView(R.layout.activity_staff_login)

        val backImageView = findViewById<ImageView>(R.id.backTeacherImageView)
        backImageView.setOnClickListener {
            // This will close the current activity and take you back to the previous activity
            finish()
        }

        // Initialize UI components
        val emailEditText = findViewById<TextInputEditText>(R.id.emailTextInputEditText)
        val passwordEditText = findViewById<TextInputEditText>(R.id.passwordTextInputEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val forgetPass = findViewById<TextView>(R.id.forgetPassView)

        // Set up the onClickListener for the login button
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            // Use the login function when the login button is clicked
            login(email,password)
        }

         //Set up the onClickListener for the forget password TextView
        forgetPass.setOnClickListener {
                    // Navigate to ForgetPasswordActivity
            val intent = Intent(this, ResetpassActivity::class.java)
            startActivity(intent)
        }



    }

    // Function to handle user login
    private fun login(email: String, password: String) {
        // Validate email and password, and authenticate using Firebase
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Login successful
                    checkUserRoleAndNavigate(FirebaseAuth.getInstance().currentUser)
                } else {
                    // Login failure
                    handleLoginFailure(task.exception)
                }
            }
    }

    private fun checkUserRoleAndNavigate(firebaseUser: FirebaseUser?) {
        firebaseUser?.let { user ->
            FirebaseFirestore.getInstance().collection("users").document(user.uid)
                .get().addOnSuccessListener { documentSnapshot ->
                    val role = documentSnapshot.getString("role")
                    if (role == "Teacher" || role == "Principal" )  {
                        // User is a parent, proceed to navigate
                        navigateToIndexActivity()
                    } else {
                        // User is not a parent, logout and show an error message
                        FirebaseAuth.getInstance().signOut()
                        Toast.makeText(this, "Access denied. You are not registered as a Staff.", Toast.LENGTH_LONG).show()
                    }
                }.addOnFailureListener { e ->
                    Log.e(ContentValues.TAG, "Error checking user role", e)
                    Toast.makeText(this, "Failed to verify user role.", Toast.LENGTH_SHORT).show()
                }
        } ?: run {
            // Handle the case where firebaseUser is null
            Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
        }
    }

    // Navigate to the MainActivity after successful login
    private fun navigateToIndexActivity() {
        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    // Handle login failure, including specific exceptions
    private fun handleLoginFailure(exception: Exception?) {
        val message = when (exception) {
            is FirebaseAuthInvalidCredentialsException -> "Invalid credentials"
            is FirebaseAuthInvalidUserException -> "User not found"
            else -> "Login failed: ${exception?.message}"
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}