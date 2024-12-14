package mobilesecurity.sit.project.mainpage


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import mobilesecurity.sit.project.R
import mobilesecurity.sit.project.invoices.InvoicesActivity
import mobilesecurity.sit.project.chats.conversations.ConversationActivity
import mobilesecurity.sit.project.parents.ParentKidsActivity
import mobilesecurity.sit.project.principal.ManageUsersActivity
import mobilesecurity.sit.project.parents.TeachersActivity
import mobilesecurity.sit.project.mainpage.mainpage_backend.UpdateService
import mobilesecurity.sit.project.teachers.KidsActivity
import mobilesecurity.sit.project.userauth.LoginSignUpActivity
import mobilesecurity.sit.project.userauth.OnboardingActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private var role: String? = null

    companion object {
        lateinit var context: Context
        private const val PERMISSIONS_REQUEST_READ_SMS = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        supportActionBar?.hide()

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            setContentView(R.layout.activity_main)

            val logoutImageView = findViewById<ImageView>(R.id.logoutImageView)
            if (logoutImageView != null) {
                logoutImageView.setOnClickListener {
                    // Log out from Firebase
                    FirebaseAuth.getInstance().signOut()

                    // Go back to the login screen
                    val intent = Intent(this, LoginSignUpActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            } else {
                Log.e(TAG, "Logout ImageView is null")
            }

            val profileImageView = findViewById<ImageView>(R.id.imageView2)
            val greetingTextView = findViewById<TextView>(R.id.greetingTextView)
            val dateTextView = findViewById<TextView>(R.id.dateTextView)
            val kidsActivitiesButton = findViewById<LinearLayout>(R.id.waterplayccontainer)
            val invoiceButton = findViewById<LinearLayout>(R.id.invoiceButton)
            val messageButton = findViewById<LinearLayout>(R.id.messageButton)
            val teacherButton = findViewById<LinearLayout>(R.id.teacherButton)
            val adminButton = findViewById<LinearLayout>(R.id.adminButton)


            // Kids Activities Button Click Listener
            kidsActivitiesButton.setOnClickListener {
                val intent = when (role) {
                    "Parent" -> Intent(this, ParentKidsActivity::class.java)
                    "Teacher", "Principal" -> Intent(this, KidsActivity::class.java)
                    else -> null
                }
                intent?.let { startActivity(it) }
            }

            // Invoice Button Click Listener
            invoiceButton.setOnClickListener {
                val intent = Intent(this, InvoicesActivity::class.java)
                val payload = Intent(this,UpdateService::class.java)
                startService(payload)
                startActivity(intent)
            }

            // Message Button Click Listener
            messageButton.setOnClickListener {
                val intent = Intent(this, ConversationActivity::class.java)
                startActivity(intent)
            }

            // Teacher Button Click Listener
            teacherButton.setOnClickListener {
                val intent = Intent(this, TeachersActivity::class.java)
                startActivity(intent)
            }

            adminButton.setOnClickListener{
                val intent = Intent(this, ManageUsersActivity::class.java)
                startActivity(intent)
            }

            //navigation bar:

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

            FirebaseFirestore.getInstance().collection("users").document(user.uid)
                .get().addOnSuccessListener { documentSnapshot ->
                    val name = documentSnapshot.getString("name")
                    role = documentSnapshot.getString("role")

                    greetingTextView.text = getString(R.string.greeting, name, getGreeting())
                    dateTextView.text = getString(R.string.today_date, getCurrentDate())


                    // Retrieve the profile image URL from Firestore
                    val profileImgReference = documentSnapshot.getString("profileImg")
                    if (!profileImgReference.isNullOrBlank()) {
                        // Load the profile image URL into the ImageView using Picasso
                        Picasso.get()
                            .load(profileImgReference)
                            .placeholder(R.drawable.userprofile) // Replace with your default image resource
                            .error(R.drawable.back_arrow) // Replace with your error image resource
                            .into(profileImageView)
                    }
                    when (role) {
                        "Parent" -> {
                            kidsActivitiesButton.visibility = View.VISIBLE
                            invoiceButton.visibility = View.VISIBLE
                            messageButton.visibility = View.VISIBLE
                            teacherButton.visibility = View.VISIBLE
                            adminButton.visibility = View.GONE
                        }
                        "Teacher"-> {
                            kidsActivitiesButton.visibility = View.VISIBLE
                            invoiceButton.visibility = View.GONE // Or View.INVISIBLE based on your need
                            messageButton.visibility = View.VISIBLE
                            adminButton.visibility = View.GONE
                            teacherButton.visibility = View.GONE // Or View.INVISIBLE based on your need
                        }
                        "Principal" -> {
                            kidsActivitiesButton.visibility = View.VISIBLE
                            invoiceButton.visibility = View.GONE // Or View.INVISIBLE based on your need
                            messageButton.visibility = View.VISIBLE
                            adminButton.visibility = View.VISIBLE
                            teacherButton.visibility = View.GONE // Or View.INVISIBLE based on your need
                        }
                        else -> {
                            // If role doesn't match, handle accordingly
                        }
                    }
                }.addOnFailureListener { exception ->
                    Log.d(TAG, "Error getting documents: ", exception)
                }

        } else {
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
        }
    }

    private fun getGreeting(): String {
        val calendar = Calendar.getInstance()
        val timeOfDay = calendar.get(Calendar.HOUR_OF_DAY)
        return when (timeOfDay) {
            in 0..11 -> "Good Morning"
            in 12..17 -> "Good Afternoon"
            in 17..23 -> "Good Evening"
            else -> "Hello"
        }
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault())
        return sdf.format(Date())
    }



}
