package mobilesecurity.sit.project.userauth

import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import mobilesecurity.sit.project.R

class ResetpassActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        supportActionBar?.hide()
        setContentView(R.layout.activity_forget_password) // Ensure this matches your layout file name

//        val auth = FirebaseAuth.getInstance()

        // Initialize UI components
//        val emailEditText = findViewById<TextInputEditText>(R.id.emailTextInputEditText)
//        val resetPassButton = findViewById<MaterialButton>(R.id.resetPassButton)

//        resetPassButton.setOnClickListener {
//            val email = emailEditText.text.toString().trim()
//
//            if (email.isEmpty()) {
//                Toast.makeText(this, "Please enter your email address", Toast.LENGTH_SHORT).show()
//            } else {
//                // Send password reset email
//                auth.sendPasswordResetEmail(email)
//                    .addOnCompleteListener { task ->
//                        if (task.isSuccessful) {
//                            Toast.makeText(this, "Reset link sent to your email", Toast.LENGTH_LONG).show()
//                            // Optionally, navigate the user back to the login screen or inform them to check their email
//                        } else {
//                            // Handle exceptions, e.g., no user found for that email, network errors, etc.
//                            Toast.makeText(this, "Failed to send reset email", Toast.LENGTH_LONG).show()
//                        }
//                    }
//            }
//        }



    }
}
